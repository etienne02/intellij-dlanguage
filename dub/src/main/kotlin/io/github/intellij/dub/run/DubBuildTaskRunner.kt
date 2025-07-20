package io.github.intellij.dub.run

import com.intellij.build.BuildContentManager
import com.intellij.build.BuildViewManager
import com.intellij.execution.ExecutorRegistry
import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.TransactionGuard
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.BackgroundTaskQueue
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.UserDataHolderEx
import com.intellij.task.*
import com.intellij.task.impl.ProjectModelBuildTaskImpl
import com.intellij.util.ui.UIUtil
import org.jetbrains.concurrency.AsyncPromise
import org.jetbrains.concurrency.Promise
import org.jetbrains.concurrency.isPending
import org.jetbrains.concurrency.rejectedPromise
import org.jetbrains.concurrency.resolvedPromise
import java.util.concurrent.*
import java.util.concurrent.ConcurrentHashMap.KeySetView
import java.util.concurrent.atomic.AtomicInteger

class DubBuildTaskRunner : ProjectTaskRunner() {

    override fun run(project: Project, context: ProjectTaskContext, vararg tasks: ProjectTask): Promise<Result> {
        if (project.isDisposed)
            return rejectedPromise("Project is already disposed")

        // TODO replace with coroutines

        val resultPromise = AsyncPromise<Result>()
        val waitingIndicator = CompletableFuture<ProgressIndicator>()
        val queuedTask = BackgroundableProjectTaskRunner(
            project,
            tasks,
            this,
            resultPromise,
            waitingIndicator
        )

        /*if (!ApplicationManager.getApplication().isHeadlessEnvironment) {
            WaitingTask(project, waitingIndicator, queuedTask.executionStarted).queue()
        }*/
        DubBuildSessionsQueueManager.getInstance(project)
            .buildSessionsQueue
            .run(queuedTask, ModalityState.defaultModalityState(), EmptyProgressIndicator())

        return resultPromise
    }

    override fun canRun(p0: ProjectTask): Boolean {
        TODO("Not yet implemented")
    }

    override fun canRun(
        project: Project,
        projectTask: ProjectTask,
        projectTaskContext: ProjectTaskContext?
    ): Boolean {
        return when (projectTask) {
            is ModuleFilesBuildTask -> false
            is ModuleBuildTask -> {
                val runManager = RunManager.getInstance(project)
                val buildableElement = runManager.selectedConfiguration?.configuration
                buildableElement is DlangRunDubConfiguration
            }
            is ProjectModelBuildTask<*> -> {
                val buildableElement = projectTask.buildableElement
                buildableElement is DubBuildConfiguration
            }
            else -> false
        }
    }

    fun expandTask(task: ProjectTask): List<ProjectTask> {
        if (task !is ModuleBuildTask) return listOf(task)

        val project = task.module.project
        val runManager = RunManager.getInstance(project)

        val selectedConfiguration = runManager.selectedConfiguration?.configuration as? DlangRunDubConfiguration
        if (selectedConfiguration != null) {
            val buildConfiguration = getBuildConfiguration(selectedConfiguration)
            val environment = createBuildEnvironment(buildConfiguration) ?: return emptyList()
            val buildableElement = DubBuildConfiguration(buildConfiguration, environment)
            return listOf(ProjectModelBuildTaskImpl(buildableElement, task.isIncrementalBuild))
        }

        val executor = ExecutorRegistry.getInstance().getExecutorById(DefaultRunExecutor.EXECUTOR_ID) ?: return emptyList()
        val runner = ProgramRunner.findRunnerById(DubBuildRunner.RUNNER_ID) ?: return emptyList()

        val settings = createDubCommandRunConfiguration(runManager)
        val environment = ExecutionEnvironment(executor, runner, settings, project)
        val configuration = settings.configuration as? DlangRunDubConfiguration ?: return listOf()
        configuration.generalDubOptions = 0
        //configuration.emulateTerminal = false
        val buildableElement = DubBuildConfiguration(configuration, environment)
        return listOf(ProjectModelBuildTaskImpl(buildableElement, task.isIncrementalBuild))
    }

