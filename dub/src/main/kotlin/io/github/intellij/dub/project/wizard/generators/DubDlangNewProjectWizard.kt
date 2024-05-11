package io.github.intellij.dub.project.wizard.generators

import com.intellij.ide.projectWizard.NewProjectWizardCollector.BuildSystem.logSdkChanged
import com.intellij.ide.projectWizard.NewProjectWizardCollector.BuildSystem.logSdkFinished
import com.intellij.ide.wizard.NewProjectWizardStep
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.SdkTypeId
import com.intellij.openapi.roots.ui.configuration.sdkComboBox
import com.intellij.openapi.ui.BrowseFolderDescriptor.Companion.withPathToTextConvertor
import com.intellij.openapi.ui.getCanonicalPath
import com.intellij.openapi.ui.getPresentablePath
import com.intellij.openapi.ui.setEmptyState
import com.intellij.openapi.ui.validation.CHECK_DIRECTORY
import com.intellij.openapi.ui.validation.CHECK_NON_EMPTY
import com.intellij.ui.UIBundle
import com.intellij.ui.dsl.builder.*
import io.github.intellij.dlanguage.DLanguage
import io.github.intellij.dlanguage.DlangSdkType
import io.github.intellij.dlanguage.project.wizard.generators.BuildSystemDlangNewProjectWizard
import io.github.intellij.dlanguage.project.wizard.generators.BuildSystemDlangNewProjectWizardData
import io.github.intellij.dlanguage.project.wizard.generators.DlangNewProjectWizard
import io.github.intellij.dub.project.wizard.DubNewProjectWizardStep


internal class DubDlangNewProjectWizard : BuildSystemDlangNewProjectWizard {

    override val name = "Dub"

    override val ordinal = 200

    override fun createStep(parent: DlangNewProjectWizard.NewDlangProjectStep): NewProjectWizardStep =
        DubStep(parent)

    class DubStep(parent: DlangNewProjectWizard.NewDlangProjectStep) :
        DubNewProjectWizardStep<DlangNewProjectWizard.NewDlangProjectStep>(parent),
        BuildSystemDlangNewProjectWizardData by parent,
        DubDlangNewProjectWizardData {

        override fun setupUI(builder: Panel) {
            setupDlangSdkUI(builder)
            setupDubFileLang(builder)
            setupDubProjectType(builder)
            builder.collapsibleGroup(UIBundle.message("label.project.wizard.new.project.advanced.settings")) {
                setupAdvancedSettingsUI(this)
            }.topGap(TopGap.MEDIUM)
        }

        protected fun setupAdvancedSettingsUI(builder: Panel) {
            //setupDubDistributionUI(builder)
        }

        override fun setupProject(project: Project) {
            linkDubProject(project)
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

        protected fun setupDubFileLang(builder: Panel) {
            builder.row("Dub DSL:") {
                segmentedButton(DubDsl.entries) { text = it.text }
                    .bind(dubDslProperty)
            }.bottomGap(BottomGap.SMALL)
        }

        protected fun setupDubProjectType(builder: Panel) {
            builder.row("Project Type:") {
                segmentedButton(DubProjectType.entries) { text = it.text }
                    .bind(dubProjectTypeProperty)
            }.bottomGap(BottomGap.SMALL)
        }

        init {
            data.putUserData(DubDlangNewProjectWizardData.KEY, this)
        }
    }
}
