// WITH_RUNTIME

fun box(): String {
    val arr = intArrayOf(1, 2, 3) as Array<vInt>

    val zipResult = arr.anyfiedZip(arr) { x, y -> x }

    assert(zipResult[0] == arr[0])
    assert(zipResult[1] == arr[1])

    return "OK"
}

inline fun <@Anyfied T, @Anyfied R, reified @Anyfied V> Array<out T>.anyfiedZip(other: Array<out R>, transform: (T, R) -> V): Array<V> {
    val size = Math.min(size, other.size)
    val array = createAnyfiedArray<V>(size)
    for (i in 0..size-1) {
        array[i] = transform(this[i], other[i])
    }

    return array
}

inline fun <reified @Anyfied T> createAnyfiedArray(size: Int): Array<T> {
    return arrayOfNulls<T>(size) as Array<T>
}

value class vInt(val v: Int)