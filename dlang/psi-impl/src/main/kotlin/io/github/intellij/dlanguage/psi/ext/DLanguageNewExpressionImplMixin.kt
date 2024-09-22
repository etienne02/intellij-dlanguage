package io.github.intellij.dlanguage.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import io.github.intellij.dlanguage.psi.DLanguageNewExpression
import io.github.intellij.dlanguage.psi.types.DType

abstract class DLanguageNewExpressionImplMixin(node: ASTNode) : ASTWrapperPsiElement(node),
    DLanguageNewExpression {

    override fun getDType(): DType? {
        return type?.dType
    }
}
