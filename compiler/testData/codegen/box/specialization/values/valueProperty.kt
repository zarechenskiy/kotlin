// WITH_RUNTIME

value class VInt(val v: Int)
value class VLong(val l: Long)

fun box(): String {
    val first = VInt(10)
    val second = VLong(20)

    println("int: ${first.v}")
    println("long: ${second.l}")

    assert(first.v == 10) { "int failed" }
    assert(second.l == 20L) { "long failed" }

    return "OK"
}