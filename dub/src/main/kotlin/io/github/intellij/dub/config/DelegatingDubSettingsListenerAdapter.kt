package io.github.intellij.dub.config

import com.intellij.openapi.externalSystem.settings.DelegatingExternalSystemSettingsListener
import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListener
import io.github.intellij.dub.settings.DubProjectSettings
import io.github.intellij.dub.settings.DubSettingsListener

class DelegatingDubSettingsListenerAdapter(delegate: ExternalSystemSettingsListener<DubProjectSettings>) : DelegatingExternalSystemSettingsListener<DubProjectSettings>(delegate),
    DubSettingsListener {

}
