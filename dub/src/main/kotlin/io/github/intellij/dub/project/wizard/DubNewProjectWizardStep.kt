package io.github.intellij.dub.project.wizard

import com.intellij.ide.wizard.AbstractNewProjectWizardStep
import com.intellij.ide.wizard.NewProjectWizardBaseData
import com.intellij.ide.wizard.NewProjectWizardStep
import com.intellij.openapi.observable.util.bindEnumStorage
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import io.github.intellij.dub.module.DlangDubModuleBuilder
import org.jetbrains.annotations.Nls

abstract class DubNewProjectWizardStep<ParentStep>(
    protected val parentStep: ParentStep
) : AbstractNewProjectWizardStep(parentStep),
    DubNewProjectWizardData
    where ParentStep : NewProjectWizardStep,
          ParentStep : NewProjectWizardBaseData {

    final override val sdkProperty = propertyGraph.property<Sdk?>(null)
    final override val dubDslProperty = propertyGraph.property(DubDsl.JSON)
        .bindEnumStorage("NewProjectWizard.dubDslState")
    final override val dubProjectTypeProperty = propertyGraph.property(DubProjectType.MINIMAL)
        .bindEnumStorage("NewProjectWizard.dubProjectTypeState")

    final override var sdk by sdkProperty
    final override var dubProjectType by dubProjectTypeProperty
    final override var dubDsl by dubDslProperty

    /*protected fun setupDubDistributionUI(builder: Panel) {
        builder.panel {
            row {
                label("Dub location:")
                val fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
                    .withPathToTextConvertor(::getPresentablePath)
                    .withPathToTextConvertor(::getCanonicalPath)
                val title = "Choose Dub Installation"
                textFieldWithBrowseButton(title, context.project, fileChooserDescriptor)
                    .applyToComponent { setEmptyState("Path to Dub") }
                    //.bindText(dubHomeProperty.toUiPathProperty())
                    .trimmedTextValidation(CHECK_NON_EMPTY, CHECK_DIRECTORY)
                    //.validationOnInput { validateDubHome() }
                    //.validationOnApply { validateDubHome() }
                    .align(AlignX.FILL)
            }
        }
    }*/

    /*private fun ValidationInfoBuilder.validateDubVersion(rawDubVersion: String): ValidationInfo? {
        val dubVersion = try {
            DubVersion.version(rawDubVersion)
        }
        catch (ex: IllegalArgumentException) {
            return error(ex.localizedMessage)
        }
        return null
    }

    private fun ValidationInfoBuilder.validateDubHome(): ValidationInfo? {
        val installationManager = DubInstallationManager.getInstance()
        if (!installationManager.isDubHome(context.project, dubHome)) {
            return error("Invalid Dub installation")
        }
        val dubVersion = DubInstallationManager.getDubVersion(dubHome)
        if (dubVersion == null) {
            return error("Cannot resolve Dub version")
        }
        return validateDubVersion(dubVersion)
    }*/

    protected fun linkDubProject(
        project: Project,
        builder: DlangDubModuleBuilder = DlangDubModuleBuilder(),
    ): Module? {
        builder.moduleJdk = sdk
        builder.name = parentStep.name
        builder.contentEntryPath = "${parentStep.path}/${parentStep.name}"
        val options: MutableMap<String, String> = HashMap(3)
        options["dubFormat"] = dubDsl.dubOptionValue
        options["dubType"] = dubProjectType.text
        builder.setDubOptions(options)

        // TODO take from config
        builder.setDubBinary("/usr/bin/dub")

        val model = context.getUserData(NewProjectWizardStep.MODIFIABLE_MODULE_MODEL_KEY)
        return builder.commit(project, model)?.firstOrNull()
    }

    enum class DubDsl(val text: @Nls String, val dubOptionValue: @Nls String) {
        JSON("JSON", "json"),
        SDLANG("SDlang", "sdl")
    }

    enum class DubProjectType(val text: @Nls String) {
        MINIMAL("minimal"),
        VIBE("vibe.d"),
        DEIMOS("deimos")
    }
}
