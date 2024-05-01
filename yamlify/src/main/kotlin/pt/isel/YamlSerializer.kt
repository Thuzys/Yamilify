package pt.isel

interface YamlSerializer<T> {
    fun strConverter(str: String): T
}