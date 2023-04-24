package io.github.intellij.dub.project

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.externalSystem.importing.AbstractOpenProjectProvider
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.model.internal.InternalExternalProjectInfo
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode
import com.intellij.openapi.externalSystem.service.project.ExternalProjectRefreshCallback
import com.intellij.openapi.externalSystem.service.project.ProjectDataManager
import com.intellij.openapi.externalSystem.service.project.manage.ExternalProjectsManagerImpl
import com.intellij.openapi.externalSystem.service.project.trusted.ExternalSystemTrustedProjectDialog
import com.intellij.openapi.externalSystem.service.ui.ExternalProjectDataSelectorDialog
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import io.github.intellij.dub.Dub.Companion.SYSTEM_ID
import io.github.intellij.dub.settings.DubSettings

internal class DubOpenProjectProvider : AbstractOpenProjectProvider() {
    override val systemId: ProjectSystemId = SYSTEM_ID

    override fun isProjectFile(file: VirtualFile): Boolean = isDubProjectFile(file)

    override fun linkToExistingProject(projectFile: VirtualFile, project: Project) {
        LOG.debug("Link Dub project '$projectFile' to existing project ${project.name}")

        val projectPath = getProjectDirectory(projectFile).toNioPath()

        if (ExternalSystemTrustedProjectDialog.confirmLinkingUntrustedProject(project, systemId, projectPath)) {
            val settings = createLinkSettings(projectPath, project)

            val externalProjectPath = settings.externalProjectPath
            try {
                ExternalSystemApiUtil.getSettings(project, SYSTEM_ID).linkProject(settings)
            } catch (_: IllegalArgumentException) {
                LOG.debug("Dub project '$projectFile' already linked to the existing project '$project.name'.")
                return
            }

            if (!Registry.`is`("external.system.auto.import.disabled")) {
                ExternalSystemUtil.refreshProject(
                    externalProjectPath,
                    ImportSpecBuilder(project, SYSTEM_ID)
                        .usePreviewMode()
                        .use(ProgressExecutionMode.MODAL_SYNC)
                )

                ExternalProjectsManagerImpl.getInstance(project).runWhenInitialized {
                    ExternalSystemUtil.refreshProject(
                        externalProjectPath,
                        ImportSpecBuilder(project, SYSTEM_ID)
                            .callback(createFinalImportCallback(project, externalProjectPath))
                    )
                }
            }
        }
    }

    private fun createFinalImportCallback(project: Project, externalProjectPath: String): ExternalProjectRefreshCallback {
        return object : ExternalProjectRefreshCallback {
            override fun onSuccess(externalProject: DataNode<ProjectData>?) {
                if (externalProject == null) return
                selectDataToImport(project, externalProjectPath, externalProject)
                importData(project, externalProject)
            }
        }
    }

    private fun selectDataToImport(project: Project, externalProjectPath: String, externalProject: DataNode<ProjectData>) {
        val settings = DubSettings.getInstance(project)
        val showSelectiveImportDialog = settings.showSelectiveImportDialogOnInitialImport()
        val application = ApplicationManager.getApplication()
        if (showSelectiveImportDialog && !application.isHeadlessEnvironment) {
            application.invokeAndWait {
                val projectInfo = InternalExternalProjectInfo(SYSTEM_ID, externalProjectPath, externalProject)
                val dialog = ExternalProjectDataSelectorDialog(project, projectInfo)
                if (dialog.hasMultipleDataToSelect()) {
                    dialog.showAndGet()
                }
                else {
                    Disposer.dispose(dialog.disposable)
                }
            }
        }
    }

    private fun importData(project: Project, externalProject: DataNode<ProjectData>) {
        ProjectDataManager.getInstance().importData(externalProject, project)
    }
}
