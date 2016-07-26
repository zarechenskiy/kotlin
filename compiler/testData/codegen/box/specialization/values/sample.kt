// WITH_RUNTIME

value class vClass(val v: Int)

fun box(): String {
    val a = 1 as vClass
    val t = takeAndReturn(a)

    assert(a == t)

    return "OK"
}

fun takeAndReturn(c: vClass): vClass {
    return c
}