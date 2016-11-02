// WITH_RUNTIME

// FILE: foo.kt

value class GenericValue<T>(val ls: List<T>)

interface A<T> {
    fun foo(g: GenericValue<T>)
    fun bar(g: GenericValue<String>)
    fun baz(g: GenericValue<*>)
}

// FILE: B.java

import java.util.List;
import org.jetbrains.annotations.NotNull;

class B implements A<Long> {
    @Override
    public void foo(@NotNull List<? extends Long> g) {}

    @Override
    public void bar(@NotNull List<String> g) {}

    @Override
    public void baz(@NotNull List<?> g) {}
}

// FILE: GenericB.java

import java.util.List;
import org.jetbrains.annotations.NotNull;

class GenericB<T> implements A<T> {
    @Override
    public void foo(@NotNull List<? extends T> g) {}

    @Override
    public void bar(@NotNull List<String> g) {}

    @Override
    public void baz(@NotNull List<?> g) {}
}

// FILE: baz.kt

fun box(): String {
    val b = B()

    b.foo(GenericValue(listOf(1L)))
    b.bar(GenericValue(listOf("Kotlin")))
    b.baz(GenericValue(listOf(1)))

    val genericB = GenericB<Long>()

    genericB.foo(GenericValue(listOf(1L)))
    genericB.bar(GenericValue(listOf("Kotlin")))
    genericB.baz(GenericValue(listOf(1)))

    return "OK"
}