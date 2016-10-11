// WITH_RUNTIME

value class VInt(val v: Int) {
    operator fun box(): Int = v
    operator fun unbox(boxed: Int): VInt = VInt(boxed)
}

fun box(): String {
    val ls = listOf(VInt(1), VInt(2), VInt(3))
    val (a, b, c) = ls

    assert(a.v == 1)
    assert(b.v == 2)
    assert(c.v == 3)

    return "OK"
}