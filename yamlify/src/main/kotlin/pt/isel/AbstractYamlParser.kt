package pt.isel

import java.io.Reader
import kotlin.reflect.KClass

abstract class AbstractYamlParser<T : Any>(type: KClass<T>) : YamlParser<T> {

     @Suppress("UNCHECKED_CAST")
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

    private val isPrimitive by lazy { type in primitiveType }

    companion object {
        val primitiveType =
            arrayOf(
                Int::class,
                Char::class,
                Boolean::class,
                Long::class,
                Short::class,
                Byte::class,
                Double::class,
                Float::class,
                String::class
            )
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

    /**
     * Populates the map with the elements of the yaml file.
     *
     * @param elems the elements of the yaml file.
     * @param currIdent the current indentation level.
     * @return the indentation level of the last element.
     */
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
                val list = elem
                    .split(":", limit = 2)
                    .filter(String::isNotBlank)
                    .map(String::trim)
                if (list.size > 1) {
                    val (name, value) = list
                    if (name != "-") put(name, value)
                    else put(name+idx++, value)
                } else {
                    val name = list[0]
                    key =
                        if (name == "-")
                            name + idx++
                        else
                            name
                }
            }
            else if (ident > currIdent) {
                val list = elem
                    .split(":", limit = 2)
                    .filter(String::isNotBlank)
                    .map(String::trim)
                val auxMap: MutableMap<String, Any> =
                    if (list.size > 1) {
                        val (name, value) = list
                        val newName = if (name != "-") name else name + idx++
                        mutableMapOf(newName to value)
                    } else {
                        val name = list[0]
                        if (name == "-") elems.previous()
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
        return if (isPrimitive) {
            val lines = yaml
                .readLines()
                .filter(String::isNotBlank)
            lines
                .map { it.substringAfter("- ") }
                .map(String::trim)
                .map(typeReturn)
        }
        else {
            createArgsMap(yaml)
                .values
                .map(typeReturn)
        }
    }

    private fun createArgsMap(yaml: Reader): MutableMap<String, Any> {
        val lines = yaml
            .readLines()
            .filter(String::isNotBlank)
        val firstIdent = lines
            .first()
            .indexOfFirst { it != ' ' }
        val argsMap = mutableMapOf<String, Any>()
        argsMap.populateMap(lines.listIterator(), firstIdent)
        return argsMap
    }

    fun typeReturn(map: Map<String, Any>) : List<T> =
        map.values.map(typeReturn)
}
