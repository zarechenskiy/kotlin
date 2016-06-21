// WITH_RUNTIME

fun box(): String {
    val arr = intArrayOf(1) as Array<vInt>

    val p = takeExtArray(arr) {
        arr[0]
    }

    assert(p == arr[0])

    return "OK"
}

inline fun <@Anyfied T> takeExtArray(a: Array<T>, t: Array<T>.() -> T): T {
    return a.t()
}