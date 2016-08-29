// WITH_RUNTIME

// FILE: value.kt

value class VInt(val v: Int)
value class VLong(val l: Long)

// FILE: foo.kt

fun foo() {
    val a = VInt(10).v
    val b = VInt(10)
    val c = b.v

    val d = VLong(0).l
    val e = VLong(1)
    val f = e.l

    println(c)
    println(f)
}

// @FooKt.class:
// 0 ALOAD
// 0 ARETURN
// 0 ASTORE
// 0 valueOf
// 1 INVOKEVIRTUAL java/io/PrintStream.println \(I\)V
// 1 INVOKEVIRTUAL java/io/PrintStream.println \(J\)V