// !DIAGNOSTICS_NUMBER: 1
// !DIAGNOSTICS: VARARG_OUTSIDE_PARENTHESES

fun bar(vararg a: Any) {}

class TestBar {
    val x = bar()
    {}
}
