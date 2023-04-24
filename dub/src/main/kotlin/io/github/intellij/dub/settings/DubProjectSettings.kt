package io.github.intellij.dub.settings

import com.intellij.openapi.externalSystem.settings.ExternalProjectSettings

class DubProjectSettings() : ExternalProjectSettings() {

    constructor(externalProjectPath: String) : this() {
        setExternalProjectPath(externalProjectPath)
    }
    override fun clone(): ExternalProjectSettings {
        val result = DubProjectSettings()

        copyTo(result)

        return result
    }
}
