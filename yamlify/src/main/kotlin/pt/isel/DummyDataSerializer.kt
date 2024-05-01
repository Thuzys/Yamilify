package pt.isel

import java.time.LocalDate

object DummyDataSerializer {
    fun strConverter(str: String): LocalDate {
        val r = LocalDate.parse(str)
        return r
    }
}

//fun main() {
//    val dummy = DummyDataSerializer()
//    println(dummy.strConverter("2021-10-10"))
//}