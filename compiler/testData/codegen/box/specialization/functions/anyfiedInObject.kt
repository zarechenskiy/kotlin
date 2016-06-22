// WITH_RUNTIME

interface WithOne<T> {
    fun test(): T
}

inline fun <@Anyfied T> testAnyfied(el: T): WithOne<T> {
    val k = el
    return object : WithOne<T> {
        override fun test(): T {
            return k
        }
    }
}

fun test(v: vInt) {
    val testAnyfied = testAnyfied(v)
    testAnyfied.test()
}

fun box(): String {
    test(1 as vInt)
    return "OK"
}