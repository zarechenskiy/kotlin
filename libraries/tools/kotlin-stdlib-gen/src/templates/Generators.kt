package templates

import templates.Family.*

fun generators(): List<GenericFunction> {
    val templates = arrayListOf<GenericFunction>()

    templates add f("plus(element: T)") {
        doc { "Creates an [[Iterator]] which iterates over this iterator then the given element at the end" }
        returns("List<T>")
        body {
            """
                val answer = toArrayList()
                answer.add(element)
                return answer
            """
        }

        doc(Streams) { "Creates an [[Iterator]] which iterates over this iterator then the given element at the end" }
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
        doc { "Creates an [[Iterator]] which iterates over this iterator then the following collection" }
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
        doc { "Creates an [[Iterator]] which iterates over this iterator then the following collection" }
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
        doc { "Partitions this collection into a pair of collections" }
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