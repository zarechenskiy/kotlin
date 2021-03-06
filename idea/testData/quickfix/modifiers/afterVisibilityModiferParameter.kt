// "Change visibility modifier" "true"
trait ParseResult<out T> {
    public val success : Boolean
    public val value : T
}

class Success<T>(<caret>public override val value : T) : ParseResult<T> {
    public override val success : Boolean = true
}