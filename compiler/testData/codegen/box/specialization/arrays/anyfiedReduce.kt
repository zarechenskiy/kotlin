// WITH_RUNTIME

fun box(): String {
    val vArr = intArrayOf(1, 2, 3, 4) as Array<vInt>

    val reduceResult = vArr.anyfiedReduce { x, y -> y }

    assert(reduceResult == vArr[3])

    return "OK"
}

inline fun <@Anyfied S, @Anyfied T : S> Array<out T>.anyfiedReduce(operation: (S, T) -> S): S {
    if (isEmptyAnyfied())
        throw UnsupportedOperationException("Empty array can't be reduced.")
    var accumulator: S = this[0]
    for (index in 1..getLastIndex()) {
        accumulator = operation(accumulator, this[index])
    }
    return accumulator
}

inline fun <T> Array<out T>.isEmptyAnyfied(): Boolean {
    return size == 0
}

inline fun <@Anyfied T> Array<out T>.getLastIndex(): Int {
    return size - 1
}