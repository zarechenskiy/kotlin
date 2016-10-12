value class VInt(val v: Int) {
    operator fun box(): Int = v
    operator fun unbox(boxed: Int): VInt = VInt(boxed)
}

interface A {
    fun foo(): VInt?
}

class B : A {
    override fun foo(): VInt {
        return VInt(123)
    }
}