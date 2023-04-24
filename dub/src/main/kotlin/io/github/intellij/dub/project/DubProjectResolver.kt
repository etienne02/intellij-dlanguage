package io.github.intellij.dub.project

import com.intellij.openapi.externalSystem.importing.ProjectResolverPolicy
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import com.intellij.openapi.externalSystem.service.project.ExternalSystemProjectResolver
import com.intellij.util.containers.MultiMap
import io.github.intellij.dub.settings.DubExecutionSettings

class DubProjectResolver : ExternalSystemProjectResolver<DubExecutionSettings> {

    //private val cancellationMap: MultiMap<ExternalSystemTaskId, CancellationTokenSource> = MultiMap.createConcurrent()

    override fun resolveProjectInfo(
        id: ExternalSystemTaskId,
        projectPath: String,
        isPreviewMode: Boolean,
        settings: DubExecutionSettings?,
        resolverPolicy: ProjectResolverPolicy?,
        listener: ExternalSystemTaskNotificationListener
    ): DataNode<ProjectData>? {
        return null
    }
    override fun cancelTask(taskId: ExternalSystemTaskId, listener: ExternalSystemTaskNotificationListener): Boolean {

        return true
    }
}
