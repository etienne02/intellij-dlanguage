package io.github.intellij.dlanguage.project.wizard.generators

import com.intellij.ide.projectWizard.NewProjectWizardCollector.BuildSystem.logSdkChanged
import com.intellij.ide.projectWizard.NewProjectWizardCollector.BuildSystem.logSdkFinished
import com.intellij.ide.projectWizard.NewProjectWizardConstants.BuildSystem.INTELLIJ
import com.intellij.ide.projectWizard.generators.IntelliJNewProjectWizardStep
import com.intellij.ide.wizard.NewProjectWizardStep
import com.intellij.openapi.projectRoots.SdkTypeId
import com.intellij.openapi.roots.ui.configuration.sdkComboBox
import com.intellij.ui.dsl.builder.*
import io.github.intellij.dlanguage.DLanguage
import io.github.intellij.dlanguage.DlangSdkType


class IntellijDlangNewProjectWizard : BuildSystemDlangNewProjectWizard {

    override val name = INTELLIJ

    override val ordinal = 0

    override fun createStep(parent: DlangNewProjectWizard.NewDlangProjectStep): NewProjectWizardStep =
        DlangStep(parent)

    class DlangStep(parent: DlangNewProjectWizard.NewDlangProjectStep) :
        IntelliJNewProjectWizardStep<DlangNewProjectWizard.NewDlangProjectStep>(parent),
        BuildSystemDlangNewProjectWizardData by parent,
        IntelliJDlangNewProjectWizardData {

        override fun setupSettingsUI(builder: Panel) {
            setupDlangSdkUI(builder)
        }

        override fun setupAdvancedSettingsUI(builder: Panel) {
            setupModuleNameUI(builder)
            setupModuleContentRootUI(builder)
            setupModuleFileLocationUI(builder)
        }

        init {
            data.putUserData(IntelliJDlangNewProjectWizardData.KEY, this)
        }

        protected fun setupDlangSdkUI(builder: Panel) {
            builder.row("D SDK") {
                val sdkTypeFilter = { it: SdkTypeId -> it is DlangSdkType }
                sdkComboBox(context, sdkProperty, DLanguage.MODULE_TYPE_ID, sdkTypeFilter)
                    .columns(COLUMNS_MEDIUM)
                    .whenItemSelectedFromUi { logSdkChanged(sdk) }
                    .onApply { logSdkFinished(sdk) }
            }.bottomGap(BottomGap.SMALL)
        }

    }
}
