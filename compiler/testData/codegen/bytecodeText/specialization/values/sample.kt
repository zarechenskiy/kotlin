// WITH_RUNTIME

// FILE: value.kt

value class vClass(val v: Int)

// FILE: foo.kt

fun foo() {
    val v = vClass(1)
    takeValue(v)
}

fun takeValue(v: vClass): vClass {
    return v
}

// @FooKt.class:
// 1 public final static takeValue\(I\)I
// 0 ALOAD
// 0 ARETURN
// 0 ASTORE
// 2 ILOAD
// 1 IRETURN
// 1 ISTORE
// 0 valueOf