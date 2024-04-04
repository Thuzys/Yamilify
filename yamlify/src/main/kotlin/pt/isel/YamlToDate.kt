package pt.isel

import java.time.LocalDate

class YamlToDate: YamlSerializer<LocalDate> {
    override fun convert(str: String): LocalDate {
        return LocalDate.parse(str)
    }
}