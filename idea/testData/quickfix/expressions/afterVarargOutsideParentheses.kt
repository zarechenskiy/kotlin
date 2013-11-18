// "Add semicolon after invocation of 'bar'" "true"

fun bar(vararg a: Any) {}

class TestBar {
    val x = bar();
            {}
}
