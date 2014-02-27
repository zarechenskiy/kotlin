// !DIAGNOSTICS: -UNUSED_VARIABLE
package o

class TestFunctionLiteral {
    val sum: (Int)->Int = { (x: Int) ->
        sum(x - 1) + x
    }
}

open class A(val a: A)

class TestObjectLiteral {
    val obj: A = object: A(<!UNINITIALIZED_VARIABLE!>obj<!>) {
        {
            val x = <!UNINITIALIZED_VARIABLE!>obj<!>
        }
        fun foo() {
            val y = obj
        }
    }
}

class TestOther {
    val x: Int = <!UNINITIALIZED_VARIABLE!>x<!> + 1
}
