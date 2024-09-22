package io.github.intellij.dlanguage.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import io.github.intellij.dlanguage.psi.DLanguageReferenceExpression
import io.github.intellij.dlanguage.psi.interfaces.DTypedElement
import io.github.intellij.dlanguage.psi.references.GenericExpressionElementReference
import io.github.intellij.dlanguage.psi.types.DAliasType
import io.github.intellij.dlanguage.psi.types.DArrayType
import io.github.intellij.dlanguage.psi.types.DPointerType
import io.github.intellij.dlanguage.psi.types.DPrimitiveType
import io.github.intellij.dlanguage.psi.types.DType
import io.github.intellij.dlanguage.psi.types.UserDefinedDType
import io.github.intellij.dlanguage.utils.EnumDeclaration

abstract class DLanguageReferenceExpressionImplMixin(node: ASTNode) : ASTWrapperPsiElement(node),
    DLanguageReferenceExpression {

    override fun getDType(): DType? {
        val result = reference?.resolve()
        if (result is DTypedElement) {
            return result.dType
        }
        val qualifierType = expression?.dType
        // Compiler defined property? : https://dlang.org/spec/property.html
        val dtype =  when (identifier?.text) {
            "init" -> qualifierType
            "alignof",
            "sizeof" -> DPrimitiveType.fromText("uint") // TODO actually it is a size_t
            "mangleof",
            "stringof" -> DArrayType(DPrimitiveType.fromText("char"), null) // TODO it is immutable(char) array
            else -> null
        }
        if (dtype != null) {
            return dtype
        }
        if (isArrayType(qualifierType)) {
            return when (identifier?.text) {
                "dup" -> qualifierType
                "idup" -> qualifierType // TODO idup return an immutable array
                "length",
                "ptr" -> DPointerType(qualifierType!!)
                // TODO add tupleof for static array
                // TODO add capacity for dynamic array
                else -> null
            }
        } else if (isEnumType(qualifierType)) {
            return when (identifier?.text) {
                "init" -> qualifierType
                "min",
                "max" -> DPrimitiveType.fromText("int") // TODO actually need to fetch it from the enum
                else -> null
            }
        }
        // TODO handle all compiler defined properties (class, associative array, struct vector)

        return null
    }

    private fun isArrayType(type: DType?): Boolean {
        if (type is DPointerType) return isArrayType(type.base)
        if (type is DAliasType) return  isArrayType(type.aliased)
        return type is DArrayType
    }

    private fun isEnumType(type: DType?): Boolean {
        if (type is DPointerType) return isArrayType(type.base)
        if (type is DAliasType) return  isArrayType(type.aliased)
        return (type is UserDefinedDType && type.resolve() is EnumDeclaration)
    }

    override fun getReference(): PsiReference? {
        val referenceElement: PsiElement
        val range: TextRange
        if (identifier != null) {
            referenceElement = identifier!!
            range = referenceElement.textRangeInParent
        } else if (templateInstance != null && templateInstance!!.identifier != null) {
            referenceElement = templateInstance!!.identifier!!
            range = referenceElement.textRangeInParent.shiftRight(templateInstance!!.startOffsetInParent)
        }
        else {
            return null
        }
        return GenericExpressionElementReference(this, range, expression, referenceElement.text)
    }
}
