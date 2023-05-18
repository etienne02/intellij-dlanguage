package io.github.intellij.dub.settings

import com.intellij.openapi.externalSystem.model.settings.ExternalSystemExecutionSettings

class DubExecutionSettings : ExternalSystemExecutionSettings() {

    var ideProjectPath: String? = null

    var dubBinaryPath: String? = null
}
