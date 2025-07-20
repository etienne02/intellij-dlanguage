package io.github.intellij.dub.clion

import com.intellij.execution.ExecutorRegistry
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.openapi.project.Project
import com.jetbrains.cidr.execution.CidrBuildConfiguration
import com.jetbrains.cidr.execution.build.CidrBuildConfigurationProvider
import io.github.intellij.dub.run.DlangRunDubConfiguration
import io.github.intellij.dub.run.DubBuildRunner
import io.github.intellij.dub.run.isBuildConfiguration

class DubBuildConfigurationProvider : CidrBuildConfigurationProvider {
    override fun getBuildableConfigurations(project: Project): List<CidrBuildConfiguration> {
        val runManager = RunManager.getInstance(project)
        val configuration = runManager.selectedConfiguration?.configuration as DlangRunDubConfiguration ?: return emptyList()
        val buildConfiguration = getBuildConfiguration(configuration) ?: return emptyList()
        val environment = createBuildEnvironment(buildConfiguration) ?: return emptyList()
        return listOf(ClionDubBuildConfiguration(buildConfiguration, environment))
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
