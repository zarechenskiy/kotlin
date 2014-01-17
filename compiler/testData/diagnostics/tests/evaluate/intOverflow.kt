val intMaxValue: Int = 0x7fffffff
val intMinValue: Int = 1 shl 31

val a3: Int = <!INTEGER_OVERFLOW!><!INTEGER_OVERFLOW!>intMaxValue + 1<!> - 10<!>
val a4: Int = <!INTEGER_OVERFLOW!>intMaxValue + 1<!> + 10
val i2: Int = <!INTEGER_OVERFLOW!>intMaxValue - 1 + 2<!>
val i3: Int = <!INTEGER_OVERFLOW!>intMaxValue - intMinValue<!>
val i4: Int = <!INTEGER_OVERFLOW!>-intMinValue<!>
val i5: Int = <!INTEGER_OVERFLOW!>intMinValue - 1<!>
val i6: Int = <!INTEGER_OVERFLOW!>intMinValue - intMaxValue<!>
val i7: Int = intMinValue + intMaxValue
val i8: Int = -intMaxValue
val i10: Int = <!INTEGER_OVERFLOW!>intMinValue * -1<!>
val i11: Int = <!INTEGER_OVERFLOW!>intMinValue * 2<!>
val i12: Int = <!INTEGER_OVERFLOW!>intMaxValue * -2<!>
val i13: Int = intMaxValue * -1
val i15: Int = <!INTEGER_OVERFLOW!>intMinValue / -1<!>
val l20: Int = <!INTEGER_OVERFLOW!>30 * 24 * 60 * 60 * 1000<!>
val l21: Int = intMinValue - intMinValue
val l22: Int = <!INTEGER_OVERFLOW!>intMinValue + <!INTEGER_OVERFLOW!>-intMinValue<!><!>
val l23: Int = intMaxValue + <!INTEGER_OVERFLOW!>-intMinValue<!>

fun foo() {
    val a3: Int = <!INTEGER_OVERFLOW!><!INTEGER_OVERFLOW!>intMaxValue + 1<!> - 10<!>
    val a4: Int = <!INTEGER_OVERFLOW!>intMaxValue + 1<!> + 10
    val i2: Int = <!INTEGER_OVERFLOW!>intMaxValue - 1 + 2<!>
    val i3: Int = <!INTEGER_OVERFLOW!>intMaxValue - intMinValue<!>
    val i4: Int = <!INTEGER_OVERFLOW!>-intMinValue<!>
    val i5: Int = <!INTEGER_OVERFLOW!>intMinValue - 1<!>
    val i6: Int = <!INTEGER_OVERFLOW!>intMinValue - intMaxValue<!>
    val i7: Int = intMinValue + intMaxValue
    val i8: Int = -intMaxValue
    val i10: Int = <!INTEGER_OVERFLOW!>intMinValue * -1<!>
    val i11: Int = <!INTEGER_OVERFLOW!>intMinValue * 2<!>
    val i12: Int = <!INTEGER_OVERFLOW!>intMaxValue * -2<!>
    val i13: Int = intMaxValue * -1
    val i15: Int = <!INTEGER_OVERFLOW!>intMinValue / -1<!>
    val l20: Int = <!INTEGER_OVERFLOW!>30 * 24 * 60 * 60 * 1000<!>
    val l21: Int = intMinValue - intMinValue
    val l22: Int = <!INTEGER_OVERFLOW!>intMinValue + <!INTEGER_OVERFLOW!>-intMinValue<!><!>
    val l23: Int = intMaxValue + <!INTEGER_OVERFLOW!>-intMinValue<!>

    val <!UNUSED_VARIABLE!>a<!> = "$a3$a4$i2$i3$i4$i5$i6$i7$i8$i10$i11$i12$i13$i15$l20$l21$l22$l23"
}

class A {
    fun foo() {
        val a3: Int = <!INTEGER_OVERFLOW!><!INTEGER_OVERFLOW!>intMaxValue + 1<!> - 10<!>
        val a4: Int = <!INTEGER_OVERFLOW!>intMaxValue + 1<!> + 10
        val i2: Int = <!INTEGER_OVERFLOW!>intMaxValue - 1 + 2<!>
        val i3: Int = <!INTEGER_OVERFLOW!>intMaxValue - intMinValue<!>
        val i4: Int = <!INTEGER_OVERFLOW!>-intMinValue<!>
        val i5: Int = <!INTEGER_OVERFLOW!>intMinValue - 1<!>
        val i6: Int = <!INTEGER_OVERFLOW!>intMinValue - intMaxValue<!>
        val i7: Int = intMinValue + intMaxValue
        val i8: Int = -intMaxValue
        val i10: Int = <!INTEGER_OVERFLOW!>intMinValue * -1<!>
        val i11: Int = <!INTEGER_OVERFLOW!>intMinValue * 2<!>
        val i12: Int = <!INTEGER_OVERFLOW!>intMaxValue * -2<!>
        val i13: Int = intMaxValue * -1
        val i15: Int = <!INTEGER_OVERFLOW!>intMinValue / -1<!>
        val l20: Int = <!INTEGER_OVERFLOW!>30 * 24 * 60 * 60 * 1000<!>
        val l21: Int = intMinValue - intMinValue
        val l22: Int = <!INTEGER_OVERFLOW!>intMinValue + <!INTEGER_OVERFLOW!>-intMinValue<!><!>
        val l23: Int = intMaxValue + <!INTEGER_OVERFLOW!>-intMinValue<!>

        val <!UNUSED_VARIABLE!>a<!> = "$a3$a4$i2$i3$i4$i5$i6$i7$i8$i10$i11$i12$i13$i15$l20$l21$l22$l23"
    }
}