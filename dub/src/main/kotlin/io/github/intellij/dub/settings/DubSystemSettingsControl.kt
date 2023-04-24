package io.github.intellij.dub.settings

import com.intellij.openapi.externalSystem.util.ExternalSystemSettingsControl
import com.intellij.openapi.externalSystem.util.ExternalSystemUiUtil
import com.intellij.openapi.externalSystem.util.PaintAwarePanel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.UIUtil
import com.intellij.xml.util.XmlStringUtil

class DubSystemSettingsControl(val settings: DubSettings) : ExternalSystemSettingsControl<DubSettings> {

    private lateinit var myOfflineMode: JBCheckBox

    private lateinit var myOfflineModeComment: JBLabel
    override fun fillUi(canvas: PaintAwarePanel, indentLevel: Int) {
        myOfflineMode = JBCheckBox("Offline mode")
        canvas.add(myOfflineMode, ExternalSystemUiUtil.getFillLineConstraints(indentLevel))

        myOfflineModeComment = JBLabel(
            XmlStringUtil.wrapInHtml("Pass the <b>--offline</b> option to dub to avoid network requests"),
            UIUtil.ComponentStyle.SMALL)
        myOfflineModeComment.foreground = UIUtil.getLabelFontColor(UIUtil.FontColor.BRIGHTER)

        val constraints = ExternalSystemUiUtil.getFillLineConstraints(indentLevel)
        constraints.insets.left += UIUtil.getCheckBoxTextHorizontalOffset(myOfflineMode)
        constraints.insets.top = 0
        canvas.add(myOfflineModeComment, constraints)
    }

    override fun reset() {
        myOfflineMode.isSelected = settings.offlineMode
    }

    override fun isModified(): Boolean {
        return settings.offlineMode != myOfflineMode.isSelected
    }

    override fun disposeUIResources() {
        ExternalSystemUiUtil.disposeUi(this)
    }

    override fun showUi(show: Boolean) {
        ExternalSystemUiUtil.showUi(this, show)
    }

    override fun validate(settings: DubSettings): Boolean {
        return true
    }

    override fun apply(settings: DubSettings) {
        settings.offlineMode = myOfflineMode.isSelected
    }
}
