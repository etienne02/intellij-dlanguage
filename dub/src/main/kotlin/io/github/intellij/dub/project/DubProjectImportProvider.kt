package io.github.intellij.dub.project

import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.externalSystem.service.project.wizard.AbstractExternalProjectImportProvider
import com.intellij.openapi.vfs.VirtualFile
import io.github.intellij.dub.Dub

/**
 * IDEA only
 */
@Deprecated("Use the open and link project utility functions")
class DubProjectImportProvider : AbstractExternalProjectImportProvider(DubProjectImportBuilder(), Dub.SYSTEM_ID) {

    override fun createSteps(context: WizardContext): Array<ModuleWizardStep> {
        return ModuleWizardStep.EMPTY_ARRAY
    }

    override fun getPathToBeImported(file: VirtualFile): String = getDefaultPath(file)

    override fun canImportFromFile(file: VirtualFile): Boolean = canOpenDubProject(file)

    override fun getFileSample(): String = "<b>Dub</b> project file (dub.json, dub.sdl)"
}
