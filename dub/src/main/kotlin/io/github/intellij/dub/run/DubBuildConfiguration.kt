package io.github.intellij.dub.run

import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.roots.ProjectModelBuildableElement
import com.intellij.openapi.roots.ProjectModelExternalSource

open class DubBuildConfiguration(
    val configuration: DlangRunDubConfiguration,
    val environment: ExecutionEnvironment
) : ProjectModelBuildableElement {
    open val enabled: Boolean get() = true

    init {
        require(isBuildConfiguration(configuration))
    }

    override fun getExternalSource(): ProjectModelExternalSource? = null
}

fun isBuildConfiguration(configuration: DlangRunDubConfiguration) =
    // 0 mean build
    configuration.generalDubOptions == 0
