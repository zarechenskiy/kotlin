package templates

import templates.Family.*

fun filtering(): List<GenericFunction> {
    val templates = arrayListOf<GenericFunction>()

    templates add f("drop(n: Int)") {
        doc { "Returns a list containing all elements except first *n* elements" }
        returns("List<T>")
        body {
            """
            var count = 0
            val list = ArrayList<T>()
            for (item in this) {
                if (count++ >= n) list.add(item)
            }
            return list
            """
        }

        doc(Streams) { "Returns a stream containing all elements except first *n* elements" }
        returns(Streams) { "Stream<T>" }
        body(Streams) {
            """
            var count = 0;
            return FilteringStream(this) { count++ >= n }
            """
        }

        include(Collections)
        body(Collections, ArraysOfObjects, ArraysOfPrimitives) {
            """
            if (n >= size)
                return ArrayList<T>()

            var count = 0
            val list = ArrayList<T>(size - n)
            for (item in this) {
                if (count++ >= n) list.add(item)
            }
            return list
            """
        }
    }

    templates add f("take(n: Int)") {
        doc { "Returns a list containing first *n* elements" }
        returns("List<T>")
        body {
            """
            var count = 0
            val list = ArrayList<T>(n)
            for (item in this)
                if (count++ >= n)
                    list.add(item)
            return list
            """
        }

        doc(Streams) { "Returns a stream containing first *n* elements" }
        returns(Streams) { "Stream<T>" }
        body(Streams) {
            """
            var count = 0
            return LimitedStream(this) { count++ == n }
            """
        }

        include(Collections)
        body(Collections, ArraysOfObjects, ArraysOfPrimitives) {
            """
            var count = 0
            val realN = if (n > size) size else n
            val list = ArrayList<T>(realN)
            for (item in this) {
                if (count++ == realN)
                    break;
                list.add(item)
            }
            return list
            """
        }
    }

    templates add f("dropWhile(predicate: (T)->Boolean)") {
        doc { "Returns a list containing all elements except first elements that satisfy the given *predicate*" }
        returns("List<T>")
        body {
            """
            var yielding = false
            val list = ArrayList<T>()
            for (item in this)
                if (yielding)
                    list.add(item)
                else if(!predicate(item)) {
                    list.add(item)
                    yielding = true
                }
            return list
            """
        }

        doc(Streams) { "Returns a stream containing all elements except first elements that satisfy the given *predicate*" }
        returns(Streams) { "Stream<T>" }
        body(Streams) {
            """
            var yielding = false
            return FilteringStream(this) {
                        if (yielding)
                            true
                        else if (!predicate(it)) {
                            yielding = true
                            true
                        } else
                            false
                    }
            """
        }

    }

    templates add f("takeWhile(predicate: (T)->Boolean)") {
        doc { "Returns a list containing first elements satisfying the given *predicate*" }
        returns("List<T>")
        body {
            """
            val list = ArrayList<T>()
            for (item in this) {
                 if(!predicate(item))
                    break;
                 list.add(item)
            }
            return list
            """
        }

        doc(Streams) { "Returns a stream containing first elements satisfying the given *predicate*" }
        returns(Streams) { "Stream<T>" }
        body(Streams) {
            """
            return LimitedStream(this, false, predicate)
            """
        }
    }

    templates add f("filter(predicate: (T)->Boolean)") {
        doc { "Returns a list containing all elements except first elements that satisfy the given *predicate*" }
        returns("List<T>")
        body {
            """
            return filterTo(ArrayList<T>(), predicate)
            """
        }

        doc(Streams) { "Returns a stream containing all elements except first elements that satisfy the given *predicate*" }
        returns(Streams) { "Stream<T>" }
        body(Streams) {
            """
            return FilteringStream(this, true, predicate)
            """
        }
    }

    templates add f("filterTo(result: C, predicate: (T) -> Boolean)") {
        doc { "Filters all elements which match the given predicate into the given list" }
        typeParam("C: MutableCollection<in T>")
        returns("C")

        body {
            """
            for (element in this) if (predicate(element)) result.add(element)
            return result
            """
        }
    }

    templates add f("filterIsInstanceTo(result: C, klass: Class<R>)") {
        doc { "Filters all elements which match the given predicate into the given list" }
        typeParam("C: MutableCollection<in R>")
        typeParam("R: T")
        returns("C")

        body {
            """
            for (element in this) if (klass.isInstance(element)) result.add(element as R)
            return result
            """
        }
    }

    templates add f("filterIsInstance(klass: Class<R>)") {
        doc { "Filters all elements which match the given predicate into the given list" }
        typeParam("R: T")
        returns("List<R>")
        body {
            """
            return filterIsInstanceTo(ArrayList<R>(), klass)
            """
        }
    }


    templates add f("filterNot(predicate: (T)->Boolean)") {
        doc { "Returns a list containing all elements except first elements that does not satisfy the given *predicate*" }
        returns("List<T>")
        body {
            """
            return filterNotTo(ArrayList<T>(), predicate)
            """
        }

        doc(Streams) { "Returns a stream containing all elements except first elements that does not satisfy the given *predicate*" }
        returns(Streams) { "Stream<T>" }
        body(Streams) {
            """
            return FilteringStream(this, false, predicate)
            """
        }
    }

    templates add f("filterNotTo(result: C, predicate: (T) -> Boolean)") {
        doc { "Returns a list containing all elements which do not match the given *predicate*" }
        typeParam("C: MutableCollection<in T>")
        returns("C")

        body {
            """
            for (element in this) if (!predicate(element)) result.add(element)
            return result
            """
        }
    }

    templates add f("filterNotNull()") {
        exclude(ArraysOfPrimitives)
        doc { "Returns a list containing all elements except first elements that does not satisfy the given *predicate*" }
        typeParam("T: Any")
        returns("List<T>")
        toNullableT = true
        body {
            """
            return filterNotNullTo(ArrayList<T>())
            """
        }

        doc(Streams) { "Returns a stream containing all elements except first elements that does not satisfy the given *predicate*" }
        returns(Streams) { "Stream<T>" }
        body(Streams) {
            """
            return FilteringStream(this, false, { it != null }) as Stream<T>
            """
        }
    }

    templates add f("filterNotNullTo(result: C)") {
        exclude(ArraysOfPrimitives)
        doc { "Returns a list containing all elements which do not match the given *predicate*" }
        typeParam("C: MutableCollection<in T>")
        typeParam("T: Any")
        returns("C")
        toNullableT = true
        body {
            """
            for (element in this) if (element != null) result.add(element)
            return result
            """
        }
    }

    return templates
}