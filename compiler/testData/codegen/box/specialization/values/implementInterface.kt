// WITH_RUNTIME

interface ISample {
    fun two(x: Int, y: String): String
}

value class VTest(val v: Int) : ISample, Comparable<VTest> {
    override fun two(x: Int, y: String): String {
        return if (v < x) y else "!$y!"
    }

    override fun compareTo(other: VTest): Int {
        return v.compareTo(other.v)
    }
}

fun box(): String {
    val less = VTest(10) < VTest(20)

    val l1 = VTest(10).two(20, "something")
    val l2 = VTest(1).two(0, "something")

    assert(less)
    assert(l1 == "something")
    assert(l2 == "!something!")

    return "OK"
}