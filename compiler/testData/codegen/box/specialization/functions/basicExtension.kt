// WITH_RUNTIME

inline fun <@Anyfied T> T.basicExtension() {
}

fun box(): String {
    (1 as vInt).basicExtension()
    return "OK"
}

// 0 ALOAD
// 0 ILOAD
// 0 valueOf
// 0 intValue
// 1 ISTORE