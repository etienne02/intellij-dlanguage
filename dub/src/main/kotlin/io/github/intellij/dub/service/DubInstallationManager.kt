package io.github.intellij.dub.service

import com.intellij.openapi.components.*

@State(name = "DubExecutable",
    category = SettingsCategory.TOOLS,
    reportStatistic = false,
    storages = [Storage(value = "dubExecutable.xml", roamingType = RoamingType.PER_OS)]
)
class DubInstallationManager : PersistentStateComponent<DubInstallationManager.MyState> {

    private val state = MyState()

    override fun getState(): MyState {
        return state
    }

    fun getPath() {

    }

    fun getVersion() {

    }

    override fun loadState(newState: MyState) {
        state.binPath = newState.binPath
    }

    class MyState(var binPath: String? = null) {
    }
}