    fun executeTask(task: ProjectTask): Promise<Result> {
        if (task !is ProjectModelBuildTask<*>) {
            return resolvedPromise(TaskRunnerResults.ABORTED)
        }

        val buildConfiguration = task.buildableElement as DubBuildConfiguration

        val result = try {
            val buildFuture = DubBuildManager.build(buildConfiguration)
            val buildResult = buildFuture.get()
            when {
                buildResult.canceled -> TaskRunnerResults.ABORTED
                buildResult.succeeded -> TaskRunnerResults.SUCCESS
                else -> TaskRunnerResults.FAILURE
            }
        } catch (e: ExecutionException) {
            TaskRunnerResults.FAILURE
        }

        val promise = AsyncPromise<Result>()
        promise.setResult(result)
        return promise
    }
}

fun createDubCommandRunConfiguration(runManager: RunManager) : RunnerAndConfigurationSettings {
    val runnerAndConfigurationSettings = runManager.createConfiguration(
        "build",
        DlangRunDubConfigurationType.getInstance().configurationFactories.single()
    )
    val configuration = runnerAndConfigurationSettings.configuration as DlangRunDubConfiguration
    return runnerAndConfigurationSettings
}

private class BackgroundableProjectTaskRunner(
    project: Project,
    private val tasks: Array<out ProjectTask>,
    private val parentRunner: DubBuildTaskRunner,
    private val totalPromise: AsyncPromise<ProjectTaskRunner.Result>,
    private val waitingIndicator: Future<ProgressIndicator>
) : Task.Backgroundable(project, "Building", true) {
    val executionStarted: CompletableFuture<Boolean> = CompletableFuture()

    override fun run(indicator: ProgressIndicator) {
        if (!waitForStart()) {
            if (totalPromise.state == Promise.State.PENDING) {
                totalPromise.cancel()
            }
            return
        }

        val allTasks = collectTasks(tasks)
        if (allTasks.isEmpty()) {
            totalPromise.setResult(TaskRunnerResults.FAILURE)
            return
        }

        try {
            for (task in allTasks) {
                val promise = runTask(task)
                if (promise.blockingGet(Integer.MAX_VALUE) != TaskRunnerResults.SUCCESS) {
                    // Do not continue session if one of builds failed
                    totalPromise.setResult(TaskRunnerResults.FAILURE)
                    break
                }
            }

            // everything succeeded - set final result to success
            if (totalPromise.isPending) {
                totalPromise.setResult(TaskRunnerResults.SUCCESS)
            }
        } catch (e: InterruptedException) {
            totalPromise.setResult(TaskRunnerResults.ABORTED)
            throw ProcessCanceledException(e)
        } catch (e: CancellationException) {
            totalPromise.setResult(TaskRunnerResults.ABORTED)
            throw ProcessCanceledException(e)
        } catch (e: Throwable) {
            LOG.error(e)
            totalPromise.setResult(TaskRunnerResults.FAILURE)
        }
    }

    private fun waitForStart(): Boolean {
        //if (isHeadlessEnvironment) return true

        try {
            // Check if this build wasn't cancelled while it was in queue through waiting indicator
            val cancelled = waitingIndicator.get().isCanceled
            // Notify waiting background task that this build started and there is no more need for this indicator
            executionStarted.complete(true)
            return !cancelled
        } catch (e: InterruptedException) {
            totalPromise.setResult(TaskRunnerResults.ABORTED)
            throw ProcessCanceledException(e)
        } catch (e: CancellationException) {
            totalPromise.setResult(TaskRunnerResults.ABORTED)
            throw ProcessCanceledException(e)
        } catch (e: Throwable) {
            LOG.error(e)
            totalPromise.setResult(TaskRunnerResults.FAILURE)
            throw ProcessCanceledException(e)
        }
    }

    private fun collectTasks(tasks: Array<out ProjectTask>): Collection<ProjectTask> {
        val expandedTasks = tasks.filter { parentRunner.canRun(it) }.map { parentRunner.expandTask(it) }
        return if (expandedTasks.any { it.isEmpty() }) emptyList() else expandedTasks.flatten()
    }

    private fun runTask(task: ProjectTask): Promise<ProjectTaskRunner.Result> = parentRunner.executeTask(task)
}

