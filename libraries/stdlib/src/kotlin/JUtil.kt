package kotlin

import java.util.*

/** Returns a new read-only list of given elements */
public fun listOf<T>(vararg values: T): List<T> = arrayListOf(*values)

/** Returns a new read-only map of given pairs, where the first value is the key, and the second is value */
public fun mapOf<K, V>(vararg values: Pair<K, V>): Map<K, V> = hashMapOf(*values)

/** Returns a new ArrayList with a variable number of initial elements */
public fun arrayListOf<T>(vararg values: T) : ArrayList<T> = values.toCollection(ArrayList<T>(values.size))

deprecated("Use listOf(...) or arrayListOf(...) instead")
public fun arrayList<T>(vararg values: T) : ArrayList<T> = arrayListOf(*values)

/** Returns a new HashSet with a variable number of initial elements */
public fun hashSetOf<T>(vararg values: T) : HashSet<T> = values.toCollection(HashSet<T>(values.size))

deprecated("Use setOf(...) or hashSetOf(...) instead")
public fun hashSet<T>(vararg values: T) : HashSet<T> = hashSetOf(*values)

/**
 * Returns a new [[HashMap]] populated with the given pairs where the first value in each pair
 * is the key and the second value is the value
 *
 * @includeFunctionBody ../../test/MapTest.kt createUsingPairs
 */
public fun <K,V> hashMapOf(vararg values: Pair<K,V>): HashMap<K,V> {
    val answer = HashMap<K,V>(values.size)
    answer.putAll(*values)
    return answer
}

deprecated("Use mapOf(...) or hashMapOf(...) instead")
public fun <K,V> hashMap(vararg values: Pair<K,V>): HashMap<K,V> = hashMapOf(*values)

/** Returns the size of the collection */
public val Collection<*>.size : Int
    get() = size()

/** Returns true if this collection is empty */
public val Collection<*>.empty : Boolean
    get() = isEmpty()

public val Collection<*>.indices : IntRange
    get() = 0..size-1

public val Int.indices: IntRange
    get() = 0..this-1

/** Returns true if the collection is not empty */
public fun <T> Collection<T>.isNotEmpty() : Boolean = !this.isEmpty()

/** Returns true if this collection is not empty */
val Collection<*>.notEmpty : Boolean
    get() = isNotEmpty()

/** Returns the Collection if its not null otherwise it returns the empty list */
public fun <T> Collection<T>?.orEmpty() : Collection<T> = this ?: Collections.emptyList<T>()

// List APIs

/** Returns the List if its not null otherwise returns the empty list */
public fun <T> List<T>?.orEmpty() : List<T> = this ?: Collections.emptyList<T>()

/**
  TODO figure out necessary variance/generics ninja stuff... :)
public inline fun <in T> List<T>.sort(transform: fun(T) : java.lang.Comparable<*>) : List<T> {
  val comparator = java.util.Comparator<T>() {
    public fun compare(o1: T, o2: T): Int {
      val v1 = transform(o1)
      val v2 = transform(o2)
      if (v1 == v2) {
        return 0
      } else {
        return v1.compareTo(v2)
      }
    }
  }
  answer.sort(comparator)
}
*/

/**
 * Returns the first item in the list or null if the list is empty
 *
 * @includeFunctionBody ../../test/ListTest.kt first
 */
val <T> List<T>.first : T?
    get() = this.head


/**
 * Returns the last item in the list or null if the list is empty
 *
 * @includeFunctionBody ../../test/ListTest.kt last
 */
val <T> List<T>.last : T?
    get() {
      val s = this.size
      return if (s > 0) this.get(s - 1) else null
    }

/**
 * Returns the index of the last item in the list or -1 if the list is empty
 *
 * @includeFunctionBody ../../test/ListTest.kt lastIndex
 */
val <T> List<T>.lastIndex : Int
    get() = this.size - 1

/**
 * Returns the first item in the list or null if the list is empty
 *
 * @includeFunctionBody ../../test/ListTest.kt head
 */
val <T> List<T>.head : T?
    get() = if (this.isNotEmpty()) this.get(0) else null

/**
 * Returns all elements in this collection apart from the first one
 *
 * @includeFunctionBody ../../test/ListTest.kt tail
 */
val <T> List<T>.tail : List<T>
    get() {
        return this.drop(1)
    }
