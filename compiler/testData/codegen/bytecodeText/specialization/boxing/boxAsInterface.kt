// WITH_RUNTIME

// FILE: library.kt

interface Greeter {
    fun greeting(): String
}

value class NameImpl(val s: String) : Greeter {
    fun box(): Any {
        return BoxedGreeter(s)
    }

    fun unbox(boxed: Any): Name {
        return Name((boxed as BoxedGreeter).s)
    }

    override fun greeting(): String {
        return ""
    }
}

class BoxedGreeter(val s: String) : Greeter {
    override fun greeting(): String {
        TODO("not implemented")
    }
}

// FILE: foo.kt

fun box() {
    val v = NameImpl("some")
    takeGreeter(v)
}

fun takeGreeter(g: Greeter) {}

// @FooKt.class:
// 1 INVOKESTATIC NameImpl\$AnyfiedImpls.box \(Ljava/lang/String;\)Ljava/lang/Object;
// 1 INVOKESTATIC FooKt.greeter \(LGreeter;\)V
// 0 INVOKESPECIAL