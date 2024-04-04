package pt.isel

import kotlin.reflect.KClass

@Target(AnnotationTarget.PROPERTY)
annotation class YamlConvert(val converter: KClass<out YamlSerializer<*>>)
