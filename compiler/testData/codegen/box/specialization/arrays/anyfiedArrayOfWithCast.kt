// WITH_RUNTIME

fun box(): String {
    val v1 = 1 as vInt
    val v2 = 2 as vInt
    val array = anyfiedArrayOf(v1, v2)
    return if (array[0] == v1 && array[1] == v2) "OK" else "Fail: array contains ${array[0]}, ${array[1]}"
}

inline fun <reified @Anyfied T> anyfiedArrayOf(vararg elements: T): Array<T> {
    return elements as Array<T>
}