package io.github.intellij.dlanguage.project.wizard.generators

import com.intellij.ide.wizard.NewProjectWizardMultiStepFactory
import com.intellij.openapi.extensions.ExtensionPointName

/**
 * Describes dlang build system step in new project wizard.
 *
 * @see NewProjectWizardMultiStepFactory
 */
interface BuildSystemDlangNewProjectWizard :
    NewProjectWizardMultiStepFactory<DlangNewProjectWizard.NewDlangProjectStep> {
    companion object {
        var EP_NAME = ExtensionPointName<BuildSystemDlangNewProjectWizard>("io.github.intellij.dlanguage.projectWizard.dlang.buildSystem")
    }
}

