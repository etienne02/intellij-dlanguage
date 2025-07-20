package io.github.intellij.dub.run

import com.intellij.execution.BeforeRunTask
import com.intellij.execution.BeforeRunTaskProvider
import com.intellij.execution.ExecutorRegistry
import com.intellij.execution.RunManager
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.util.Key
import com.intellij.task.ProjectTaskManager
import org.jetbrains.annotations.Nls
import java.util.concurrent.CompletableFuture
import javax.swing.Icon

class DubBuildTaskProvider : BeforeRunTaskProvider<DubBuildTaskProvider.BuildTask>() {

    override fun getId(): Key<BuildTask> = ID
    override fun getName(): @Nls(capitalization = Nls.Capitalization.Title) String = "Build"
    override fun getIcon(): Icon = AllIcons.Actions.Compile

    override fun createTask(runConfiguration: RunConfiguration): BuildTask? =
        if (runConfiguration is DlangRunDubConfiguration) BuildTask() else null

    private fun doExecuteTask(
        buildConfiguration: DlangRunDubConfiguration,
        environment: ExecutionEnvironment
    ): Boolean {
        val buildEnvironment = createBuildEnvironment(buildConfiguration, environment) ?: return false
        val buildableElement = DubBuildConfiguration(buildConfiguration, buildEnvironment)

        val result = CompletableFuture<Boolean>()
        ProjectTaskManager.getInstance(environment.project).build(buildableElement).onProcessed {
            result.complete(!it!!.hasErrors() && !it.isAborted)
        }
        return result.get()
    }

    override fun executeTask(
        dataContext: DataContext,
        configuration: RunConfiguration,
        environment: ExecutionEnvironment,
        buildTask: BuildTask
    ): Boolean {
        if (configuration !is DlangRunDubConfiguration) return false
        val buildConfiguration = getBuildConfiguration(configuration) ?: return false

        return doExecuteTask(buildConfiguration, environment)
    }

    class BuildTask : BeforeRunTask<BuildTask>(ID)

    companion object {
        @JvmField
        val ID: Key<BuildTask> = Key.create("DUB.BUILD_TASK_PROVIDER")
    }
}

fun getBuildConfiguration(configuration: DlangRunDubConfiguration): DlangRunDubConfiguration {
    if (isBuildConfiguration(configuration)) return configuration

    val buildConfiguration = configuration.clone() as DlangRunDubConfiguration
    buildConfiguration.generalDubOptions = 0
    buildConfiguration.name = "Build `${buildConfiguration.name}`"

    return buildConfiguration
}

fun createBuildEnvironment(
    buildConfiguration: DlangRunDubConfiguration,
    environment: ExecutionEnvironment? = null
): ExecutionEnvironment? {
    require(isBuildConfiguration(buildConfiguration))
    val project = buildConfiguration.project
    val runManager = RunManager.getInstance(project) as? RunManagerImpl ?: return null
    val executor = ExecutorRegistry.getInstance().getExecutorById(DefaultRunExecutor.EXECUTOR_ID) ?: return null
    val runner = ProgramRunner.findRunnerById(DubBuildRunner.RUNNER_ID) ?: return null
    val settings = RunnerAndConfigurationSettingsImpl(runManager, buildConfiguration)
    settings.isActivateToolWindowBeforeRun = true // TODO isActivateToolWindowBeforeRun
    val buildEnvironment = ExecutionEnvironment(executor, runner, settings, project)
    environment?.copyUserDataTo(buildEnvironment)
    return buildEnvironment
}
