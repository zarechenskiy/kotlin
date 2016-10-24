// WITH_RUNTIME

value class VInt(val v: Int) {
    fun foo(n: Int): Int {
        return v + n
    }
}

fun box(): String {
    val seed = VInt(10)

    val c = (1..3).map(seed::foo).sum()
    assert(c == 36)

    return "OK"
}