private val LOG: Logger = logger<DubBuildTaskRunner>()


@Service(Service.Level.PROJECT)
class DubBuildSessionsQueueManager(project: Project) {
    val buildSessionsQueue: BackgroundTaskQueue = BackgroundTaskQueue(project, "Building")

    companion object {
        fun getInstance(project: Project): DubBuildSessionsQueueManager = project.service()
    }
}

private class WaitingTask(
    project: Project,
    val waitingIndicator: CompletableFuture<ProgressIndicator>,
    val executionStarted: Future<Boolean>
) : Task.Backgroundable(project, "Waiting for current build to finish", true) {

    override fun run(indicator: ProgressIndicator) {
        // Wait until queued task will start executing.
        // Needed so that user can cancel build tasks from queue.
        waitingIndicator.complete(indicator)
        try {
            while (true) {
                indicator.checkCanceled()
                try {
                    executionStarted.get(100, TimeUnit.MILLISECONDS)
                    break
                } catch (ignore: TimeoutException) {
                }
            }
        } catch (e: CancellationException) {
            throw ProcessCanceledException(e)
        } catch (e: InterruptedException) {
            throw ProcessCanceledException(e)
        } catch (e: ExecutionException) {
            LOG.error(e)
            throw ProcessCanceledException(e)
        }
    }
}

object DubBuildManager {
    fun build(buildConfiguration: DubBuildConfiguration): Future<DubBuildResult> {
        val configuration = buildConfiguration.configuration
        val environment = buildConfiguration.environment
        val project = environment.project
        val state = DlangRunDubState(
            environment,
            configuration
        )


        BuildContentManager.getInstance(project).getOrCreateToolWindow()
        val buildId = Any()
        return execute(DubBuildContext(
            project = project,
            environment = environment,
            taskName = "Build",
            progressTitle = "Building",
            buildId = buildId,
            parentId = buildId
        )) {
            val buildProgressListener = project.service<BuildViewManager>()
            //if (!isHeadlessEnvironment) {
                @Suppress("UsePropertyAccessSyntax")
                val buildToolWindow = BuildContentManager.getInstance(project).getOrCreateToolWindow()
                buildToolWindow.setAvailable(true, null)
                //if (environment.isActivateToolWindowBeforeRun) {
                    buildToolWindow.activate(null)
                //}
            //}

            processHandler = state.startProcess()
            //processHandler?.addProcessListener(DubBuildAdapter(this, buildProgressListener))
            processHandler?.startNotify()
        }
    }

    private fun execute(
        context: DubBuildContext,
        doExecute: DubBuildContext.() -> Unit
    ): Future<DubBuildResult> {

        //context.environment.notifyProcessStartScheduled()
        val processCreationLock = Any()
        val indicatorResult = CompletableFuture<ProgressIndicator>()
        UIUtil.invokeLaterIfNeeded {
            object : Task.Backgroundable(context.project, context.taskName, true) {
                override fun run(indicator: ProgressIndicator) {
                    indicatorResult.complete(indicator)

                    var wasCanceled = false
                    while (!context.result.isDone) {
                        if (!wasCanceled && indicator.isCanceled) {
                            wasCanceled = true
                            synchronized(processCreationLock) {
                                context.processHandler?.destroyProcess()
                            }
                        }

                        try {
                            Thread.sleep(100)
                        } catch (e: InterruptedException) {
                            throw ProcessCanceledException(e)
                        }
                    }
                }
            }.queue()

            try {
                context.indicator = indicatorResult.get()
            } catch (e: ExecutionException) {
                context.result.completeExceptionally(e)
            }
        }
        context.indicator?.text = context.progressTitle
        context.indicator?.text2 = ""

        ApplicationManager.getApplication().executeOnPooledThread {
            if (!context.waitAndStart()) return@executeOnPooledThread
            //context.environment.notifyProcessStarting()

            /*if (isUnitTestMode) {
                context.doExecute()
                return@executeOnPooledThread
            }*/

            invokeLater {
                synchronized(processCreationLock) {
                    val isCanceled = context.indicator?.isCanceled ?: false
                    if (isCanceled) {
                        context.canceled()
                    } else {
                        FileDocumentManager.getInstance().saveAllDocuments()
                        context.doExecute()
                    }
                }
            }
        }

        return context.result
    }
}

