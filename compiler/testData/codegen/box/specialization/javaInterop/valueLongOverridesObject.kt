// WITH_RUNTIME

// FILE: foo.kt

value class VLong(val v: Long) {
    operator fun box(): Long = v
    operator fun unbox(boxed: java.lang.Long): VLong = VLong(boxed.toLong())
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

//public class ExtendsC extends C {
//    void test() {
//        int x = foo();
//        Integer y = foo();
//        Object z = foo();
//    }
//
//    @Override
//    @NotNull
//    public Integer foo() { return 52; }
//}

// FILE: 1.kt

fun box(): String {
    val fooB = B().foo()
    val eb = ExtendsB()
    val fooEB = eb.foo()

    eb.test()

//    val ec = ExtendsC()
//    val fooEC: VLong = ec.foo()
//
//    ec.test()

    assert(fooB.v == 42L)
    assert(fooEB.v == 42L)
//    assert(fooEC.v == 52L)

    return "OK"
}