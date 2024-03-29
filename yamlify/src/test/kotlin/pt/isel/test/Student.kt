package pt.isel.test

import pt.isel.YamlArg

class Student @JvmOverloads constructor (
    val name: String,
    val nr: Int,
    @YamlArg("origin")val from: String,
    val address: Address? = null,
    val grades: List<Grade> = emptyList()
)