interface Greeter {
}

value class NameCustomBox(val s: String) : Greeter {
    operator fun box(): BoxedGreeter = BoxedGreeter(s)
    operator fun unbox(boxed: BoxedGreeter): NameCustomBox = NameCustomBox(boxed.s)
}

class BoxedGreeter(val s: String) : Greeter

interface A {
    fun foo(): NameCustomBox?
}

class B : A {
    override fun foo(): NameCustomBox {
        return NameCustomBox("kotlin")
    }
}

fun box(): String {
    val b = B()
    val c = b.foo()

    return if (c.s == "kotlin") "OK" else "Fail"
}