package io.github.intellij.dub.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemLocalSettings
import com.intellij.openapi.project.Project
import io.github.intellij.dub.Dub

@State(name = "DubLocalSettings", storages = [Storage(StoragePathMacros.CACHE_FILE)])
class DubLocalSettings(project: Project) : AbstractExternalSystemLocalSettings<DubLocalSettings.MyState>(Dub.SYSTEM_ID, project, MyState()), PersistentStateComponent<DubLocalSettings.MyState> {

    override fun loadState(state: MyState) {
        super.loadState(state)
    }

    class MyState : State() {

    }

    companion object {
        fun getInstance(project: Project): DubLocalSettings {
            return project.getService(DubLocalSettings::class.java)
        }
    }
}
