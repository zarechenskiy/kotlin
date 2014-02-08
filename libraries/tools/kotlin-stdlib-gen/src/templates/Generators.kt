package templates

import templates.Family.*

fun generators(): List<GenericFunction> {
    val templates = arrayListOf<GenericFunction>()

    templates add f("plus(element: T)") {
        doc { "Returns a list containing all elements of original collection and then the given element" }
        returns("List<T>")
        body {
            """
                val answer = toArrayList()
                answer.add(element)
                return answer
            """
        }

        doc(Streams) { "Returns a stream containing all elements of original stream and then the given element" }
        returns(Streams) { "Stream<T>" }
        // TODO: Implement lazy behavior
        body(Streams) {
            """
                val answer = toArrayList()
                answer.add(element)
                return answer.stream()
            """
        }
    }

    templates add f("plus(collection: Iterable<T>)") {
        exclude(Streams)
        doc { "Returns a list containing all elements of original collection and then all elements of the given *collection*" }
        returns("List<T>")

        body {
            """
                val answer = toArrayList()
                answer.addAll(collection)
                return answer
            """
        }
    }

    templates add f("plus(stream: Stream<T>)") {
        only(Streams)
        doc { "Returns a stream containing all elements of original stream and then all elements of the given *stream*" }
        returns("Stream<T>")
        body {
            // TODO: Implement lazy behavior
            """
                val answer = toArrayList()
                answer.addAll(stream)
                return answer.stream()
            """
        }
    }

    templates add f("partition(predicate: (T) -> Boolean)") {
        doc {
            """
            Splits original collection into pair of collections,
            where *first* collection contains elements for which predicate yielded *true*,
            while *second* collection contains elements for which predicate yielded *false*
            """
        }
        // TODO: Stream variant
        returns("Pair<List<T>, List<T>>")
        body {
            """
                val first = ArrayList<T>()
                val second = ArrayList<T>()
                for (element in this) {
                    if (predicate(element)) {
                        first.add(element)
                    } else {
                        second.add(element)
                    }
                }
                return Pair(first, second)
            """
        }
    }

    return templates
}