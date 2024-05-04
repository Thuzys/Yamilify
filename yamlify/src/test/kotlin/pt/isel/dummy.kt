package pt.isel

import pt.isel.test.Student
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

//fun main() {
//    val t = Student::class.java
//    t.constructors.first{ it.parameters.size == 5 }.parameters.forEach {
//        println(it.name)
//        if (it.type == List::class.java) {
//            val type: Type = it.parameterizedType
//            println(type)
//            println(type.typeName)
////            val jClass = Class.forName(type.typeName)
////            println(jClass)
//            println(type)
////            println(it.type)
////            println(Class.forName(it.type.typeName))
////            println(it.type.arrayType())
//        }
//    }
//}

fun main() {
    val t = Student::class.java
    t.constructors.first{ it.parameters.size == 5 }.parameters.forEach {
        println(it.name)
        if (it.parameterizedType is ParameterizedType) {
            val type = it.parameterizedType as ParameterizedType
            val actualTypeArguments = type.actualTypeArguments
            if (actualTypeArguments.isNotEmpty()) {
                val listElementType = actualTypeArguments[0]
                println("List element type: $listElementType")
                try {
                    val listElementClass = Class.forName(listElementType.typeName)
                    println("List element class: $listElementClass")
                } catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }
}