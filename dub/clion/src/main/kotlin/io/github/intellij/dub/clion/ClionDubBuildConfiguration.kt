package io.github.intellij.dub.clion

import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.roots.ProjectModelExternalSource
import com.jetbrains.cidr.execution.CidrBuildConfiguration
import io.github.intellij.dub.run.DlangRunDubConfiguration
import io.github.intellij.dub.run.DubBuildConfiguration

class ClionDubBuildConfiguration(configuration: DlangRunDubConfiguration, environment: ExecutionEnvironment) :
    DubBuildConfiguration(configuration, environment), CidrBuildConfiguration {

    override val enabled: Boolean get() = true

    override fun getName(): String = "Dub Build"

    override fun getExternalSource(): ProjectModelExternalSource? = super<DubBuildConfiguration>.getExternalSource()
}
