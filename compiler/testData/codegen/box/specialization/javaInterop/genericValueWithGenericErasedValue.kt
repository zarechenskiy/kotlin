// WITH_RUNTIME

// FILE: foo.kt

value class GenericValue<T>(val ls: List<T>)
value class HoldGeneric<T>(val g: GenericValue<T>)

interface A<T> {
    fun foo(h: HoldGeneric<T>)
    fun bar(h: HoldGeneric<String>)
}

// FILE: B.java

import org.jetbrains.annotations.NotNull;
import java.util.List;

class B implements A<Long> {
    @Override
    public void foo(@NotNull List<? extends Long> h) {}

    @Override
    public void bar(@NotNull List<String> h) {}
}

// FILE: baz.kt

fun box(): String {
    val b = B()

    b.foo(HoldGeneric(GenericValue(listOf(1L))))
    b.bar(HoldGeneric(GenericValue(listOf("Str"))))

    return "OK"
}