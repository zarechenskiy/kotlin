// WITH_RUNTIME

inline fun <@Anyfied T> returnI(el: T): T {
    return el
}

fun test(): vInt {
    return returnI(1 as vInt)
}

fun box(): String {
    test()
    return "OK"
}

// 1 ALOAD
// 1 ILOAD
// 1 ARETURN
// 1 IRETURN
// 1 ISTORE
// 0 valueOf
// 0 intValue