// WITH_RUNTIME

fun box(): String {
    val vArr = intArrayOf(1) as Array<vInt>
    val el1 = vInt(1)
    val el2 = vInt(2)

    storeInto(vArr, el1) {
        el2
    }

    return if (vArr[0] == el2) "OK" else "Fail: ${vArr[0]} not equals expected ($el2)"
}

inline fun <@Anyfied T> storeInto(
        destination: Array<T>, el: T, transform: (T) -> T) {

    destination[0] = transform(el)
}

value class vInt(val v: Int)