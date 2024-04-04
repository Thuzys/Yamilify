package pt.isel

interface YamlSerializer<T> {
    fun convert(str: String): T
}