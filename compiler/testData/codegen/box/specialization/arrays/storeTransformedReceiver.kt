// WITH_RUNTIME

fun box(): String {
    val vArr1 = intArrayOf(1) as Array<vInt>
    val vArr2 = intArrayOf(0) as Array<vInt>
    val el = 1 as vInt

    vArr1.storeInto(vArr2) {
        el
    }

    return if (vArr2[0] == el) "OK" else "Fail: ${vArr2[0]} not equals expected ($el)"
}

inline fun <@Anyfied T> Array<T>.storeInto(
        destination: Array<T>, transform: (T) -> T) {

    destination[0] = transform(this[0])
}

value class vInt(val v: Int)