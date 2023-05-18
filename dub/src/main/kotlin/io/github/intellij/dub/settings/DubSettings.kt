package io.github.intellij.dub.settings

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemSettings
import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListener
import com.intellij.openapi.project.Project
import io.github.intellij.dub.config.DelegatingDubSettingsListenerAdapter
import java.util.TreeSet

@State(name = "DubSettings", storages = [Storage("dub.xml")])
class DubSettings(project: Project) : AbstractExternalSystemSettings<DubSettings, DubProjectSettings, DubSettingsListener>(DubSettingsListener.TOPIC, project),
    PersistentStateComponent<DubSettings.MyState> {

    var offlineMode = false

    var binaryPath: String? = null

    override fun subscribe(listener: ExternalSystemSettingsListener<DubProjectSettings>, parentDisposable: Disposable) {
        doSubscribe(DelegatingDubSettingsListenerAdapter(listener), parentDisposable)
    }

    override fun copyExtraSettingsFrom(settings: DubSettings) {
    }

    override fun checkSettings(old: DubProjectSettings, current: DubProjectSettings) {
    }

    override fun loadState(state: MyState) {
        super.loadState(state)

        offlineMode = state.isOfflineMode
    }

    override fun getState(): MyState {
        val state = MyState()
        fillState(state)

        state.isOfflineMode = offlineMode

        return state
    }



    class MyState : State<DubProjectSettings> {

        private var myProjectSettings: MutableSet<DubProjectSettings> = TreeSet()

        var isOfflineMode: Boolean = false
        override fun getLinkedExternalProjectsSettings(): MutableSet<DubProjectSettings> = myProjectSettings

        override fun setLinkedExternalProjectsSettings(settings: MutableSet<DubProjectSettings>?) {
            if (settings != null)
                myProjectSettings.addAll(settings)
        }
    }

    companion object {
        fun getInstance(project: Project): DubSettings = project.getService(DubSettings::class.java)
    }
}