data class DubBuildResult(
    val succeeded: Boolean,
    val canceled: Boolean,
    val started: Long,
    val duration: Long = 0,
    val errors: Int = 0,
    val warnings: Int = 0,
    val message: String = ""
)

abstract class DubBuildContextBase(
    val project: Project,
    @NlsContexts.ProgressText val progressTitle: String,
    val isTestBuild: Boolean,
    val buildId: Any,
    val parentId: Any
) {
    @Volatile
    var indicator: ProgressIndicator? = null

    val errors: AtomicInteger = AtomicInteger()
    val errorCodes: KeySetView<String, Boolean> = ConcurrentHashMap.newKeySet()
    val warnings: AtomicInteger = AtomicInteger()

    @Volatile
    var artifacts: List<String> = emptyList()
}

class DubBuildContext(
    project: Project,
    val environment: ExecutionEnvironment,
    @NlsContexts.ProgressTitle val taskName: String,
    @NlsContexts.ProgressText progressTitle: String,
    buildId: Any,
    parentId: Any
) : DubBuildContextBase(project, progressTitle, false, buildId, parentId) {

    @Volatile
    var processHandler: ProcessHandler? = null

    private val buildSemaphore: Semaphore = project.getUserData(BUILD_SEMAPHORE_KEY)
        ?: (project as UserDataHolderEx).putUserDataIfAbsent(BUILD_SEMAPHORE_KEY, Semaphore(1))

    val result: CompletableFuture<DubBuildResult> = CompletableFuture()

    val started: Long = System.currentTimeMillis()
    @Volatile
    var finished: Long = started
    private val duration: Long get() = finished - started

    fun waitAndStart(): Boolean {
        indicator?.pushState()
        try {
            indicator?.text = "Waiting for build to finish"
            indicator?.text2 = ""
            while (true) {
                indicator?.checkCanceled()
                try {
                    if (buildSemaphore.tryAcquire(100, TimeUnit.MILLISECONDS)) break
                } catch (e: InterruptedException) {
                    throw ProcessCanceledException()
                }
            }
        } catch (e: ProcessCanceledException) {
            canceled()
            return false
        } finally {
            indicator?.popState()
        }
        return true
    }

    fun finished(isSuccess: Boolean) {
        val isCanceled = indicator?.isCanceled ?: false

        //environment.artifacts = artifacts.takeIf { isSuccess && !isCanceled }

        finished = System.currentTimeMillis()
        buildSemaphore.release()

        val finishMessage: String
        val finishDetails: String?

        val errors = errors.get()
        val warnings = warnings.get()

        // We report successful builds with errors or warnings correspondingly
        val messageType = if (isCanceled) {
            finishMessage = "Build canceled"
            finishDetails = null
            MessageType.INFO
        } else {
            val hasWarningsOrErrors = errors > 0 || warnings > 0
            finishMessage = if (isSuccess) "Build finished" else "Build failed"
            finishDetails = if (hasWarningsOrErrors) {
                val errorsString = if (errors == 1) "error" else "errors"
                val warningsString = if (warnings == 1) "warning" else "warnings"
                "Error or warning"
            } else {
                null
            }

            when {
                !isSuccess -> MessageType.ERROR
                hasWarningsOrErrors -> MessageType.WARNING
                else -> MessageType.INFO
            }
        }

        result.complete(DubBuildResult(
            succeeded = isSuccess,
            canceled = isCanceled,
            started = started,
            duration = duration,
            errors = errors,
            warnings = warnings,
            message = finishMessage
        ))

        //showBuildNotification(project, messageType, finishMessage, finishDetails, duration)
    }

    fun canceled() {
        finished = System.currentTimeMillis()

        result.complete(DubBuildResult(
            succeeded = false,
            canceled = true,
            started = started,
            duration = duration,
            errors = errors.get(),
            warnings = warnings.get(),
            message = "$taskName canceled"
        ))

        //environment.notifyProcessNotStarted()
    }

    companion object {
        private val BUILD_SEMAPHORE_KEY: Key<Semaphore> = Key.create("BUILD_SEMAPHORE_KEY")
    }
}

