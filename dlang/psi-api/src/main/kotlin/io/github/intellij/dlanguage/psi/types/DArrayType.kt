package io.github.intellij.dlanguage.psi.types

class DArrayType(val base: DType, val size: Long? = null) : DType {

    override fun toString(): String = if (size == null) "$base[]" else "$base[$size]"

    fun isFixedSize(): Boolean = size != null
}

class DAssociativeArrayType(val base: DType, val keyType: DType) : DType {
    override fun toString(): String = "$base[$keyType]"
}
