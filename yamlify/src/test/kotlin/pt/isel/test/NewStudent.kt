package pt.isel.test

import pt.isel.YamlArg
import pt.isel.YamlConvert
import pt.isel.YamlToDate
import java.time.LocalDate

class NewStudent @JvmOverloads constructor(
    val name: String,
    val nr: Int,
    @YamlArg("origin")val from: String,
    @YamlConvert(YamlToDate::class)val birth: LocalDate = LocalDate.now(),
    val address: NewAddress? = null,
    val grades: List<Grade> = emptyList(),
)
