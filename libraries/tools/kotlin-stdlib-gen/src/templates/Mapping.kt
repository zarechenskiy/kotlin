package templates

import templates.Family.*

fun mapping(): List<GenericFunction> {
    val templates = arrayListOf<GenericFunction>()

    templates add f("map(transform : (T) -> R)") {
        doc { "Returns a new List containing the results of applying the given *transform* function to each element in this collection" }
        typeParam("R")
        returns("List<R>")
        body {
            "return mapTo(ArrayList<R>(), transform)"
        }

        returns(Streams) { "Stream<R>" }
        body(Streams) {
            "return TransformingStream(this, transform) "
        }

    }

    templates add f("mapTo(result: C, transform : (T) -> R)") {
        doc {
            """
            Transforms each element of this collection with the given *transform* function and
            adds each return value to the given *results* collection
            """
        }

        typeParam("R")
        typeParam("C: MutableCollection<in R>")
        returns("C")

        body {
            """
                for (item in this)
                    result.add(transform(item))
                return result
            """
        }
    }

    templates add f("flatMap(transform: (T)-> Iterable<R>)") {
        exclude(Streams)
        doc { "Returns the result of transforming each element to one or more values which are concatenated together into a single list" }
        typeParam("R")
        returns("List<R>")
        body {
            "return flatMapTo(ArrayList<R>(), transform)"
        }
    }

    templates add f("flatMap(transform: (T)-> Stream<R>)") {
        only(Streams)
        doc { "Returns the result of transforming each element to one or more values which are concatenated together into a single list" }
        typeParam("R")
        returns("Stream<R>")
        body {
            // TODO: Implement lazy flatMap
            "return flatMapTo(ArrayList<R>(), transform).stream()"
        }
    }

    templates add f("flatMapTo(result: C, transform: (T) -> Iterable<R>)") {
        exclude(Streams)
        doc { "Returns the result of transforming each element to one or more values which are concatenated together into a single collection" }
        typeParam("R")
        typeParam("C: MutableCollection<in R>")
        returns("C")
        body {
            """
                for (element in this) {
                    val list = transform(element)
                    result.addAll(list)
                }
                return result
            """
        }
    }

    templates add f("flatMapTo(result: C, transform: (T) -> Stream<R>)") {
        only(Streams)
        doc { "Returns the result of transforming each element to one or more values which are concatenated together into a single stream" }
        typeParam("R")
        typeParam("C: MutableCollection<in R>")
        returns("C")
        body {
            """
                for (element in this) {
                    val list = transform(element)
                    result.addAll(list)
                }
                return result
            """
        }
    }

    templates add f("flatMapTo(result: C, transform: (T) -> Iterable<R>)") {
        only(Streams)
        doc { "Returns the result of transforming each element to one or more values which are concatenated together into a single stream" }
        typeParam("R")
        typeParam("C: MutableCollection<in R>")
        returns("C")
        body {
            """
                for (element in this) {
                    val list = transform(element)
                    result.addAll(list)
                }
                return result
            """
        }
    }

    templates add f("groupBy(toKey: (T) -> K)") {
        doc { "Groups the elements in the collection into a new [[Map]] using the supplied *toKey* function to calculate the key to group the elements by" }
        typeParam("K")
        returns("Map<K, List<T>>")
        body { "return groupByTo(HashMap<K, MutableList<T>>(), toKey)" }
    }

    templates add f("groupByTo(result: MutableMap<K, MutableList<T>>, toKey: (T) -> K)") {
        typeParam("K")
        returns("Map<K, MutableList<T>>")
        body {
            """
                for (element in this) {
                    val key = toKey(element)
                    val list = result.getOrPut(key) { ArrayList<T>() }
                    list.add(element)
                }
                return result
            """
        }
    }
    return templates
}