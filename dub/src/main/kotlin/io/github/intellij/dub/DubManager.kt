package io.github.intellij.dub

import com.intellij.execution.configurations.SimpleJavaParameters
import com.intellij.openapi.externalSystem.ExternalSystemAutoImportAware
import com.intellij.openapi.externalSystem.ExternalSystemConfigurableAware
import com.intellij.openapi.externalSystem.ExternalSystemManager
import com.intellij.openapi.externalSystem.ExternalSystemUiAware
import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.model.project.ContentRootData
import com.intellij.openapi.externalSystem.model.project.ModuleData
import com.intellij.openapi.externalSystem.service.project.ExternalSystemProjectResolver
import com.intellij.openapi.externalSystem.service.project.autoimport.CachingExternalSystemAutoImportAware
import com.intellij.openapi.externalSystem.task.ExternalSystemTaskManager
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.Pair
import com.intellij.util.Function
import io.github.intellij.dub.project.DubAutoImportAware
import io.github.intellij.dub.project.DubConfigurationParserNew
import io.github.intellij.dub.project.DubProjectResolver
import io.github.intellij.dub.settings.*
import io.github.intellij.dub.task.DubTaskManager
import java.io.File
import javax.swing.Icon

class DubManager : ExternalSystemConfigurableAware, ExternalSystemUiAware,
    ExternalSystemAutoImportAware, ProjectActivity, ExternalSystemManager<
        DubProjectSettings,
        DubSettingsListener,
        DubSettings,
        DubLocalSettings,
        DubExecutionSettings> {

    private val myAutoImportDelegate = CachingExternalSystemAutoImportAware(DubAutoImportAware())

    override fun getConfigurable(project: Project): Configurable = DubConfigurable(project)

    override fun getProjectRepresentationName(targetProjectPath: String, rootProjectPath: String?): String =
        ExternalSystemApiUtil.getProjectRepresentationName(targetProjectPath, rootProjectPath)

    override fun getExternalProjectConfigDescriptor(): FileChooserDescriptor? =
        FileChooserDescriptorFactory.createSingleFolderDescriptor()

    override fun getProjectIcon(): Icon = DubIcons.BALL // TODO

    override fun getTaskIcon(): Icon = DubIcons.BALL // TODO

    override fun getAffectedExternalProjectPath(changedFileOrDirPath: String, project: Project): String? =
        myAutoImportDelegate.getAffectedExternalProjectPath(changedFileOrDirPath, project)

    override fun getAffectedExternalProjectFiles(projectPath: String?, project: Project): MutableList<File> =
        myAutoImportDelegate.getAffectedExternalProjectFiles(projectPath, project)

    override suspend fun execute(project: Project) {
        // TODO We want to automatically refresh linked project on dub settings change
    }

    override fun enhanceRemoteProcessing(parameters: SimpleJavaParameters) {
    }

    override fun getSystemId(): ProjectSystemId = Dub.SYSTEM_ID

    override fun getSettingsProvider(): Function<Project, DubSettings> = Function{
        project -> DubSettings.getInstance(project)
    }

    override fun getLocalSettingsProvider(): Function<Project, DubLocalSettings> = Function{
        project -> DubLocalSettings.getInstance(project)
    }

    override fun getExecutionSettingsProvider(): Function<Pair<Project, String>, DubExecutionSettings> = Function{
        pair ->
        val project = pair.first
        val projectPath = pair.second
        val settings = DubSettings.getInstance(project)
        val projectLevelSettings: DubProjectSettings? = settings.getLinkedProjectSettings(projectPath)
        val rootProjectPath = projectLevelSettings?.externalProjectPath?: projectPath
        val result = DubExecutionSettings()
        result.dubBinaryPath = settings.binaryPath

        val dubBinaryPath = "/usr/bin/dub"
        val parser = DubConfigurationParserNew(projectPath, dubBinaryPath)

        val dubProject = parser.dubProject.get()
        val x = dubProject.configuration
        result
    }

    override fun getProjectResolverClass(): Class<out ExternalSystemProjectResolver<DubExecutionSettings>> = DubProjectResolver::class.java

    override fun getTaskManagerClass(): Class<out ExternalSystemTaskManager<DubExecutionSettings>> = DubTaskManager::class.java

    // TODO should only allow "dub.sdl" and "dub.json" files
    override fun getExternalProjectDescriptor(): FileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
}
