// WITH_RUNTIME

fun box(): String {
    val vArr = intArrayOf(1) as Array<vInt>
    val actual: vInt = vArr.test()

    val expected = vInt(1)

    return if (actual == expected) "OK" else "Fail: $actual not equals expected ($expected)"
}

inline fun <@Anyfied T> Array<T>.test(): T {
    return this[0]
}

value class vInt(val v: Int)