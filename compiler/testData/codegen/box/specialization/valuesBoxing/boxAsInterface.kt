// WITH_RUNTIME

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

fun box(): String {
    val v = NameImpl("some")
    return if (takeGreeter(v)) "OK" else "not ok"
}

fun takeGreeter(g: Greeter): Boolean {
    return g is Greeter
}