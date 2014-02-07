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

private fun returnTypeForSum(primitive: PrimitiveType) = when (primitive) {
    PrimitiveType.Byte -> PrimitiveType.Int
    PrimitiveType.Short -> PrimitiveType.Int
    else -> primitive
}

fun sumFunction(primitive: PrimitiveType) =
        f("sum()") {
            doc = "Sums up the elements"
            makeInline = false
            if (returnTypeForSum(primitive) != primitive) {
                //this is required to avoid clash of erasured method signatures (Iterables.sum(): Int)
                //absentFor(Iterables)
            }
            Iterables.customReceiver("Iterable<${primitive.name}>")
            ArraysOfObjects.customReceiver("Array<${primitive.name}>")
            returns(returnTypeForSum(primitive).name)
            body {
                "return fold(${primitive.zero()}, {a,b -> a+b})"
            }
        }
