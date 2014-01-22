package kotlin

annotation class Ann(val i: IntArray)

Ann(intArray(<!ANNOTATION_PARAMETER_MUST_BE_CONST!>i<!>))
Ann(intArray(i2))
Ann(intArray(<!ANNOTATION_PARAMETER_MUST_BE_CONST!>i3<!>))
Ann(intArray(<!ANNOTATION_PARAMETER_MUST_BE_CONST!>i<!>, i2, <!ANNOTATION_PARAMETER_MUST_BE_CONST!>i3<!>))
Ann(intArray(<!TYPE_MISMATCH!>intArray(i, i2, i3)<!>))
class Test

var i = 1
val i2 = 1
val i3 = foo()

fun foo(): Int = 1

fun intArray(vararg content : Int) : IntArray = content