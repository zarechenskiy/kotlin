// !DIAGNOSTICS: -UNUSED_PARAMETER

<info descr="null">value</info> class <error descr="[VALUE_CLASS_WRONG_PARAMETERS_SIZE] Value class must have exactly one primary constructor parameter">WithoutPrimaryConstructor</error>

<info descr="null">value</info> class NonProperty(<error descr="[VALUE_CLASS_NOT_PROPERTY_PARAMETER] Value class primary constructor must have only property (val) parameter"><warning descr="[UNUSED_PARAMETER] Parameter 'v' is never used">v</warning>: Int</error>)

<info descr="null">value</info> class MutableProperty(<error descr="[VALUE_CLASS_NOT_IMMUTABLE_PARAMETER] Value class primary constructor must have only immutable (val) parameter">var v: Int</error>)

<info descr="null">value</info> class SeveralProperties<error descr="[VALUE_CLASS_WRONG_PARAMETERS_SIZE] Value class must have exactly one primary constructor parameter">(val v1: Int, val v2: Int)</error>

class InClass {
    <error descr="[VALUE_CLASS_NOT_TOP_LEVEL_OR_OBJECT] Value classes are only allowed on top level or in objects"><info descr="null">value</info> class VClass(val v1: Int)</error>
}

<info descr="null">value</info> class VClass(val v1: Int)