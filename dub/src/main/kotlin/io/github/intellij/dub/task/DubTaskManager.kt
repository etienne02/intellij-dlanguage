package io.github.intellij.dub.task

import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import com.intellij.openapi.externalSystem.task.ExternalSystemTaskManager
import io.github.intellij.dub.settings.DubExecutionSettings

class DubTaskManager : ExternalSystemTaskManager<DubExecutionSettings> {
    override fun cancelTask(id: ExternalSystemTaskId, listener: ExternalSystemTaskNotificationListener): Boolean {
        return true
    }
}
