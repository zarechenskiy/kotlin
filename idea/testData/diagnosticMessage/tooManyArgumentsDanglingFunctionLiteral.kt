// !DIAGNOSTICS_NUMBER: 1
// !DIAGNOSTICS: TOO_MANY_ARGUMENTS_POSSIBLY_DANGLING_FUNCTION_LITERAL
// see org.jetbrains.jet.plugin.highlighter.DiagnosticMessageTest

fun Function1<Int, Int>.noInlineExt(p: Int) {}

fun Function1<Int, Int>.inlineExt2(p: Int) {

}

fun inlineExt2() {
    {(i: Int) -> 1}.inlineExt2(1) //if add ; no error
    {(i: Int) -> 2}.noInlineExt(1) //Kotlin: Too many arguments for internal fun (jet.Int) -> jet.Int.inlineExt2(p: jet.Int): jet.Unit defined in root package
}

fun main(args: Array<String>) {
    inlineExt2 ()
}