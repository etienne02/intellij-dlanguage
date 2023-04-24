package io.github.intellij.dub.config

import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListener
import io.github.intellij.dub.settings.DubProjectSettings
import io.github.intellij.dub.settings.DubSettingsListener

abstract class DubSettingsListenerAdapter : ExternalSystemSettingsListener<DubProjectSettings>,
    DubSettingsListener {

}
