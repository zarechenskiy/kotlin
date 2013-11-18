// "Add semicolon after invocation of 'foo'" "true"
fun foo() {}
fun bar() {
    foo();

    {}<caret>
}
