fun foo() {}
fun foo(<warning>x</warning> : Int) {}
fun bar() {
    <error>foo</error>(4)

    <warning>{}</warning>
}

class C {
    val f = <error>foo</error>(4)

    <warning>{}</warning>
}
