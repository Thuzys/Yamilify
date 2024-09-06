package pt.isel

import java.io.File
import java.io.Reader
import kotlin.reflect.KClass

/**
 * An abstract class that implements the YamlParser interface.
 */
abstract class AbstractYamlParser<T : Any>(type: KClass<T>) : YamlParser<T> {

    /**
     * A property that returns a function that converts a string to a type T.
     */
    @Suppress("UNCHECKED_CAST")
    private val typeReturnVal: (Any) -> T by lazy {
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
            val elem = elems.next() // name: arthur -> -
            val ident = elem.indexOfFirst { it != ' ' }
            if (ident == currIdent) {
                val list = elem
                    .split(":", limit = 2) // [name, arthur]
                    .filter(String::isNotBlank)
                    .map(String::trim)
                if (list.size > 1) {
                    val (name, value) = list
                    if (name != "-") put(name, value)
                    else put(name+idx++, value)
                } else {
                    val name = list[0] // address
                    key =
                        if (name == "-")
                            name + idx++
                        else
                            name
                }
            }
            else if (ident > currIdent) {
                val list = elem // street: "Rua do Ouro"
                    .split(":", limit = 2)
                    .filter(String::isNotBlank)
                    .map(String::trim)
                val auxMap: MutableMap<String, Any> =
                    if (list.size > 1) {
                        val (name, value) = list
                        val newName = if (name != "-") name else name + idx++
                        mutableMapOf(newName to value) // { street to "Rua do Ouro" }
                    } else {
                        val name = list[0]
                        if (name == "-") elems.previous() // fix iterator
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

    /**
     * Parses a list of T from a yaml file.
     *
     * @param yaml the yaml file.
     * @return a list of T.
     */
    final override fun parseList(yaml: Reader): List<T> {
        return if (isPrimitive) {
            val lines = yaml
                .readLines()
                .filter(String::isNotBlank)
            lines
                .map { it.substringAfter("- ") }
                .map(String::trim)
                .map(typeReturnVal)
        }
        else {
            createArgsMap(yaml)
                .values
                .map(typeReturnVal)
        }
    }

    /**
     * Parses a sequence of T from a yaml file.
     *
     * @param yaml the yaml file.
     * @return a sequence of T.
     */
    final override fun parseSequence(yaml: Reader): Sequence<T> {
        return sequence {
            val argMap = createArgsMap(yaml)
            argMap
                .values
                .forEach {
                yield(typeReturnVal(it))
            }
        }
    }

    /**
     * Parses a folder of yaml files eagerly.
     *
     * @param path the path of the folder.
     * @return a list of T.
     */
    final override fun parseFolderEager(path: String): List<T> {
        var number = 0
        return File(path)
            .listFiles()
            ?.map { createArgsMap(it.absolutePath) }
            ?.map {
                try {
                    number++
                    typeReturnVal(it)
                } catch (e: IllegalArgumentException) {
                    if (number > 0)
                        throw IllegalArgumentException("Type of arguments in the list are not the same.")
                    else
                        throw e
                }
            } ?: emptyList()
    }

    /**
     * Parses a folder of yaml files lazily.
     *
     * @param path the path of the folder.
     * @return a sequence of T.
     */
    final override fun parseFolderLazy(path: String): Sequence<T> {
        val listOfPath = File(path).listFiles()
        var number = 0
        return sequence {
            listOfPath?.forEach {
                val argMap = createArgsMap(it.absolutePath)
                try {
                    yield(typeReturnVal(argMap))
                    number++
                } catch (e: IllegalArgumentException) {
                    if (number > 0)
                        throw IllegalArgumentException("Type of arguments in the list are not the same.")
                    else
                        throw e
                }
            }
        }
    }

    /**
     * Creates a map of arguments from a yaml file.
     *
     * @param path the path of the yaml file.
     * @return a map of arguments.
     */
    private fun createArgsMap(path: String): Map<String, Any> {
        val reader = File(path)
            .readText()
            .split('\n')
            .filter(String::isNotBlank)
        val firstIdent = reader
            .first()
            .indexOfFirst { it != ' ' }
        val argsMap = mutableMapOf<String, Any>()
        argsMap.populateMap(reader.listIterator(), firstIdent)
        return argsMap
    }

    /**
     * Creates a map of arguments from a yaml file.
     *
     * @param yaml the yaml file.
     * @return a map of arguments.
     */
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

    /**
     * Returns a list of T from a map.
     *
     * @param map the map.
     * @return a list of T.
     */
    fun typeReturn(map: Map<String, Any>) : List<T> =
        map.values.map(typeReturnVal)
}
