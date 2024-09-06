package pt.isel.test

import pt.isel.YamlSerializer

/**
 * A test class that implements the YamlSerializer interface.
 * It is used to test the lazy evaluation of the converter.
 * It has a companion object that keeps track of the number of times the converter is called.
 * The converter simply increments the count and returns the string.
 * The count can be reset using the resetCount method.
 */
class TestYamlLazyConverter: YamlSerializer<String> {
    companion object {
        var count = 0
        fun resetCount(){
            count = 0
        }
    }
    override fun strConverter(str: String): String {
        count++
        return str
    }
}
