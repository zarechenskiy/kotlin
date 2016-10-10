// WITH_RUNTIME

// FILE: foo.kt

value class Name(val s: String) {
    operator fun box(): String = s
    operator fun unbox(boxed: String): Name = Name(boxed)
}

interface SomeT<T> {
    fun sample(ls: MutableList<T>): Int
}

open class SomeValue : SomeT<Name> {
    override fun sample(ls: MutableList<Name>): Int = 100
}

// FILE: A.java

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class A extends SomeValue {
    @Override
    public int sample(@NotNull List<String> ls) {
        return super.sample(ls);
    }
}

// FILE: 1.kt

fun box(): String {
    val a = A()
    val lsOfName = arrayListOf(Name("Kotlin"))

    val result = a.sample(lsOfName)
    assert(result == 100)

    return "OK"
}