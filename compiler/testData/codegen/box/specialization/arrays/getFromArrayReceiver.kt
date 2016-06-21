// WITH_RUNTIME

fun box(): String {
    val vArr = intArrayOf(1) as Array<vInt>
    val actual: vInt = vArr.test()

    val expected = 1 as vInt

    return if (actual == expected) "OK" else "Fail: $actual not equals expected ($expected)"
}

inline fun <@Anyfied T> Array<T>.test(): T {
    return this[0]
}