// WITH_RUNTIME

fun box(): String {
    val v = vInt(1)
    val arr = AnyfiedArray(10) { v }

    assert(arr[5] == v)

    return "OK"
}

inline fun <reified @Anyfied T> AnyfiedArray(size: Int, init: (Int) -> T): Array<T> {
    val result = createAnyfiedArray<T>(size)
    for (i in 0..size - 1) {
        result[i] = init(i)
    }
    return result
}

inline fun <reified @Anyfied T> createAnyfiedArray(size: Int): Array<T> {
    return arrayOfNulls<T>(size) as Array<T>
}

value class vInt(val v: Int)