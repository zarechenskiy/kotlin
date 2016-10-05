// WITH_RUNTIME

// FILE: foo.kt

value class VLong(val v: Long) {
    operator fun box(): Long = v
    operator fun unbox(boxed: Long): VLong = VLong(boxed)
}

interface A<T> {
    fun foo(): T
}

open class B : A<VLong> {
    override fun foo(): VLong = VLong(42)
}

abstract class C : A<VLong>

// FILE: ExtendsB.java

import java.util.List;
import org.jetbrains.annotations.NotNull;

class ExtendsB extends B {
    void test() {
        long x = foo();
        Long y = foo();
        Object z = foo();
    }
}

// FILE: 1.kt

fun box(): String {
    val fooB = B().foo()
    val extendsB = ExtendsB()
    val fooEB: VLong = extendsB.foo()

    extendsB.test()

    assert(fooB.v == 42L)
    assert(fooEB.v == 42L)

    return "OK"
}