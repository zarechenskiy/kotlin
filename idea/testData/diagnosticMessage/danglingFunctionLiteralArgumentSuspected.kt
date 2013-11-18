// !DIAGNOSTICS_NUMBER: 1
// !DIAGNOSTICS: DANGLING_FUNCTION_LITERAL_ARGUMENT_SUSPECTED

fun foo() {}
fun foo(i: Int) {}
class C {
    val f = foo()

    {

    }
}