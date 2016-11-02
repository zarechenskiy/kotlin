// WITH_RUNTIME

interface Foo {
    fun foo(): Int
    fun bar(): String
}

value class FooHolder(val f: Foo) : Foo by f

class FooImpl : Foo {
    override fun foo(): Int = 10
    override fun bar(): String = "bar"
}

fun box(): String {
    val f: Foo = FooImpl()
    val holder = FooHolder(f)

    assert(holder.foo() == 10)
    assert(holder.bar() == "bar")

    return "OK"
}