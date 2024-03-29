package pt.isel

import java.io.Reader
import kotlin.reflect.*
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

abstract class AbstractYamlParser<T : Any>(private val type: KClass<T>) : YamlParser<T> {
    private val ctor: KFunction<T> = type.constructors.first()

    /**
     * Used to get a parser for other Type using this same parsing approach.
     */
    abstract fun <T : Any> yamlParser(type: KClass<T>): AbstractYamlParser<T>

    /**
     * Creates a new instance of T through the first constructor
     * that has all the mandatory parameters in the map and optional parameters for the rest.
     */
    abstract fun newInstance(args: Map<String, Any>): T

    final override fun parseObject(yaml: Reader): T {
        val lines = yaml.readLines().filter { it.isNotBlank() }
        val firstIdent = lines.first().indexOfFirst { it != ' ' }
        var key = ""
        val args: MutableMap<String, String> = mutableMapOf()

        lines.forEach {
            val ident = it.indexOfFirst { c -> c != ' ' }
            if (ident == firstIdent) {
                val (name, value) = it.trimIndent().split(":")
                key = name
                args[name] = value
            } else if (ident > firstIdent) {
                args[key] = args[key] + "\n" + it
            }
        }
        require(args.size in ctor.parameters.filter { !it.isOptional }.size..ctor.parameters.size) {
            "The number of arguments in the yaml does not match the number of parameters in the constructor"
        }
        val argsMap: Map<String, String> = args.mapValues { (name, value) ->
            require(isValidName(name)) { "Parameter $name not found in constructor" }
            value.trimIndent()
        }
        return newInstance(argsMap)
    }

    private fun isValidName(name: String): Boolean = (hasSameName(name) || hasAnnotation(name))

    private fun hasAnnotation(name: String) =
        type.memberProperties
            .mapNotNull { it.findAnnotation<YamlArg>() }
            .any { it.scrName == name }

    private fun hasSameName(name: String) = ctor.parameters.firstOrNull { it.name == name } != null

    final override fun parseList(yaml: Reader): List<T> {
        val lines = yaml.readLines().filter { it.isNotBlank() }
        val firstIdent = lines.first().indexOfFirst { it != ' ' }
        val listObjects: MutableList<String> = mutableListOf()
        var idx = 0
        lines.forEach {
            val ident = it.indexOfFirst { c -> c != ' ' }
            if (ident == firstIdent) {
                listObjects.add(it.trim().drop(1))
                idx = listObjects.size - 1
            } else if (ident > firstIdent) {
                listObjects[idx] = listObjects[idx] + "\n" + it
            }
        }
        return listObjects.map { typePrimitive(type, it) }
    }

    private fun typePrimitive(type: KClassifier, elem: String): T {
        val trimmed = elem.trim()
         val r: Any =  when (type) {
            Int::class -> trimmed.toInt()
            Char::class -> trimmed.first()
            Boolean::class -> trimmed.toBoolean()
            Long::class -> trimmed.toLong()
            Short::class -> trimmed.toShort()
            Byte::class -> trimmed.toByte()
            Double::class -> trimmed.toDouble()
            Float::class -> trimmed.toFloat()
            String::class -> trimmed
            else -> parseObject(elem.reader())
        }
        return r as? T ?: throw IllegalArgumentException("Type not found")
    }

}