package io.github.intellij.dlanguage.project.wizard.generators

import com.intellij.ide.projectWizard.NewProjectWizardCollector.BuildSystem.logBuildSystemChanged
import com.intellij.ide.wizard.*
import com.intellij.ide.wizard.LanguageNewProjectWizardData.Companion.languageData
import com.intellij.ide.wizard.language.LanguageGeneratorNewProjectWizard
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.Row
import com.intellij.ui.dsl.builder.SegmentedButton
import io.github.intellij.dlanguage.DLanguage
import javax.swing.Icon


class DlangNewProjectWizard : LanguageGeneratorNewProjectWizard {

    override val icon: Icon = DLanguage.Icons.LANGUAGE

    override val name = "D"

    override val ordinal = 600

    override fun createStep(parent: NewProjectWizardStep): NewProjectWizardStep = NewDlangProjectStep(parent)

    class NewDlangProjectStep(parent: NewProjectWizardStep) :
        AbstractNewProjectWizardMultiStep<NewDlangProjectStep, BuildSystemDlangNewProjectWizard>(parent, BuildSystemDlangNewProjectWizard.EP_NAME),
        LanguageNewProjectWizardData by parent.languageData!!,
        BuildSystemDlangNewProjectWizardData {

        override val self = this

        override val label = "Build System:"

        override val buildSystemProperty by ::stepProperty

        override var buildSystem by ::step

        override fun createAndSetupSwitcher(builder: Row): SegmentedButton<String> {
            return super.createAndSetupSwitcher(builder)
                .whenItemSelectedFromUi { logBuildSystemChanged() }
        }

        override fun setupProject(project: Project) {
            super.setupProject(project)
            logBuildSystemChanged()
        }

        init {
            data.putUserData(BuildSystemDlangNewProjectWizardData.KEY, this)
        }

    }

}
