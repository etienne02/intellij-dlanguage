package io.github.intellij.dub.project

import com.intellij.openapi.module.ModifiableModuleModel
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.packaging.artifacts.ModifiableArtifactModel
import com.intellij.projectImport.DeprecatedProjectBuilderForImport
import com.intellij.projectImport.ProjectImportBuilder
import com.intellij.projectImport.ProjectOpenProcessor
import io.github.intellij.dlanguage.DLanguage
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
 * Use [io.github.intellij.dub.project.linkAndRefreshDubProject] to attach a dub project to an opened idea project.
 */
class DubProjectImportBuilder : ProjectImportBuilder<DubPackage>(), DeprecatedProjectBuilderForImport {
    private var parameters: Parameters? = null
    fun getParameters(): Parameters {
        if (parameters == null) {
            parameters = Parameters()
        }
        return parameters!!
    }

    override fun getName(): String {
        return DubProjectOpenProcessor.NAME
    }

    override fun getIcon(): Icon {
        return DLanguage.Icons.MODULE
    }

    override fun getList(): List<DubPackage>? {
        return getParameters().packages
    }

    override fun setList(list: List<DubPackage>) {
        getParameters().packages = list
    }

    override fun isOpenProjectSettingsAfter(): Boolean {
        return getParameters().openModuleSettings
    }

    override fun setOpenProjectSettingsAfter(on: Boolean) {
        getParameters().openModuleSettings = on
    }

    override fun isMarked(dubPackage: DubPackage): Boolean {
        return list!!.contains(dubPackage)
    }

    override fun commit(
        project: Project,
        modifiableModuleModel: ModifiableModuleModel?,
        modulesProvider: ModulesProvider?,
        modifiableArtifactModel: ModifiableArtifactModel?
    ): List<Module> {
        linkAndRefreshDubProject(fileToImport, project)
        return emptyList()
    }

    class Parameters {
        var packages: List<DubPackage>? = null
        var openModuleSettings = false
        @JvmField
        var dubBinary: String? = null
    }

    override fun getProjectOpenProcessor(): ProjectOpenProcessor =
        ProjectOpenProcessor.EXTENSION_POINT_NAME.findExtensionOrFail(DubProjectOpenProcessor::class.java)
}
