package generators

import java.io.*
import templates.Family.*
import templates.*
import templates.PrimitiveType.*

fun generateCollectionsAPI(outDir : File) {
    elements().writeTo(File(outDir, "_Elements.kt")) { build() }
    subsequences().writeTo(File(outDir, "_Subsequences.kt")) { build() }
    ordering().writeTo(File(outDir, "_Ordering.kt")) { build() }
    arrays().writeTo(File(outDir, "_Arrays.kt")) { build() }
    conversions().writeTo(File(outDir, "_Conversions.kt"))  { build() }
    mapping().writeTo(File(outDir, "_Mapping.kt")) { build() }
    aggregates().writeTo(File(outDir, "_Aggregates.kt")) { build() }
    guards().writeTo(File(outDir, "_Guards.kt")) { build() }
    generators().writeTo(File(outDir, "_Generators.kt")) { build() }

    numeric().writeTo(File(outDir, "_Numeric.kt")) {
        val builder = StringBuilder()
        val numerics = listOf(Int, Long, Byte, Short, Double, Float)
        for(numeric in numerics) {
            build(builder, Iterables, numeric)
            build(builder, ArraysOfObjects, numeric)
            build(builder, ArraysOfPrimitives, numeric)
        }
        builder.toString()
    }

}
