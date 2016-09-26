// WITH_RUNTIME

fun box(): String {
    val arr = intArrayOf(1) as Array<vInt>
    val el = vInt(3)

    testInit(arr, el)

    assert(arr[0] == el)

    return "OK"
}

fun testInit(vArr: Array<vInt>, v: vInt) {
    sampleInit(vArr) {
        v
    }
}

inline fun <@Anyfied T> sampleInit(arr: Array<T>, init: (Int) -> T) {
    arr[0] = init(3)
}

value class vInt(val v: Int)