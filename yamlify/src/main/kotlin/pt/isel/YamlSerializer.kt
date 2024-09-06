package pt.isel

/**
 * Interface for a YamlSerializer.
 */
interface YamlSerializer<T> {
    fun strConverter(str: String): T
}