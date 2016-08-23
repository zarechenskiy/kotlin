// WITH_RUNTIME

value class vDouble(val d: Double)

fun box(): String {
    val a = 1.0 as vDouble
    val t = takeAndReturn(a)

    assert(a == t)

    return "OK"
}

fun takeAndReturn(c: vDouble): vDouble {
    return c
}