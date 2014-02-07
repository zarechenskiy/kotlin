package templates

import templates.Family.*

fun conversions(): List<GenericFunction> {
    val templates = arrayListOf<GenericFunction>()

    templates add f("toCollection(collection : C)") {
        doc { "Adds all elements to a new ArrayList" }
        returns("C")
        typeParam("C : MutableCollection<in T>")
        body {
            """
            for (item in this) {
                collection.add(item)
            }
            return collection
            """
        }
    }

    templates add f("toSet()") {
        doc { "Copies all elements into a [[Set]]" }
        returns("Set<T>")
        body { "return toCollection(LinkedHashSet<T>())" }
    }

    templates add f("toSortedSet()") {
        doc { "Copies all elements into a [[SortedSet]]" }
        returns("SortedSet<T>")
        body { "return toCollection(TreeSet<T>())" }
    }

    templates add f("toArrayList()") {
        doc { "Adds all elements to a new ArrayList" }
        returns("ArrayList<T>")
        body { "return toCollection(ArrayList<T>())" }

        include(Collections)
        body(Collections) {
            """
            return ArrayList<T>(this)
            """
        }
        body(ArraysOfObjects, ArraysOfPrimitives) {
            """
            val list = ArrayList<T>(size)
            for (item in this) list.add(item)
            return list
            """
        }
    }

    templates add f("toList()") {
        doc { "Returns new List<T> containing all elements" }
        returns("List<T>")
        body { "return toCollection(ArrayList<T>())" }

        include(Collections)
        body(Collections) {
            """
            return ArrayList<T>(this)
            """
        }
        body(ArraysOfObjects) {
            """
            return ArrayList<T>(Arrays.asList(*this))
            """
        }
        body(ArraysOfPrimitives) {
            """
            val list = ArrayList<T>(size)
            for (item in this) list.add(item)
            return list
            """
        }
    }

    templates add f("toLinkedList()") {
        doc { "Copies all elements into a [[LinkedList]]" }
        returns("LinkedList<T>")
        body { "return toCollection(LinkedList<T>())" }
    }

    // TODO: is this needed?
    templates add f("toSortedList()") {
        doc { "Copies all elements into a [[List]] and sorts them" }
        typeParam("T: Comparable<T>")
        returns("List<T>")
        body { "return toArrayList().sort()" }
    }

    return templates
}