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

fun box(a: A, lsOfName: List<Name>, lsOfString: List<String>) {
    a.sample(lsOfName)
    a.sample(<!TYPE_MISMATCH!>lsOfString<!>)
}