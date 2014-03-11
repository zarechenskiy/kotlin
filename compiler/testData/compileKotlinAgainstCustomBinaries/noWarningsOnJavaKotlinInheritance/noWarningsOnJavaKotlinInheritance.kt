class K : A {
    override fun foo(x: Int) {}
}

trait L : B

class M : L {
    override fun bar(string: String?, double: Double) { }
}



class N : C {
    override fun foo(overriddenName: String) {}
}
