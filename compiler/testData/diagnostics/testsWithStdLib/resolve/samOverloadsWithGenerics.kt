// !DIAGNOSTICS: -UNUSED_PARAMETER
// !CHECK_TYPE

// FILE: Foo.java
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

class Foo {
    interface FObject<T> {
        void invoke(T i);
    }

    public String test(FObject<Integer> f) { return ""; }
    public int test(Function1<Integer, Unit> f) { return 1; }
}

// FILE: 1.kt
fun fn(i: Int) {}
fun bar() {
    Foo().test(::fn) checkType { _<Int>() }
    Foo().test {} checkType { _<Int>() }
}