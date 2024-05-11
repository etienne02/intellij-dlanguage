package io.github.intellij.dlanguage.project.wizard.generators

import com.intellij.ide.highlighter.ModuleFileType
import com.intellij.ide.projectWizard.NewProjectWizardCollector.BuildSystem.logSdkChanged
import com.intellij.ide.projectWizard.NewProjectWizardCollector.BuildSystem.logSdkFinished
import com.intellij.ide.projectWizard.NewProjectWizardConstants.BuildSystem.INTELLIJ
import com.intellij.ide.projectWizard.generators.IntelliJNewProjectWizardStep
import com.intellij.ide.wizard.NewProjectWizardStep
import com.intellij.ide.wizard.NewProjectWizardStep.Companion.MODIFIABLE_MODULE_MODEL_KEY
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.SdkTypeId
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.roots.ui.configuration.sdkComboBox
import com.intellij.openapi.util.io.FileUtil
import com.intellij.ui.dsl.builder.*
import io.github.intellij.dlanguage.DLanguage
import io.github.intellij.dlanguage.DlangSdkType
import io.github.intellij.dlanguage.module.DlangModuleBuilder
import java.nio.file.Paths


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

        override fun setupProject(project: Project) {
            val builder = DlangModuleBuilder()
            val moduleFile = Paths.get(moduleFileLocation, moduleName + ModuleFileType.DOT_DEFAULT_EXTENSION)

            builder.name = moduleName
            builder.moduleFilePath = FileUtil.toSystemDependentName(moduleFile.toString())
            builder.contentEntryPath = FileUtil.toSystemDependentName(contentRoot)

            if (context.isCreatingNewProject) {
                // New project with a single module: set project JDK
                context.projectJdk = sdk
            }
            else {
                // New module in an existing project: set module JDK
                val isSameSdk = ProjectRootManager.getInstance(project).projectSdk?.name == sdk?.name
                builder.moduleJdk = if (isSameSdk) null else sdk
            }

            val model = context.getUserData(MODIFIABLE_MODULE_MODEL_KEY)
            builder.commit(project, model)?.singleOrNull()
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
