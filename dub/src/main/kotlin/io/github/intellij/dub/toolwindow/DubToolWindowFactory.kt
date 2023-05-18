package io.github.intellij.dub.toolwindow

import com.intellij.openapi.externalSystem.service.task.ui.AbstractExternalSystemToolWindowFactory
import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemSettings
import com.intellij.openapi.project.Project
import io.github.intellij.dub.Dub
import io.github.intellij.dub.settings.DubSettings

class DubToolWindowFactory : AbstractExternalSystemToolWindowFactory(Dub.SYSTEM_ID) {
    override fun getSettings(project: Project): AbstractExternalSystemSettings<*, *, *> =
        DubSettings.getInstance(project)
}
