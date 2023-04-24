package io.github.intellij.dub.module

import com.intellij.ide.starters.StarterModuleImporter
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import io.github.intellij.dub.project.canLinkAndRefreshDubProject
import io.github.intellij.dub.project.linkAndRefreshDubProject
import java.io.File

class DubStarterModuleImporter : StarterModuleImporter {
    override val id: String = "dub"
    override val title: String = "dub"

    override fun runAfterSetup(module: Module): Boolean {
        val project = module.project
        val dubFile = findDubFile(module) ?: return true

        val rootDirectory = dubFile.parent

        if (!canLinkAndRefreshDubProject(rootDirectory, project))
            return false
        linkAndRefreshDubProject(rootDirectory, project)
        return false
    }

    private fun findDubFile(module: Module): File? {
        for (contentRoot in ModuleRootManager.getInstance(module).contentRoots) {
            val baseDir = VfsUtilCore.virtualToIoFile(contentRoot)
            var file = File(baseDir, "dub.sdl")
            if (file.exists()) return file
            file = File(baseDir, "dub.json")
            if (file.exists()) return file
        }
        return null
    }
}
