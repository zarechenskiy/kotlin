package templates

import templates.Family.*

fun numeric(): List<GenericFunction> {
    val templates = arrayListOf<GenericFunction>()

    templates add f("sum()") {
        doc { "Returns the largest element or null if there are no elements" }
        returns("SUM")
        body {
            """
                val iterator = iterator()
                var sum : SUM = ZERO
                while (iterator.hasNext()) {
                    sum += iterator.next()
                }
                return sum
            """
        }
    }

    return templates
}
