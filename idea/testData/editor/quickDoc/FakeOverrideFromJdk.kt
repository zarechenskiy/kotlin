import java.util.HashSet

fun main() {
    val c = HashSet<Any>()
    c.<caret>addAll(HashSet<Any>())
}

// INFO: <b>public</b> <b>open</b> <b>fun</b> addAll(c: jet.Collection&lt;jet.Any&gt;): jet.Boolean <i>defined in</i> java.util.HashSet<br/>Java declaration:<br/>java.util.AbstractCollection public boolean addAll (java.util.Collection&lt;? extends E&gt; )