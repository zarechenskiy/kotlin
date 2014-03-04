package kotlin

import java.io.Serializable

private fun Any?.safeHashCode() : Int = if (this == null) 0 else this.hashCode()

/** Represents generic data structure holding three values and exposing value semantics */
public data class Pair<out A, out B> (
        public val first: A,
        public val second: B
) : Serializable {
    override fun toString(): String = "($first, $second)"

    override fun hashCode(): Int {
        var result = first.safeHashCode()
        result = 31 * result + second.safeHashCode()
        return result;
    }

    override fun equals(o: Any?): Boolean {
        if (this identityEquals o) return true;
        if (o == null || this.javaClass != o.javaClass) return false;

        val t = o as Pair<*, *>
        return first == t.first && second == t.second;
    }
}

/** Represents generic data structure holding three values and exposing value semantics */
public data class Triple<out A, out B, out C> (
        public val first: A,
        public val second: B,
        public val third: C
) : Serializable {
    override fun toString(): String = "($first, $second, $third)"

    override fun hashCode(): Int {
        var result = first.safeHashCode()
        result = 31 * result + second.safeHashCode()
        result = 31 * result + third.safeHashCode()
        return result;
    }

    override fun equals(o: Any?): Boolean {
        if (this identityEquals o) return true;
        if (o == null || this.javaClass != o.javaClass) return false;

        val t = o as Triple<*, *, *>
        return first == t.first &&
               second == t.second &&
               third == t.third;
    }
}


/**
 * Creates a tuple of type [[Pair<A,B>]] from this and *that* which can be useful for creating [[Map]] literals
 * with less noise, for example

 * @includeFunctionBody ../../test/MapTest.kt createUsingTo
 */
public fun <A,B> A.to(that: B): Pair<A, B> = Pair(this, that)
