// WITH_RUNTIME

// FILE: library.kt

interface Greeter {
    fun greeting(): String
}

value class NameImpl(val s: String) : Greeter {
    operator fun box(): BoxedGreeter {
        return BoxedGreeter(s)
    }

    operator fun unbox(boxed: BoxedGreeter): NameImpl {
        return NameImpl(boxed.s)
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
// 1 INVOKESTATIC NameImpl\$AnyfiedImpls.box \(Ljava/lang/String;\)LBoxedGreeter;
// 1 INVOKESTATIC FooKt.takeGreeter \(LGreeter;\)V
// 0 INVOKESPECIAL