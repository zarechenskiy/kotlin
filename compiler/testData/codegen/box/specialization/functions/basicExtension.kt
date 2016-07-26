// WITH_RUNTIME

inline fun <@Anyfied T> T.basicExtension() {
}

fun box(): String {
    (1 as vInt).basicExtension()
    return "OK"
}

value class vInt(val v: Int)