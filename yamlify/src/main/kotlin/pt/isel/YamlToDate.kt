package pt.isel

import java.time.LocalDate

class YamlToDate: YamlSerializer<LocalDate> {
    override fun strConverter(str: String): LocalDate = LocalDate.parse(str)
}