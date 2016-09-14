// WITH_RUNTIME

// FILE: library.kt

value class Name(val s: String) {
    fun box(): Any {
        return s
    }

    fun unbox(boxed: Any): Name {
        return Name(boxed as String)
    }

    fun greeting(): String {
        return "Hello, $s"
    }
}

// FILE: foo.kt

fun box() {
    val name = Name("Kotlin")
    val ls = listOf(name)
    val t1 = ls[0]

    val g = t1.greeting()
}

// @FooKt.class:
// 1 INVOKESTATIC Name\$AnyfiedImpls.box \(Ljava/lang/String;\)Ljava/lang/Object;
// 1 INVOKESTATIC Name\$AnyfiedImpls.unbox \(Ljava/lang/Object;\)Ljava/lang/String;
// 1 INVOKESTATIC Name\$AnyfiedImpls.greeting \(Ljava/lang/String;\)Ljava/lang/String;
// 0 INVOKESPECIAL