// WITH_RUNTIME

value class Name(val s: String) {
    fun box(): Any {
        return s
    }

    fun unbox(boxed: String): Name {
        return Name(boxed)
    }

    fun greeting(): String {
        return "Hello, $s"
    }
}

fun box(): String {
    val v = Name("Kotlin")

    val ls = listOf(v)
    val t1 = ls[0]

    val g = t1.greeting()

    assert(g == "Hello, Kotlin")

    return "OK"
}