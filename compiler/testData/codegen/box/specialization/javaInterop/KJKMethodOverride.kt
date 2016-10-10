// WITH_RUNTIME

// FILE: foo.kt

value class Name(val s: String) {
    operator fun box(): String = s
    operator fun unbox(boxed: String): Name = Name(boxed)
}

interface SomeK {
    fun sample(ls: List<Name>): Int
}

// FILE: A.java

import java.util.List;
import org.jetbrains.annotations.NotNull;

public class A implements SomeK {
    @Override
    public int sample(@NotNull List<String> ls) {
        return 100;
    }
}

// FILE: 1.kt

class KOverride : A() {
    override fun sample(ls: List<Name>): Int {
        return 200
    }
}

fun box(): String {
    val a = A()
    val lsOfName = listOf(Name("Kotlin"))

    val result = a.sample(lsOfName)
    assert(result == 100)

    val k = KOverride()
    assert(k.sample(lsOfName) == 200)

    return "OK"
}