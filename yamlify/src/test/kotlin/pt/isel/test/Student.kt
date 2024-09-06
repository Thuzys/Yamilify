package pt.isel.test

import pt.isel.YamlArg
import pt.isel.YamlConvert
import pt.isel.YamlToDate
import java.time.LocalDate

class Student @JvmOverloads constructor (
    @YamlConvert(TestYamlLazyConverter::class) val name: String,
    val nr: Int,
    @YamlArg("origin") val from: String,
    val address: Address? = null,
    val grades: List<Grade> = emptyList(),
    @YamlConvert(YamlToDate::class)val birth: LocalDate? = null,
)