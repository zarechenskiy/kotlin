package templates

import templates.Family.*

fun specialJVM(): List<GenericFunction> {
    val templates = arrayListOf<GenericFunction>()

    templates add f("filterIsInstanceTo(collection: C, klass: Class<R>)") {
        doc { "Appends all elements that are instances of specified class into the given *collection*" }
        typeParam("C: MutableCollection<in R>")
        typeParam("R: T")
        returns("C")
        exclude(ArraysOfPrimitives)
        body {
            """
            for (element in this) if (klass.isInstance(element)) collection.add(element as R)
            return collection
            """
        }
    }

    templates add f("filterIsInstance(klass: Class<R>)") {
        doc { "Returns a list containing all elements that are instances of specified class" }
        typeParam("R: T")
        returns("List<R>")
        body {
            """
            return filterIsInstanceTo(ArrayList<R>(), klass)
            """
        }
        exclude(ArraysOfPrimitives)

        doc(Streams) { "Returns a stream containing all elements that are instances of specified class" }
        returns(Streams) { "Stream<T>" }
        body(Streams) {
            """
            return FilteringStream(this, true, { klass.isInstance(it) })
            """
        }
    }

    return templates
}