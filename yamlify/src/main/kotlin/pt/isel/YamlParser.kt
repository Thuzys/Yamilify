package pt.isel

import java.io.Reader

/**
 * Interface for a YamlParser.
 */
interface YamlParser<T> {
    fun parseObject(yaml: Reader): T
    fun parseList(yaml: Reader): List<T>
    fun parseSequence(yaml: Reader): Sequence<T>
    fun parseFolderEager(path: String): List<T>
    fun parseFolderLazy(path: String): Sequence<T>
}