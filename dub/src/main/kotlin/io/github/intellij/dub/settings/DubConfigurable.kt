package io.github.intellij.dub.settings

import com.intellij.openapi.externalSystem.service.settings.AbstractExternalSystemConfigurable
import com.intellij.openapi.externalSystem.util.ExternalSystemSettingsControl
import com.intellij.openapi.project.Project
import io.github.intellij.dub.Dub

class DubConfigurable(project: Project) : AbstractExternalSystemConfigurable<DubProjectSettings, DubSettingsListener, DubSettings>(project, Dub.SYSTEM_ID) {
    override fun getId(): String = "reference.settings-dialog.project.dub"

    override fun newProjectSettings(): DubProjectSettings = DubProjectSettings()

    override fun createSystemSettingsControl(settings: DubSettings): ExternalSystemSettingsControl<DubSettings> =
        DubSystemSettingsControl(settings)

    override fun createProjectSettingsControl(settings: DubProjectSettings): ExternalSystemSettingsControl<DubProjectSettings> =
        DubProjectSettingsControl(settings)
}
