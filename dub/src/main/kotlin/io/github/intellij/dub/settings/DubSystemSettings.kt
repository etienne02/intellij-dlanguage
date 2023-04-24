package io.github.intellij.dub.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.RoamingType
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "DubSystemSettings", storages = [Storage(value = "dub.settings.xml", roamingType = RoamingType.DISABLED)])
class DubSystemSettings : PersistentStateComponent<DubSystemSettings.MyState> {
    override fun getState(): MyState {
        return MyState()
    }

    override fun loadState(state: MyState) {
    }

    class MyState {}

    companion object {
        fun getInstance(): DubSystemSettings = ApplicationManager.getApplication().getService(DubSystemSettings::class.java)
    }
}
