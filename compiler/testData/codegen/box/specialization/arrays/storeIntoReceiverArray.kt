// WITH_RUNTIME

fun box(): String {
    val vArr = intArrayOf(1) as Array<vInt>
    val el = 1 as vInt

    vArr.storeEl(el)

    return if (vArr[0] == el) "OK" else "Fail: ${vArr[0]} not equals expected ($el)"
}

inline fun <@Anyfied T> Array<T>.storeEl(el: T) {
    this[0] = el
}