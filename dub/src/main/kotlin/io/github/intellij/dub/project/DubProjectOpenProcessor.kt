package io.github.intellij.dub.project

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.projectImport.ProjectOpenProcessorBase
import io.github.intellij.dlanguage.DLanguage
import kotlinx.coroutines.runBlocking
import javax.swing.Icon

/**
 * Used when opening a dub project within the IDE.
 */
class DubProjectOpenProcessor : ProjectOpenProcessorBase<DubProjectImportBuilder>() {
    override val name: String
        get() = NAME

    override val icon: Icon
        get() = DLanguage.Icons.FILE

    // from ProjectOpenProcessorBase<DubProjectImportBuilder>
    override val supportedExtensions: Array<String>
        get() = arrayOf("json", "sdl")

    override fun canOpenProject(file: VirtualFile): Boolean = canOpenDubProject(file)

    override fun doOpenProject(
        virtualFile: VirtualFile,
        projectToClose: Project?,
        forceOpenInNewFrame: Boolean
    ): Project? {
       return runBlocking { openDubProject(virtualFile, projectToClose, forceOpenInNewFrame) }
    }

    override suspend fun openProjectAsync(
        virtualFile: VirtualFile,
        projectToClose: Project?,
        forceOpenInNewFrame: Boolean
    ): Project? {
        return openDubProject(virtualFile, projectToClose, forceOpenInNewFrame)
    }

    override fun canImportProjectAfterwards(): Boolean = true

    override fun importProjectAfterwards(project: Project, file: VirtualFile) {
        linkAndRefreshDubProject(file.path, project)
    }

    companion object {
        const val NAME = "Dub"
    }
}
