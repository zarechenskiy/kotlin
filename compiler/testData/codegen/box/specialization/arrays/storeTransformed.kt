// WITH_RUNTIME

fun box(): String {
    val vArr = intArrayOf(1) as Array<vInt>
    val el1 = 1 as vInt
    val el2 = 2 as vInt

    storeInto(vArr, el1) {
        el2
    }

    return if (vArr[0] == el2) "OK" else "Fail: ${vArr[0]} not equals expected ($el2)"
}

inline fun <@Anyfied T> storeInto(
        destination: Array<T>, el: T, transform: (T) -> T) {

    destination[0] = transform(el)
}