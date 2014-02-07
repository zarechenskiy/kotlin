package templates

import templates.Family.*

enum class PrimitiveType(val name: String) {
    Boolean: PrimitiveType("Boolean")
    Byte: PrimitiveType("Byte")
    Char: PrimitiveType("Char")
    Short: PrimitiveType("Short")
    Int: PrimitiveType("Int")
    Long: PrimitiveType("Long")
    Float: PrimitiveType("Float")
    Double: PrimitiveType("Double")
}

private fun PrimitiveType.zero() = when (this) {
    PrimitiveType.Int -> "0"
    PrimitiveType.Byte -> "0"
    PrimitiveType.Short -> "0"
    PrimitiveType.Long -> "0.toLong()"
    PrimitiveType.Double -> "0.0"
    PrimitiveType.Float -> "0.toFloat()"
    else -> throw IllegalArgumentException("Primitive type $this doesn't have default value")
}