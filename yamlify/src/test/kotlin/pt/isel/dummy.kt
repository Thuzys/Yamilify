package pt.isel

import pt.isel.test.Student

fun main() {
    val t = Student::class.java
    t.constructors.first{ it.parameters.size == 5 }.parameters.forEach {
        println(it.name)
        if (it.type == List::class.java) {
            println(it.type)
            println(it.parameterizedType as Class<*>)
            println(it.type.componentType)
        }
    }
}