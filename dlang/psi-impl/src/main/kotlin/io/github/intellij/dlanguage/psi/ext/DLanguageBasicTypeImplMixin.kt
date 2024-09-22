package io.github.intellij.dlanguage.psi.ext

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import io.github.intellij.dlanguage.psi.DLanguageBasicType
import io.github.intellij.dlanguage.psi.interfaces.DTypedElement
import io.github.intellij.dlanguage.psi.interfaces.TemplateParameter
import io.github.intellij.dlanguage.psi.types.DArrayType
import io.github.intellij.dlanguage.psi.types.DPrimitiveType
import io.github.intellij.dlanguage.psi.types.DType
import io.github.intellij.dlanguage.psi.types.DUnknownType

abstract class DLanguageBasicTypeImplMixin(node: ASTNode) : ASTWrapperPsiElement(node),
    DLanguageBasicType {

    override fun getDType(): DType {
        if (builtinType != null) {
            return DPrimitiveType.fromText(builtinType!!.text);
        }
        if (qualifiedIdentifier != null) {
            var resolved = qualifiedIdentifier?.reference?.resolve()
            var dType: DType = DUnknownType()
            if (resolved != null) {
                dType = when (resolved) {
                    is TemplateParameter -> DUnknownType()  // TODO
                    is DTypedElement -> resolved.dType
                    else -> throw NotImplementedError("Unexpected case of Qualified identifier DType for a Basic Type")
                }
            }
            if (qualifiedIdentifier!!.oP_BRACKET_LEFT != null) {
                return DArrayType(dType, null) // TODO set size
            }
            return dType
        }
        if (type != null) {
            // TODO theses are type constraints, they enforce different properties on the values of this types
            if (kW_CONST != null) {

            }
            if (kW_IMMUTABLE != null) {

            }
            if (kW_INOUT != null) {

            }
            if (kW_SHARED != null) {

            }
            return type!!.dType
        }
        // Fallback case
        return DUnknownType()
    }
}
