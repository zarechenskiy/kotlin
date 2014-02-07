package templates

import java.util.ArrayList
import templates.Family.*
import java.util.HashSet
import java.util.HashMap
import java.io.StringReader
import java.util.StringTokenizer

enum class Family {
    Streams
    Iterables
    Collections
    Lists
    ArraysOfObjects
    ArraysOfPrimitives
}

class GenericFunction(val signature: String) : Comparable<GenericFunction> {
    val defaultFamilies = array(Iterables, Streams, ArraysOfObjects, ArraysOfPrimitives)

    var toNullableT: Boolean = false
    var makeInline: Boolean = false;
    private val customReceivers = HashMap<Family, String>()

    val buildFamilies = HashSet<Family>(defaultFamilies.toList())
    private val buildPrimitives = HashSet<PrimitiveType>(PrimitiveType.values().toList())

    var doc: String = ""
    val docs = HashMap<Family, String>()

    var defaultBody: String = ""
    val bodies = HashMap<Family, String>()

    var defaultReturnType = ""
    val returnTypes = HashMap<Family, String>()

    val typeParams = ArrayList<String>()

    fun body(vararg families: Family, b: () -> String) {
        if (families.isEmpty())
            defaultBody = b()
        else {
            for (f in families) {
                include(f)
                bodies[f] = b()
            }
        }
    }

    fun doc(vararg families: Family, b: () -> String) {
        if (families.isEmpty())
            doc = b()
        else {
            for (f in families) {
                docs[f] = b()
            }
        }
    }

    fun returns(vararg families: Family, b: () -> String) {
        if (families.isEmpty())
            defaultReturnType = b()
        else {
            for (f in families) {
                returnTypes[f] = b()
            }
        }
    }

    fun returns(r: String) {
        defaultReturnType = r
    }

    fun Family.customReceiver(r: String) {
        customReceivers[this] = r
    }

    fun typeParam(t: String) {
        typeParams.add(t)
    }

    fun exclude(vararg families: Family) {
        buildFamilies.removeAll(families.toList())
    }

    fun only(vararg families: Family) {
        buildFamilies.clear()
        buildFamilies.addAll(families.toList())
    }

    fun include(vararg families: Family) {
        buildFamilies.addAll(families.toList())
    }

    fun exclude(vararg p: PrimitiveType) {
        buildPrimitives.removeAll(p.toList())
    }

    fun include(vararg p: PrimitiveType) {
        buildPrimitives.addAll(p.toList())
    }


    fun build(vararg families: Family = Family.values()): String {
        val builder = StringBuilder()
        for (family in families.sortBy { it.name() }) {
            if (buildFamilies.contains(family))
                build(builder, family)
        }
        return builder.toString()
    }

    fun build(builder: StringBuilder, f: Family) {
        if (f == ArraysOfPrimitives) {
            for (primitive in buildPrimitives.sortBy { it.name() })
                build(builder, f, primitive)
        } else {
            build(builder, f, null)
        }
    }

    fun build(builder: StringBuilder, f: Family, primitive: PrimitiveType?) {
        val returnType = returnTypes[f] ?: defaultReturnType
        if (returnType.isEmpty())
            throw RuntimeException("No return type specified for $signature")

        val receiver = when (f) {
            Iterables -> "Iterable<T>"
            Collections -> "Collection<T>"
            Lists -> "List<T>"
            Streams -> "Stream<T>"
            ArraysOfObjects -> "Array<T>"
            ArraysOfPrimitives -> primitive?.let { it.name() + "Array" } ?: throw IllegalArgumentException("Primitive array should specify primitive type")
            else -> throw IllegalStateException("Invalid family")
        }

        fun String.renderType(): String {
            val t = StringTokenizer(this, " \t\n,:()<>?.", true)
            val answer = StringBuilder()

            while (t.hasMoreTokens()) {
                val token = t.nextToken()
                answer.append(when (token) {
                                  "SELF" -> receiver
                                  "PRIMITIVE" -> primitive?.name() ?: token
                                  "SUM" -> {
                                      when (primitive) {
                                          PrimitiveType.Byte, PrimitiveType.Short -> "Int"
                                          else -> primitive
                                      }
                                  }
                                  "ZERO" -> when (primitive) {
                                      PrimitiveType.Double -> "0.0"
                                      PrimitiveType.Float -> "0.0f"
                                      else -> "0"
                                  }
                                  "T" -> primitive?.name() ?: token
                                  else -> token
                              })
            }

            return answer.toString()
        }

        fun effectiveTypeParams(): List<String> {
            val types = ArrayList(typeParams)
            if (primitive == null) {
                if (typeParams.all { !it.startsWith("T") } && !customReceivers.containsKey(f)) {
                    types.add(0, "T")
                }
                return types
            } else {
                // primitive type arrays should drop constraints
                return typeParams.filter { !it.startsWith("T") }
            }
        }

        val methodDoc = docs[f] ?: doc
        if (methodDoc != "") {
            builder.append("/**\n")
            StringReader(methodDoc).forEachLine {
                val line = it.trim()
                if (!line.isEmpty()) {
                    builder.append(" * ").append(line).append("\n")
                }
            }
            builder.append(" */\n")
        }

        builder.append("public ")
        if (makeInline)
            builder.append("inline ")

        builder.append("fun ")

        val types = effectiveTypeParams()
        if (!types.isEmpty()) {
            builder.append(types.makeString(separator = ", ", prefix = "<", postfix = "> ").renderType())
        }

        val receiverType = (customReceivers[f] ?:
        if (toNullableT) {
            receiver.replace("T>", "T?>")
        } else {
            receiver
        }).renderType()

        builder.append(receiverType)
        builder.append(".${signature.renderType()} : ${returnType.renderType()} {")

        val body = (bodies[f] ?: defaultBody).trim("\n")
        val prefix: Int = body.takeWhile { it == ' ' }.length

        StringReader(body).forEachLine {
            builder.append('\n')
            var count = prefix
            builder.append("    ").append(it.dropWhile { count-- > 0 && it == ' ' } .renderType())
        }

        builder.append("\n}\n\n")
    }

    public override fun compareTo(other: GenericFunction): Int = this.signature.compareTo(other.signature)
}

fun String.trimTrailingSpaces(): String {
    var answer = this;
    while (answer.endsWith(' ') || answer.endsWith('\n')) answer = answer.substring(0, answer.length() - 1)
    return answer
}

fun f(signature: String, init: GenericFunction.() -> Unit): GenericFunction {
    val gf = GenericFunction(signature)
    gf.init()
    return gf
}
