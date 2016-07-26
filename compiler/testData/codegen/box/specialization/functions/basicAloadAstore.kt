// WITH_RUNTIME

inline fun <@Anyfied T> aloadAstore(t: T) {
    val t1 = t
}

fun box(): String {
    aloadAstore(1 as vInt)
    return "OK"
}

value class vInt(val v: Int)