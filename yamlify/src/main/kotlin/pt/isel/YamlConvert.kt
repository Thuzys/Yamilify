package pt.isel

import kotlin.reflect.KClass

/**
 * Annotation to specify the converter to use when serializing/deserializing a property.
 */
@Target(AnnotationTarget.PROPERTY)
annotation class YamlConvert(val converter: KClass<out YamlSerializer<*>>)
