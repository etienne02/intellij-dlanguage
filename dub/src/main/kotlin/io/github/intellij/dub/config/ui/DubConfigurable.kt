package io.github.intellij.dub.config.ui

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class DubConfigurable : SearchableConfigurable {

    /**
     * Ensures that the UI component for selecting a Dub Tool can only be used to select the correct binary
     */
    private class DubToolBinaryChooserDescriptor(binaryName: String) :
        FileChooserDescriptor(true, false, false, false, false, false) {
        init {
            withFileFilter { vf: VirtualFile ->
                vf.nameWithoutExtension.equals(binaryName, ignoreCase = true)
            }
        }
    }

    override fun createComponent(): JComponent {
        return panel {
            twoColumnsRow(
                {
                    label("Label")
                },
                {
                    textFieldWithBrowseButton(project = null, browseDialogTitle = "test", fileChooserDescriptor = DubToolBinaryChooserDescriptor("dub"))
                        .align(AlignX.FILL)
                })
            twoColumnsRow(
                null,
                {
                    text("spinner")
                })
        }
    }

    override fun isModified(): Boolean {
        return true
    }

    override fun apply() {
    }

    override fun getDisplayName(): String {
        return "Test Dub Panel"
    }

    override fun getId(): String {
        return "D config id"
    }

}
