package io.github.intellij.dub.project

import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.externalSystem.util.ExternalSystemBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.ui.getPresentablePath
import com.intellij.openapi.util.io.toCanonicalPath
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import io.github.intellij.dub.Dub
import io.github.intellij.dub.settings.DubProjectSettings
import io.github.intellij.dub.settings.DubSettings
import java.nio.file.Path

fun isDubProjectFile(file: VirtualFile): Boolean = !file.isDirectory && ("dub.json".equals(file.name, ignoreCase = true) || "dub.sdl".equals(file.name, ignoreCase = true))

fun canOpenDubProject(file: VirtualFile): Boolean = DubOpenProjectProvider().canOpenProject(file)

suspend fun openDubProject(virtualFile: VirtualFile, projectToClose: Project?, forceOpenInNewFrame: Boolean): Project? {
    return DubOpenProjectProvider().openProject(virtualFile, projectToClose, forceOpenInNewFrame)
}

fun linkAndRefreshDubProject(projectFilePath: String, project: Project) {
    DubOpenProjectProvider().linkToExistingProject(projectFilePath, project)
}

fun canLinkAndRefreshDubProject(projectFilePath: String, project: Project, showValidationDialog: Boolean = true): Boolean {
    val validationInfo = validateDubProject(projectFilePath, project) ?: return true
    if (showValidationDialog) {
        val title = "Project Reload Failed"
        invokeAndWaitIfNeeded {
            when (validationInfo.warning) {
                true -> Messages.showWarningDialog(project, validationInfo.message, title)
                else -> Messages.showErrorDialog(project, validationInfo.message, title)
            }
        }
    }
    return false
}

fun createLinkSettings(projectDirectory: Path, project: Project): DubProjectSettings {
    val dubProjectSettings = DubProjectSettings(projectDirectory.toCanonicalPath())
    return dubProjectSettings
}

private fun validateDubProject(projectFilePath: String, project: Project): ValidationInfo? {
    val system = ExternalSystemApiUtil.getSettings(project, Dub.SYSTEM_ID)
    val localFileSystem = LocalFileSystem.getInstance()
    val projectFile = localFileSystem.refreshAndFindFileByPath(projectFilePath)
    if (projectFile == null) {
        val shortPath = getPresentablePath(projectFilePath)
        return ValidationInfo(ExternalSystemBundle.message("error.project.does.not.exist", "Dub", shortPath))
    }
    val projectDirectory = if (projectFile.isDirectory) projectFile else projectFile.parent
    val projectSettings = system.getLinkedProjectSettings(projectDirectory.path)
    if (projectSettings != null) return ValidationInfo(ExternalSystemBundle.message("error.project.already.registered"))
    return null
}
