// WITH_RUNTIME

value class Name(val s: String) {
    operator fun box(): String = s
    operator fun unbox(boxed: String): Name = Name(boxed)
}

value class NameCustomBox(val s: String) : Greeter {
    operator fun box(): BoxedGreeter = BoxedGreeter(s)
    operator fun unbox(boxed: BoxedGreeter): NameCustomBox = NameCustomBox(boxed.s)
    override fun greeting(): String = ""
}

interface Greeter {
    fun greeting(): String
}

class BoxedGreeter(val s: String) : Greeter {
    override fun greeting(): String = ""
}

fun <T> takeVarargs(vararg elements: T) {

}

fun box(): String {
    val n1 = Name("K")
    val n2 = Name("o")
    takeVarargs(Name("K"), Name("o"))
    takeVarargs(n1, n2)

    val c1 = NameCustomBox("K")
    val c2 = NameCustomBox("o")

    takeVarargs(NameCustomBox("K"), NameCustomBox("o"))
    takeVarargs(c1, c2)

    val ls1 = listOf(n1, n2)
    assert(ls1[0].s == "K")

    val ls2 = listOf(Name("K"), Name("o"))
    assert(ls2[1].s == "o")

    val ls3 = listOf(NameCustomBox("K"), NameCustomBox("o"))
    assert(ls3[0].s == "K")

    val ls4 = listOf(c1, c2)
    assert(ls4[1].s == "o")

    return "OK"
}

