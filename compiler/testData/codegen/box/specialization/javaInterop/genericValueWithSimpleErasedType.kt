// WITH_RUNTIME

// FILE: foo.kt

value class GenericValue<T>(val v: Int)

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
    public void foo(int g) {}

    @Override
    public void bar(int g) {}

    @Override
    public void baz(int g) {}
}

// FILE: GenericB.java

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class GenericB<T> implements A<T> {
    @Override
    public void foo(int g) {}

    @Override
    public void bar(int g) {}

    @Override
    public void baz(int g) {}
}

// FILE: baz.kt

fun box(): String {
    val b = B()

    b.foo(GenericValue<Long>(1))
    b.bar(GenericValue<String>(2))
    b.baz(GenericValue<Any>(3))

    val genericB = GenericB<Long>()

    b.foo(GenericValue<Long>(1))
    b.bar(GenericValue<String>(2))
    b.baz(GenericValue<Any>(3))

    return "OK"
}