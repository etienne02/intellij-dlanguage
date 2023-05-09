package io.github.intellij.dub.project

import com.intellij.openapi.externalSystem.importing.ProjectResolverPolicy
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.externalSystem.model.project.ContentRootData
import com.intellij.openapi.externalSystem.model.project.ModuleData
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import com.intellij.openapi.externalSystem.service.project.ExternalSystemProjectResolver
import io.github.intellij.dub.Dub
import io.github.intellij.dub.settings.DubExecutionSettings
import java.io.File

class DubProjectResolver : ExternalSystemProjectResolver<DubExecutionSettings> {

    override fun resolveProjectInfo(
        id: ExternalSystemTaskId,
        projectPath: String,
        isPreviewMode: Boolean,
        settings: DubExecutionSettings?,
        resolverPolicy: ProjectResolverPolicy?,
        listener: ExternalSystemTaskNotificationListener
    ): DataNode<ProjectData> {
        if (isPreviewMode) {
            val projectName = File(projectPath).name
            val projectData = ProjectData(Dub.SYSTEM_ID, projectName, projectPath, projectPath)
            val projectDataNode = DataNode(ProjectKeys.PROJECT, projectData, null)

            val ideProjectPath = settings?.ideProjectPath
            val mainModuleFileDirectoryPath = ideProjectPath?:projectPath
            val moduleData = ModuleData(projectName, Dub.SYSTEM_ID, "D",
                projectName, mainModuleFileDirectoryPath, projectPath)
            projectDataNode
                .createChild(ProjectKeys.MODULE, moduleData)
                .createChild(ProjectKeys.CONTENT_ROOT, ContentRootData(Dub.SYSTEM_ID, projectPath))
            return projectDataNode
        }
        val projectName = File(projectPath).name
        val projectData = ProjectData(Dub.SYSTEM_ID, projectName, projectPath, projectPath)
        val projectDataNode = DataNode(ProjectKeys.PROJECT, projectData, null)

        val ideProjectPath = settings?.ideProjectPath
        val mainModuleFileDirectoryPath = ideProjectPath?:projectPath
        // TODO use dub to create all path and others
        val moduleData = ModuleData(projectName, Dub.SYSTEM_ID, "D",
            projectName, mainModuleFileDirectoryPath, projectPath)
        projectDataNode
            .createChild(ProjectKeys.MODULE, moduleData)
            .createChild(ProjectKeys.CONTENT_ROOT, ContentRootData(Dub.SYSTEM_ID, projectPath))
        return projectDataNode
    }
    override fun cancelTask(taskId: ExternalSystemTaskId, listener: ExternalSystemTaskNotificationListener): Boolean {
        return true
    }
}
