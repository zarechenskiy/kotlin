package a

fun foo<R> (f: ()->R, r: MutableList<R>) = r.add(f())
fun bar<R> (r: MutableList<R>, f: ()->R) = r.add(f())

fun test() {
    val <!UNUSED_VARIABLE!>a<!> = <!TYPE_INFERENCE_CONFLICTING_SUBSTITUTIONS!>foo<!>({1}, arrayListOf("")) //no type inference error on 'arrayListOf'
    val <!UNUSED_VARIABLE!>b<!> = <!TYPE_INFERENCE_CONFLICTING_SUBSTITUTIONS!>bar<!>(arrayListOf(""), {1})
}

// from standard library
fun arrayListOf<T>(vararg <!UNUSED_PARAMETER!>values<!>: T) : MutableList<T> {<!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>