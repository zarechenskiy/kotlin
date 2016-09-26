// WITH_RUNTIME

// FILE: library.kt

value class Name(val s: String) {
    operator fun box(): String {
        return s
    }

    operator fun unbox(boxed: String): Name {
        return Name(boxed)
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
// 1 INVOKESTATIC Name\$AnyfiedImpls.box \(Ljava/lang/String;\)Ljava/lang/String;
// 1 INVOKESTATIC Name\$AnyfiedImpls.unbox \(Ljava/lang/String;\)Ljava/lang/String;
// 1 INVOKESTATIC Name\$AnyfiedImpls.greeting \(Ljava/lang/String;\)Ljava/lang/String;
// 0 INVOKESPECIAL