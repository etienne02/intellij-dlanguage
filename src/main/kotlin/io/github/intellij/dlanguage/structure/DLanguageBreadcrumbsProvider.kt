package io.github.intellij.dlanguage.structure

import com.intellij.ide.ui.UISettings
import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider
import io.github.intellij.dlanguage.DLanguage

class DLanguageBreadcrumbsProvider : BreadcrumbsProvider {
    override fun isShownByDefault(): Boolean = !UISettings.getInstance().showMembersInNavigationBar

    override fun getLanguages(): Array<out Language> = arrayOf(DLanguage)

    override fun acceptElement(element: PsiElement): Boolean {
        // TODO
        return true
    }

    override fun getElementInfo(element: PsiElement): String {
        // TODO
        return "TODO"
    }
}
