package io.github.intellij.dub.project

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.projectImport.ProjectOpenProcessor
import io.github.intellij.dlanguage.DLanguage
import javax.swing.Icon

/**
 * Used when opening a dub project within the IDE.
 */
class DubProjectOpenProcessor : ProjectOpenProcessor() {
    override val name: String
        get() = NAME

    override val icon: Icon
        get() = DLanguage.Icons.FILE

    override fun canOpenProject(file: VirtualFile): Boolean = canOpenDubProject(file)

    override suspend fun openProjectAsync(
        virtualFile: VirtualFile,
        projectToClose: Project?,
        forceOpenInNewFrame: Boolean
    ): Project? {
        return openDubProject(virtualFile, projectToClose, forceOpenInNewFrame)
    }

    override fun canImportProjectAfterwards(): Boolean = true

    override suspend fun importProjectAfterwardsAsync(project: Project, file: VirtualFile) {
        linkAndSyncDubProject(project, file.path)
    }

    companion object {
        const val NAME = "Dub"
    }
}
