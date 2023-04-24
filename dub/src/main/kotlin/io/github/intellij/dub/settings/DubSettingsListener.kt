package io.github.intellij.dub.settings

import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListener
import com.intellij.util.messages.Topic

interface DubSettingsListener : ExternalSystemSettingsListener<DubProjectSettings> {
    companion object {
        val TOPIC: Topic<DubSettingsListener> = Topic(DubSettingsListener::class.java, Topic.BroadcastDirection.NONE)
    }
}
