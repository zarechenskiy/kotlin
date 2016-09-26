// WITH_RUNTIME

fun box(): String {
    val vArr = intArrayOf(1) as Array<vInt>
    val el = vInt(1)

    storeInto(vArr, el)

    assert(vArr[0] == el)

    return "OK"
}

inline fun <@Anyfied T> storeInto(destination: Array<T>, el: T) {
    destination[0] = el
}

value class vInt(val v: Int)