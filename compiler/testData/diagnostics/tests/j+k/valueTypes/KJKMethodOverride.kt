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

class KOverride1 : A() {
    override fun sample(ls: List<Name>): Int {
        return super.sample(ls)
    }
}

class KOverride2 : A() {
    <!NOTHING_TO_OVERRIDE!>override<!> fun sample(ls: List<String>): Int {
        return 200
    }
}

fun box(k: KOverride1, lsOfName: List<Name>, lsOfString: List<String>) {
    k.sample(lsOfName)
    k.sample(<!TYPE_MISMATCH!>lsOfString<!>)
}