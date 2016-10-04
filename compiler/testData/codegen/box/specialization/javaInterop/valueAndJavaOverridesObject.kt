// WITH_RUNTIME

// FILE: foo.kt

value class VLong(val v: Long) {
    operator fun box(): Long = v
    operator fun unbox(boxed: java.lang.Long): VLong = VLong(boxed.toLong())
}

interface A<T> {
    fun foo(): T
}

abstract class C : A<VLong>

// FILE: ExtendsC.java

import java.util.List;
import org.jetbrains.annotations.NotNull;

class ExtendsC extends C {
    void test() {
        long x = foo();
        Long y = foo();
        Object z = foo();
    }

    @Override
    @NotNull
    public Long foo() { return 52; }
}

// FILE: 1.kt

fun box(): String {
    val ec = ExtendsC()
    val fooEC: VLong = ec.foo()

    ec.test()

    assert(fooEC.v == 52L)

    return "OK"
}