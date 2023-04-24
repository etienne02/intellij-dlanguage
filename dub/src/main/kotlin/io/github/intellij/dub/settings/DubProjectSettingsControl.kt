package io.github.intellij.dub.settings

import com.intellij.openapi.externalSystem.service.settings.AbstractExternalProjectSettingsControl
import com.intellij.openapi.externalSystem.util.PaintAwarePanel

class DubProjectSettingsControl(initialSettings: DubProjectSettings) : AbstractExternalProjectSettingsControl<DubProjectSettings>(initialSettings) {
    override fun validate(settings: DubProjectSettings): Boolean {
        TODO("Not yet implemented")
    }

    override fun fillExtraControls(content: PaintAwarePanel, indentLevel: Int) {
        TODO("Not yet implemented")
    }

    override fun isExtraSettingModified(): Boolean {
        TODO("Not yet implemented")
    }

    override fun resetExtraSettings(isDefaultModuleCreation: Boolean) {
        TODO("Not yet implemented")
    }

    override fun applyExtraSettings(settings: DubProjectSettings) {
        TODO("Not yet implemented")
    }
}
