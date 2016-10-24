// WITH_RUNTIME

value class VInt(val v: Int) {
    val square: Int
        get() = v * v

    val isSpecial: Boolean
        get() {
            val c = square
            return v + v >= c
        }

    val inc: VInt
        get() = VInt(plusOne)

    private val plusOne: Int
        get() = v + 1
}

fun box(): String {
    val v1 = VInt(10)
    val v2 = VInt(1)

    assert(v1.square == 100)
    assert(!v1.isSpecial)
    assert(v1.inc.v == 11)

    assert(v2.isSpecial)

    return "OK"
}