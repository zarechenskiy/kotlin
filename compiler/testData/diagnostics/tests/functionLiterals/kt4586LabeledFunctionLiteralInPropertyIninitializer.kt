// !DIAGNOSTICS: -UNUSED_PARAMETER

//KT-4586 this@ does not work for builders
fun string(init: StringBuilder.() -> Unit) {
}

fun test() {
    string @l{
        append("hello, ")

        string {
            append("world!")
            this@l.append(this)  // does nothing !!
        }
    }
}