package io.github.intellij.dub.settings

import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.service.settings.ExternalSystemConfigLocator
import com.intellij.openapi.externalSystem.settings.ExternalProjectSettings
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import io.github.intellij.dub.Dub
import io.github.intellij.dub.project.canOpenDubProject
import io.github.intellij.dub.project.isDubProjectFile
import java.io.File

class DubConfigLocator : ExternalSystemConfigLocator {
    override fun getTargetExternalSystemId(): ProjectSystemId = Dub.SYSTEM_ID

    override fun adjust(configPath: VirtualFile): VirtualFile? {
        if (!configPath.isDirectory)
            return configPath

        var result = configPath.findChild("dub.sdl")
        if (result != null)
            return result
        result = configPath.findChild("dub.json")
        if (result != null)
            return result

        return null
    }

    override fun findAll(externalProjectSettings: ExternalProjectSettings): MutableList<VirtualFile> {
        val list: MutableList<VirtualFile> = mutableListOf()
        for (path: String in externalProjectSettings.modules) {
            val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(File(path))
            virtualFile?:continue
            for (child in virtualFile.children) {
                if (isDubProjectFile(child)) {
                    list.add(child)
                }
            }
        }
        return list
    }

}
