// WITH_RUNTIME

fun box(): String {
    val vArr = intArrayOf(1, 2, 3, 4) as Array<vInt>
    val seed = 0 as vInt

    val foldResult = vArr.anyfiedFold(seed) { x, y -> y }

    assert(foldResult == vArr[3])

    return "OK"
}

inline fun <@Anyfied T, @Anyfied R> Array<out T>.anyfiedFold(initial: R, operation: (R, T) -> R): R {
    var accumulator = initial
    for (element in this) accumulator = operation(accumulator, element)
    return accumulator
}