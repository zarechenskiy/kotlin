// WITH_RUNTIME

value class vDouble(val d: Double)

fun box(): String {
    val a = vDouble(1.0)
    val t = takeAndReturn(a)

    assert(a == t)

    return "OK"
}

fun takeAndReturn(c: vDouble): vDouble {
    return c
}