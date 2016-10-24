// WITH_RUNTIME

value class HoldString(val s: String) {
    companion object {
        fun foo(v: Int, s: String): String {
            return v.toString() + s
        }
    }
}

fun box(): String {
    val h = HoldString("Kotlin")

    assert(HoldString.foo(10, h.s) == "10Kotlin")

    return "OK"
}