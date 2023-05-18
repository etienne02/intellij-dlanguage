package io.github.intellij.dub.project

import com.intellij.openapi.externalSystem.importing.ProjectResolverPolicy
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.externalSystem.model.project.*
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import com.intellij.openapi.externalSystem.service.project.ExternalSystemProjectResolver
import com.intellij.openapi.roots.DependencyScope
import io.github.intellij.dlanguage.DLanguage
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

        val projectName = File(projectPath).name
        val projectData = ProjectData(Dub.SYSTEM_ID, projectName, projectPath, projectPath)
        val projectDataNode = DataNode(ProjectKeys.PROJECT, projectData, null)

        val ideProjectPath = settings?.ideProjectPath
        val mainModuleFileDirectoryPath = ideProjectPath ?: projectPath
        val mainModule = ModuleData(projectName, Dub.SYSTEM_ID, DLanguage.MODULE_TYPE_ID,
            projectName, mainModuleFileDirectoryPath, projectPath)
        val mainContentRoot = ContentRootData(Dub.SYSTEM_ID, projectPath)
        projectDataNode.createChild(ProjectKeys.MODULE, mainModule)
            .createChild(ProjectKeys.CONTENT_ROOT, mainContentRoot)

        if (isPreviewMode) {
            return projectDataNode
        }

        // TODO use dub to create all path and others
        // TODO ensure dub is configured
        val dubBinaryPath = settings?.dubBinaryPath ?: "/usr/bin/dub"//ToolKey.DUB_KEY.path!!
        val parser = DubConfigurationParserNew(projectPath, dubBinaryPath)
        if (!parser.canUseDub()) {
            return projectDataNode
        }
        val dubProject = parser.dubProject.get()

        projectData.description = dubProject.rootPackage.description

        // root package
        dubProject.rootPackage.sourcesDirs.forEach {source->
            mainContentRoot.storePath(ExternalSystemSourceType.SOURCE, "${dubProject.rootPackage.path}$source")
        }

        val moduleMap: MutableMap<String, ModuleData> = HashMap()
        val moduleDataMap: MutableMap<String, DataNode<ModuleData>> = HashMap()
        val libraryDependencyMap: MutableMap<String, LibraryDependencyData> = HashMap()
        dubProject.packages.forEach{
            if (it.name.startsWith(dubProject.rootPackageName + ":")) {
                // It’s a sub-package
                // FIXME: CLion can’t work with multi module, so we need to find another way to make it work
                val subModuleData = ModuleData(it.name, Dub.SYSTEM_ID, "D",
                    it.name, it.path, it.path)
                moduleMap[it.name] = subModuleData
                val subModuleContentRoot = ContentRootData(Dub.SYSTEM_ID, it.path)
                val moduleDataNode = projectDataNode
                    .createChild(ProjectKeys.MODULE, subModuleData)
                moduleDataNode
                    .createChild(ProjectKeys.CONTENT_ROOT, subModuleContentRoot)
                moduleDataMap[it.name] = moduleDataNode

                it.sourcesDirs.forEach {source ->
                    subModuleContentRoot.storePath(ExternalSystemSourceType.SOURCE, "${it.path}$source")
                }
            } else {
                // It’s a library
                val fullName = it.name + "-" + it.version
                val libNode = LibraryData(Dub.SYSTEM_ID, fullName)
                projectDataNode.createChild(ProjectKeys.LIBRARY, libNode)
                it.sourcesDirs.forEach {source ->
                    libNode.addPath(LibraryPathType.SOURCE, source)
                }
                val dependencyNode = LibraryDependencyData(mainModule, libNode, LibraryLevel.MODULE)
                dependencyNode.scope = DependencyScope.COMPILE
                libraryDependencyMap[it.name] = dependencyNode
                // TODO confirm
                projectDataNode.createChild(ProjectKeys.LIBRARY_DEPENDENCY, dependencyNode)
            }
        }

        dubProject.packages.forEach{
            it.dependencies.forEach{dependency ->
                if (dependency in moduleMap)
                    projectDataNode.createChild(ProjectKeys.MODULE_DEPENDENCY, ModuleDependencyData(moduleMap[it.name]?:mainModule, moduleMap[dependency]!!))
                else if (dependency in libraryDependencyMap) {
                    moduleDataMap[it.name]?.createChild(ProjectKeys.LIBRARY_DEPENDENCY, libraryDependencyMap[dependency]!!)
                }
            }
        }
        return projectDataNode
    }

    override fun cancelTask(taskId: ExternalSystemTaskId, listener: ExternalSystemTaskNotificationListener): Boolean {
        return true
    }
}
