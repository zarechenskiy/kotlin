//KT-1736 AssertionError in CallResolver

package kt1736

object Obj {
    fun method() {
    }
}

val x = Obj.method<!TOO_MANY_ARGUMENTS_POSSIBLY_DANGLING_FUNCTION_LITERAL, DANGLING_FUNCTION_LITERAL_ARGUMENT_SUSPECTED!>{ -> }<!>