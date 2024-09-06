package pt.isel

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * A YamlParser that uses reflection to parse objects.
 */
class YamlParserReflect<T : Any> private constructor(type: KClass<T>) : AbstractYamlParser<T>(type) {
    private val ctor: KFunction<T> by lazy {
        type.primaryConstructor ?: throw IllegalArgumentException("No primary constructor found")
    }
    private val parameters: HashMap<String, KParameter>
    private val constructor: HashMap<String, YamlSerializer<*>>
    private val range: IntRange
    private val paramWithType: HashMap<String, (Any) -> Any>

    init {
        if (type in primitiveType) {
            parameters = hashMapOf()
            constructor = hashMapOf()
            paramWithType = hashMapOf()
            range = 0..0
        }
        else {
            val parameterList = ctor.parameters
            constructor = hashMapOf()
            range = parameterList.count { !it.isOptional }..parameterList.size
            paramWithType = hashMapOf()
            parameters = hashMapOf()
            type
                .memberProperties
                .forEach {
                    val scrName = it.findAnnotation<YamlArg>()?.scrName
                    val ctor = it.findAnnotation<YamlConvert>()?.converter?.createInstance()
                    val param = parameterList.first { p -> p.name == it.name }
                    scrName?.let { scr ->
                        parameters[scr] = param
                        parameters[it.name] = param
                        ctor?.let { c ->
                            constructor[scr] = c
                            constructor[it.name] = c
                        } ?: run {
                            val withType = withTypeFunc(param.type)
                            paramWithType[it.name] = withType
                            paramWithType[scr] = withType
                        }
                    } ?: run {
                        parameters[it.name] = param
                        ctor?.let { c -> constructor[it.name] = c }
                            ?: run { paramWithType[it.name] = withTypeFunc(param.type) }
                    }
                }
        }
    }

    companion object {
        /**
         *Internal cache of YamlParserReflect instances.
         */
        private val yamlParsers: MutableMap<KClass<*>, YamlParserReflect<*>> = mutableMapOf()
        /**
         * Creates a YamlParser for the given type using reflection if it does not already exist.
         * Keep it in an internal cache of YamlParserReflect instances.
         */
        @Suppress("UNCHECKED_CAST")
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
        require(args.size in range) {
            "The number of arguments in the yaml does not match the number of parameters in the constructor"
        }
        val ctorArgs: Map<KParameter, Any?> =
            args
                .map { (key, value) ->
                    val param = parameters[key]
                        ?: throw IllegalArgumentException("Parameter $key not found in constructor")
                    val selValue = constructor[key]?.strConverter(value as String)
                    if (selValue != null) {
                        param to selValue
                    } else{
                        param to paramWithType[key]?.invoke(value)
                    }
                }
                .toMap()
        return ctor.callBy(ctorArgs)
    }

    @Suppress("UNCHECKED_CAST")
    private fun withTypeFunc(type: KType): (Any) -> Any {
        val classifier = type.classifier
        val kClass =
            when (classifier) {
                null -> throw IllegalArgumentException("KClassifier not found")
                List::class -> type.arguments.firstOrNull()?.type?.classifier as KClass<*>
                !in primitiveType -> classifier as KClass<*>
                else -> null
            }
        val parser = kClass?.let { Companion.yamlParser(it) }
        return when(classifier) {
            Int::class -> { it ->  (it as String).toInt() }
            Char::class -> { it -> (it as String).first() }
            Boolean::class -> { it -> (it as String).toBoolean() }
            Long::class -> { it -> (it as String).toLong() }
            Short::class -> { it -> (it as String).toShort() }
            Byte::class -> { it -> (it as String).toByte() }
            Double::class -> { it -> (it as String).toDouble() }
            Float::class -> { it -> (it as String).toFloat() }
            String::class -> { it -> it }
            else -> {
                checkNotNull(parser)
                if (classifier == List::class) {
                    { param -> parser.typeReturn(param as Map<String, Any>) }
                } else {
                    { param -> parser.newInstance(param as Map<String, Any>) }
                }
            }
        }
    }
}
