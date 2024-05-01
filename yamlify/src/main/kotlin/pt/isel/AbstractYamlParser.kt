package pt.isel

import java.io.Reader
import kotlin.reflect.KClass

abstract class AbstractYamlParser<T : Any>(type: KClass<T>) : YamlParser<T> {

     private val typeReturn: (Any) -> T by lazy {
         when (type) {
            Int::class -> { it -> (it as String).toInt() as T }
            Char::class -> { it -> (it as String).first() as T }
            Boolean::class -> { it -> (it as String).toBoolean() as T }
            Long::class -> { it -> (it as String).toLong() as T }
            Short::class -> { it -> (it as String).toShort() as T }
            Byte::class -> { it -> (it as String).toByte() as T }
            Double::class -> { it -> (it as String).toDouble() as T }
            Float::class -> { it -> (it as String).toFloat() as T }
            String::class -> { it -> it as T }
            else -> { it -> newInstance(it as Map<String, Any>) }
        }
     }

    /**
     * Used to get a parser for another Type using this same parsing approach.
     */
    abstract fun <T : Any> yamlParser(type: KClass<T>): AbstractYamlParser<T>

    /**
     * Creates a new instance of T through the first constructor
     * that has all the mandatory parameters in the map and optional parameters for the rest.
     */
    abstract fun newInstance(args: Map<String, Any>): T

    final override fun parseObject(yaml: Reader): T {
        val argsMap = createArgsMap(yaml)
        return newInstance(argsMap)
    }

    private fun MutableMap<String, Any>.populateMap(
        elems: ListIterator<String>,
        currIdent: Int
        ) : Int {
        var key = ""
        var idx = 0
        while (elems.hasNext()) {
            val elem = elems.next()
            val ident = elem.indexOfFirst { it != ' ' }
            if (ident == currIdent) {
                val (name, value) = elem.trimIndent().splitIfExist(":", " ")
                if (value.isNotBlank()) {
                    if (name != "-") put(name, value.trim() as Any)
                    else put(name+idx++, value.trim() as Any)
                } else {
                    key =
                        if (name.trim() == "-")
                            name + idx++
                        else
                            name
                }
            }
            else if (ident > currIdent) {
                val (name, value) = elem.trimIndent().splitIfExist(":", " ")
                val auxMap = if (value.isNotBlank()) {
                    val newName = if (name.trim() != "-") name else name.trim() + idx++
                    mutableMapOf(newName to value.trim() as Any)
                } else {
                    if (name.trim() == "-") elems.previous()
                    mutableMapOf()
                }
                val retIdent = auxMap.populateMap(elems, ident)
                put(key, auxMap)
                if (retIdent < currIdent) return retIdent
            } else {
                elems.previous()
                return ident
            }
        }
        return 0
    }

    final override fun parseList(yaml: Reader): List<T> {
        val argsMap = createArgsMap(yaml)
        return argsMap.values.map(typeReturn)
    }

    private fun createArgsMap(yaml: Reader): MutableMap<String, Any> {
        val lines = yaml.readLines().filter { it.isNotBlank() }
        val firstIdent = lines.first().indexOfFirst { it != ' ' }
        val argsMap = mutableMapOf<String, Any>()
        argsMap.populateMap(lines.listIterator(), firstIdent)
        return argsMap
    }

    fun typeReturn(map: Map<String, Any>) : List<T> =
        map.values.map(typeReturn)
}

private fun String.splitIfExist(vararg delimiters: String): List<String> {
    val list = this.split(*delimiters, limit = 2)
    return if (list.size > 1) list else listOf(this, "")
}
