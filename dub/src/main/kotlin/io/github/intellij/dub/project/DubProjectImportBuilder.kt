package io.github.intellij.dub.project

import com.intellij.openapi.externalSystem.model.ExternalSystemDataKeys
import com.intellij.openapi.externalSystem.service.project.manage.ExternalProjectsManagerImpl.setupCreatedProject
import com.intellij.openapi.module.ModifiableModuleModel
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.packaging.artifacts.ModifiableArtifactModel
import com.intellij.platform.backend.observation.launchTracked
import com.intellij.projectImport.DeprecatedProjectBuilderForImport
import com.intellij.projectImport.ProjectImportBuilder
import com.intellij.projectImport.ProjectImportProvider.getDefaultPath
import com.intellij.projectImport.ProjectOpenProcessor
import io.github.intellij.dlanguage.DLanguage
import io.github.intellij.dub.DubCoroutineScope.dubCoroutineScope
import javax.swing.Icon


/**
 * Do not use this project import builder directly.
 *
 * Internal stable Api
 * Use [com.intellij.ide.actions.ImportModuleAction.createFromWizard] to import (attach) a new project.
 * Use [com.intellij.ide.impl.ProjectUtil.openOrImport] to open (import) a new project.
 *
 * Internal experimental Api
 * Use [io.github.intellij.dub.project.openDubProject] to open (import) a new dub project.
 * Use [io.github.intellij.dub.project.linkAndSyncDubProject] to attach a dub project to an opened idea project.
 */
@Deprecated("Use the open and link project utility functions")
internal class DubProjectImportBuilder : ProjectImportBuilder<Any>(), DeprecatedProjectBuilderForImport {

    override fun getName(): String = DubProjectOpenProcessor.NAME

    override fun getIcon(): Icon = DLanguage.Icons.MODULE

    override fun getList(): List<Any> = emptyList()

    override fun isMarked(dubPackage: Any): Boolean = true

    override fun setOpenProjectSettingsAfter(on: Boolean) {}

    private fun getPathToImport(path: String): String {
        val localForImport = LocalFileSystem.getInstance()
        val file = localForImport.refreshAndFindFileByPath(path)
        return file?.let(::getDefaultPath) ?: path
    }

    override fun setFileToImport(path: String) = super.setFileToImport(getPathToImport(path))

    override fun createProject(name: String, path: String): Project? {
        return setupCreatedProject(super.createProject(name, path))?.also {
            it.putUserData(ExternalSystemDataKeys.NEWLY_CREATED_PROJECT, true)
        }
    }

    override fun validate(currentProject: Project?, project: Project): Boolean {
        return canLinkAndRefreshDubProject(fileToImport, project)
    }

    override fun commit(
        project: Project,
        modifiableModuleModel: ModifiableModuleModel?,
        modulesProvider: ModulesProvider?,
        modifiableArtifactModel: ModifiableArtifactModel?
    ): List<Module> {
        project.dubCoroutineScope.launchTracked {
            linkAndSyncDubProject(project, fileToImport)
        }
        return emptyList()
    }

    override fun getProjectOpenProcessor(): ProjectOpenProcessor =
        ProjectOpenProcessor.EXTENSION_POINT_NAME.findExtensionOrFail(DubProjectOpenProcessor::class.java)
}
