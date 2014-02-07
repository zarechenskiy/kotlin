package templates

import templates.Family.*

fun ordering(): List<GenericFunction> {
    val templates = arrayListOf<GenericFunction>()

    templates add f("reverse()") {
        doc { "Returns a list with elements in reversed order" }
        returns { "List<T>" }
        body {
            """
                val list = toArrayList()
                Collections.reverse(list)
                return list
            """
        }

        exclude(Streams)
    }

    templates add f("sort()") {
        doc {
            """
            Copies all elements into a [[List]] and sorts it
            """
        }
        returns("List<T>")
        typeParam("T: Comparable<T>")
        body {
            """
                val sortedList = toArrayList()
                val sortBy: Comparator<T> = comparator<T> {(x: T, y: T) -> x.compareTo(y)}
                java.util.Collections.sort(sortedList, sortBy)
                return sortedList
            """
        }

        exclude(Streams)
        exclude(ArraysOfPrimitives) // TODO: resolve collision between inplace sort and this function
        exclude(ArraysOfObjects)
    }

    templates add f("sortBy(f: (T) -> R)") {
        doc {
            """
            Copies all elements into a [[List]] and sorts it by value of f(element)
            """
        }
        returns("List<T>")
        typeParam("R: Comparable<R>")
        body {
            """
                val sortedList = toArrayList()
                val sortBy: Comparator<T> = comparator<T> {(x: T, y: T) -> f(x).compareTo(f(y))}
                java.util.Collections.sort(sortedList, sortBy)
                return sortedList
            """
        }

        exclude(Streams)
        exclude(ArraysOfPrimitives)
    }

    templates add f("sortBy(comparator : Comparator<T>)") {
        doc {
            """
            Copies all elements into a [[List]] and sorts it using provided comparator
            """
        }
        returns("List<T>")
        body {
            """
                val sortedList = toArrayList()
                java.util.Collections.sort(sortedList, comparator)
                return sortedList
            """
        }

        exclude(Streams)
        exclude(ArraysOfPrimitives)
    }

    return templates
}