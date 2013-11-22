fun box(): String {
    fun foo(): Any {
        return {}
    }

    val enclosingMethod = foo().javaClass.getEnclosingMethod()
    if (enclosingMethod?.getName() != "box") return "method: $enclosingMethod"

    val enclosingClass = foo().javaClass.getEnclosingClass()
    if (!enclosingClass!!.getName().startsWith("_DefaultPackage-lambdaInFunction-")) return "enclosing class: $enclosingClass"

    val declaringClass = foo().javaClass.getDeclaringClass()
    if (declaringClass != null) return "anonymous function has a declaring class: $declaringClass"

    return "OK"
}