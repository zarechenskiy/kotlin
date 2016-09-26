// WITH_RUNTIME

fun box(): String {
    val p = createArray<vInt>(10)
    val first = p[0]
    val zero = vInt(0)

    assert(first == zero)

    return "OK"
}

inline fun <reified @Anyfied T> createArray(size: Int): Array<T> {
    return arrayOfNulls<T>(size) as Array<T>
}

value class vInt(val v: Int)