// WITH_RUNTIME

value class VInt(val v: Int) {
    operator fun box(): Int = v
    operator fun unbox(boxed: Int) = VInt(boxed)

    fun inc(): VInt = VInt(v + 1)
}

class Normal(val first: VInt) {
    companion object {
        val COMP = VInt(3)
        val DEFAULTS = listOf(VInt(1), VInt(2))
    }

    var second = VInt(2)

    fun bar(): Boolean {
        second = second.inc()
        val b1 = first.v < second.v
        val b2 = second.v == COMP.v

        return b1 && b2
    }
}

fun box(): String {
    val n = Normal(VInt(2))
    assert(n.bar())

    assert(Normal.COMP.v == 3)

    return "OK"
}

