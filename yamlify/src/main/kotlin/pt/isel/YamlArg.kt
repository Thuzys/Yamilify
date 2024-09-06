package pt.isel

/**
 * Annotation to specify the name of the property in the yaml file.
 */
@Target(AnnotationTarget.PROPERTY)
annotation class YamlArg(val scrName: String)
