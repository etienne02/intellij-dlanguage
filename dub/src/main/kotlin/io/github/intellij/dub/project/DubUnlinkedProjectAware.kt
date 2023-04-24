package io.github.intellij.dub.project

import com.intellij.openapi.Disposable
import com.intellij.openapi.externalSystem.autolink.ExternalSystemProjectLinkListener
import com.intellij.openapi.externalSystem.autolink.ExternalSystemUnlinkedProjectAware
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import io.github.intellij.dub.Dub
import io.github.intellij.dub.config.DubSettingsListenerAdapter
import io.github.intellij.dub.settings.DubProjectSettings
import io.github.intellij.dub.settings.DubSettings

class DubUnlinkedProjectAware : ExternalSystemUnlinkedProjectAware {
    override val systemId: ProjectSystemId = Dub.SYSTEM_ID
    override fun isBuildFile(project: Project, buildFile: VirtualFile): Boolean {
        return isDubProjectFile(buildFile)
    }

    override fun isLinkedProject(project: Project, externalProjectPath: String): Boolean {
        val dubSettings = DubSettings.getInstance(project)
        val projectSettings = dubSettings.getLinkedProjectSettings(externalProjectPath)
        return projectSettings != null
    }

    override fun linkAndLoadProject(project: Project, externalProjectPath: String) {
        linkAndRefreshDubProject(externalProjectPath, project)
    }

    override fun subscribe(
        project: Project,
        listener: ExternalSystemProjectLinkListener,
        parentDisposable: Disposable
    ) {
        val dubSettings = DubSettings.getInstance(project)
        dubSettings.subscribe(object : DubSettingsListenerAdapter() {
            override fun onProjectsLinked(settings: Collection<DubProjectSettings>) =
                settings.forEach { listener.onProjectLinked(it.externalProjectPath)}

            override fun onProjectsUnlinked(linkedProjectPaths: Set<String>) =
                linkedProjectPaths.forEach { listener.onProjectUnlinked(it)}
        }, parentDisposable)
    }


}
