package pt.isel

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

/**
 * A YamlParser that uses reflection to parse objects.
 */
class YamlParserReflect<T : Any>(type: KClass<T>) : AbstractYamlParser<T>(type) {
    private val currType = type
    private val ctor = type.constructors.first()
    companion object {
        /**
         *Internal cache of YamlParserReflect instances.
         */
        private val yamlParsers: MutableMap<KClass<*>, YamlParserReflect<*>> = mutableMapOf()
        /**
         * Creates a YamlParser for the given type using reflection if it does not already exist.
         * Keep it in an internal cache of YamlParserReflect instances.
         */
        fun <T : Any> yamlParser(type: KClass<T>): AbstractYamlParser<T> {
            return yamlParsers.getOrPut(type) { YamlParserReflect(type) } as YamlParserReflect<T>
        }
    }
    /**
     * Used to get a parser for another Type using the same parsing approach.
     */
    override fun <T : Any> yamlParser(type: KClass<T>) = YamlParserReflect.yamlParser(type)
    /**
     * Creates a new instance of T through the first constructor
     * that has all the mandatory parameters in the map and optional parameters for the rest.
     */
    override fun newInstance(args: Map<String, Any>): T {
        val ctorArgs: Map<KParameter, Any?> =
            args
                .map { (key, value) ->
                    val param = ctor.parameters.first { it.name == validParameter(key).name }
                    val stringValue = value as String
                    param to stringValue.withType(param.type)
                }
                .toMap()
        return ctor.callBy(ctorArgs)
    }

    private fun validParameter(name: String) =
        currType.memberProperties.firstOrNull { it.name == name || it.findAnnotation<YamlArg>()?.scrName == name }
            ?: throw IllegalArgumentException("Parameter $name not found in constructor")

    private fun String.withType(type: KType): Any =
        when(type.classifier) {
            Int::class -> toInt()
            Char::class -> first()
            Boolean::class -> toBoolean()
            Long::class -> toLong()
            Short::class -> toShort()
            Byte::class -> toByte()
            Double::class -> toDouble()
            Float::class -> toFloat()
            String::class -> this
            List::class -> YamlParserReflect(type.arguments[0].type!!.classifier as KClass<*>).parseList(reader())
            Sequence::class ->
                YamlParserReflect(type.arguments[0].type!!.classifier as KClass<*>).parseList(reader())
                    .asSequence()
            null -> throw IllegalArgumentException("KClassifier not found")
            else -> YamlParserReflect(type.classifier as KClass<*>).parseObject(reader())
        }
}
