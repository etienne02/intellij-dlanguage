package io.github.intellij.dub

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope

internal object DubCoroutineScope {

    @Service(Service.Level.PROJECT)
    private class ProjectService(val coroutineScope: CoroutineScope)

    val Project.dubCoroutineScope: CoroutineScope
        get() = service<ProjectService>().coroutineScope
}
