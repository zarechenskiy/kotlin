package templates

import templates.Family.*

fun aggregates(): List<GenericFunction> {
    val templates = arrayListOf<GenericFunction>()

    templates add f("all(predicate: (T) -> Boolean)") {
        doc { "Returns *true* if all elements match the given *predicate*" }
        returns("Boolean")
        body {
            """
                for (element in this) if (!predicate(element)) return false
                return true
            """
        }
    }

    templates add f("none(predicate: (T) -> Boolean)") {
        doc { "Returns *true* if no elements match the given *predicate*" }
        returns("Boolean")
        body {
            """
                for (element in this) if (predicate(element)) return false
                return true
            """
        }
    }

    templates add f("any(predicate: (T) -> Boolean)") {
        doc { "Returns *true* if any elements match the given *predicate*" }
        returns("Boolean")
        body {
            """
                for (element in this) if (predicate(element)) return true
                return false
            """
        }
    }

    templates add f("count(predicate: (T) -> Boolean)") {
        doc { "Returns the number of elements matching the given *predicate*" }
        returns("Int")
        body {
            """
               var count = 0
               for (element in this) if (predicate(element)) count++
               return count
           """
        }
    }

    templates add f("count()") {
        doc { "Returns the number of elements" }
        returns("Int")
        body {
            """
               var count = 0
               for (element in this) count++
               return count
           """
        }
        include(Collections)
        body(Collections, ArraysOfObjects, ArraysOfPrimitives) {
            "return size"
        }
    }

    templates add f("min()") {
        doc { "Returns the smallest element or null if there are no elements" }
        returns("T?")
        exclude(PrimitiveType.Boolean)
        typeParam("T: Comparable<T>")
        body {
            """
                val iterator = iterator()
                if (!iterator.hasNext()) return null

                var min = iterator.next()
                while (iterator.hasNext()) {
                    val e = iterator.next()
                    if (min > e) min = e
                }
                return min
            """
        }
        body(ArraysOfObjects, ArraysOfPrimitives) {
            """
                if (isEmpty()) return null
                var min = this[0]
                for (i in 1..lastIndex) {
                    val e = this[i]
                    if (min > e) min = e
                }
                return min
            """
        }
    }

    templates add f("minBy(f: (T) -> R)") {
        doc { "Returns the first element yielding the smallest value of the given function or null if there are no elements" }
        typeParam("R: Comparable<R>")
        typeParam("T: Any")
        returns("T?")
        body {
            """
                val iterator = iterator()
                if (!iterator.hasNext()) return null

                var minElem = iterator.next()
                var minValue = f(minElem)
                while (iterator.hasNext()) {
                    val e = iterator.next()
                    val v = f(e)
                    if (minValue > v) {
                       minElem = e
                       minValue = v
                    }
                }
                return minElem
            """
        }
        body(ArraysOfObjects, ArraysOfPrimitives) {
            """
                    if (size == 0) return null

                    var minElem = this[0]
                    var minValue = f(minElem)
                    for (i in 1..lastIndex) {
                        val e = this[i]
                        val v = f(e)
                        if (minValue > v) {
                           minElem = e
                           minValue = v
                        }
                    }
                    return minElem
                """

        }
    }

    templates add f("max()") {
        doc { "Returns the largest element or null if there are no elements" }
        returns("T?")
        exclude(PrimitiveType.Boolean)
        typeParam("T: Comparable<T>")
        body {
            """
                val iterator = iterator()
                if (!iterator.hasNext()) return null

                var max = iterator.next()
                while (iterator.hasNext()) {
                    val e = iterator.next()
                    if (max < e) max = e
                }
                return max
            """
        }

        body(ArraysOfObjects, ArraysOfPrimitives) {
            """
                if (isEmpty()) return null

                var max = this[0]
                for (i in 1..lastIndex) {
                    val e = this[i]
                    if (max < e) max = e
                }
                return max
            """
        }
    }

    templates add f("maxBy(f: (T) -> R)") {
        doc { "Returns the first element yielding the largest value of the given function or null if there are no elements" }
        typeParam("R: Comparable<R>")
        typeParam("T: Any")
        returns("T?")
        body {
            """
                val iterator = iterator()
                if (!iterator.hasNext()) return null

                var maxElem = iterator.next()
                var maxValue = f(maxElem)
                while (iterator.hasNext()) {
                    val e = iterator.next()
                    val v = f(e)
                    if (maxValue < v) {
                       maxElem = e
                       maxValue = v
                    }
                }
                return maxElem
            """
        }
        body(ArraysOfObjects, ArraysOfPrimitives) {
            """
                if (isEmpty()) return null

                var maxElem = this[0]
                var maxValue = f(maxElem)
                for (i in 1..lastIndex) {
                    val e = this[i]
                    val v = f(e)
                    if (maxValue < v) {
                       maxElem = e
                       maxValue = v
                    }
                }
                return maxElem
            """
        }
    }



    templates add f("fold(initial: R, operation: (R, T) -> R)") {
        doc { "Folds all elements from from left to right with the *initial* value to perform the operation on sequential pairs of elements" }
        typeParam("R")
        returns("R")
        body {
            """
            var answer = initial
            for (element in this) answer = operation(answer, element)
            return answer
            """
        }
    }

    templates add f("foldRight(initial: R, operation: (R, T) -> R)") {
        only(Lists, ArraysOfObjects, ArraysOfPrimitives)
        doc {
            """
            Applies binary operation to all elements of iterable, going from right to left.
            Similar to foldRight function, but uses the last element as initial value
            """
        }
        typeParam("R")
        returns("R")
        body {
            """
            var index = size - 1
            if (index < 0) throw UnsupportedOperationException("Empty iterable can't be reduced")
            var answer = initial
            while (index >= 0) {
                answer = operation(answer, get(index--))
            }
            return answer
            """
        }
    }


    templates add f("reduce(operation: (T, T) -> T)") {
        doc {
            """
            Applies binary operation to all elements of iterable, going from left to right.
            Similar to fold function, but uses the first element as initial value
            """
        }
        returns("T")

        body {
            """
            val iterator = this.iterator()
            if (!iterator.hasNext()) {
                throw UnsupportedOperationException("Empty iterable can't be reduced")
            }

            var result: T = iterator.next() //compiler doesn't understand that result will initialized anyway
            while (iterator.hasNext()) {
                result = operation(result, iterator.next())
            }
            return result
            """
        }
    }

    templates add f("reduceRight(operation: (T, T) -> T)") {
        only(Lists, ArraysOfObjects, ArraysOfPrimitives)
        doc {
            """
            Applies binary operation to all elements of iterable, going from right to left.
            Similar to foldRight function, but uses the last element as initial value
            """
        }
        returns("T")
        body {
            """
            var index = size - 1
            if (index < 0) {
                throw UnsupportedOperationException("Empty iterable can't be reduced")
            }

            var r = get(index--)
            while (index >= 0) {
                r = operation(get(index--), r)
            }

            return r
            """
        }
    }


    templates add f("forEach(operation: (T) -> Unit)") {
        doc { "Performs the given *operation* on each element" }
        returns("Unit")
        body {
            """
            for (element in this) operation(element)
            """
        }
    }

    templates add f("appendString(buffer: Appendable, separator: String = \", \", prefix: String =\"\", postfix: String = \"\", limit: Int = -1, truncated: String = \"...\")") {
        doc {
            """
            Appends the string from all the elements separated using the *separator* and using the given *prefix* and *postfix* if supplied

            If a collection could be huge you can specify a non-negative value of *limit* which will only show a subset of the collection then it will
            a special *truncated* separator (which defaults to "..."
            """
        }
        returns { "Unit" }
        body {
            """
            buffer.append(prefix)
            var count = 0
            for (element in this) {
                if (++count > 1) buffer.append(separator)
                if (limit < 0 || count <= limit) {
                    val text = if (element == null) "null" else element.toString()
                    buffer.append(text)
                } else break
            }
            if (limit >= 0 && count > limit) buffer.append(truncated)
            buffer.append(postfix)
            """
        }
    }

    templates add f("makeString(separator: String = \", \", prefix: String = \"\", postfix: String = \"\", limit: Int = -1, truncated: String = \"...\")") {
        doc {
            """
            Creates a string from all the elements separated using the *separator* and using the given *prefix* and *postfix* if supplied.

            If a collection could be huge you can specify a non-negative value of *limit* which will only show a subset of the collection then it will
            a special *truncated* separator (which defaults to "..."
            """
        }

        returns("String")
        body {
            """
            val buffer = StringBuilder()
            appendString(buffer, separator, prefix, postfix, limit, truncated)
            return buffer.toString()
            """
        }
    }

    return templates
}