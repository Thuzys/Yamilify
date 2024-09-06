package pt.isel

import java.time.LocalDate

/**
 * A YamlSerializer that converts a string to a LocalDate.
 */
class YamlToDate: YamlSerializer<LocalDate> {
    override fun strConverter(str: String): LocalDate = LocalDate.parse(str)
}