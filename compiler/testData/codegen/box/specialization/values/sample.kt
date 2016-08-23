// WITH_RUNTIME

value class vClass(val v: Int)

fun box(): String {
    val a = vClass(1)
    val t = takeAndReturn(a)

    assert(a == t)

    return "OK"
}

fun takeAndReturn(c: vClass): vClass {
    return c
}