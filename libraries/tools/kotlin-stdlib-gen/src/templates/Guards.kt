package templates

import java.util.ArrayList
import templates.Family.*

fun guards(): List<GenericFunction> {

    val templates = ArrayList<GenericFunction>()

    templates add f("requireNoNulls()") {
        include(Lists)
        exclude(ArraysOfPrimitives)
        doc { "Returns an original Iterable containing all the non-*null* elements, throwing an [[IllegalArgumentException]] if there are any null elements" }
        typeParam("T:Any")
        toNullableT = true
        returns("SELF")
        body {
            val THIS = "\$this"
            """
            for (element in this) {
                if (element == null) {
                    throw IllegalArgumentException("null element found in $THIS")
                }
            }
            return this as SELF
            """
        }
        body(Streams) {
            val THIS = "\$this"
            """
            return FilteringStream(this) {
                if (it == null) {
                    throw IllegalArgumentException("null element found in $this")
                }
                true
            } as Stream<T>
            """
        }
    }

    return templates
}