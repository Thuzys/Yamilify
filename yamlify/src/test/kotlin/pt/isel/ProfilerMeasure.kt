package pt.isel

import pt.isel.test.Student

fun main() {
//    val parser = YamlParserReflect.yamlParser(Student::class)
    val yamlStudent = """
        name: Maria Candida
        nr: 873435
        address:
          street: Rua Rosa
          nr: 78
          city: Lisbon
        from: Oleiros
        grades:
          - 
            subject: LAE
            classification: 18
          -
            subject: PDM
            classification: 15
          -
            subject: PC
            classification: 19
    """.trimIndent()

//    repeat(100) {
//        parser.parseList(yamlSequenceOfStudents.reader())
//    }
//    repeat(100) {
//        parser.parseObject(yamlStudent.reader())
//    }

    val parserCojen = YamlParserCojen.yamlParser(Student::class, 5)

    val parserCojen2 = YamlParserCojen.yamlParser(Student::class, 4)

    val yamlString = """
        name: Maria Candida
        nr: 873435
        address:
          street: Rua Rosa
          nr: 78
          city: Lisbon
        from: Oleiros
    """.trimIndent()

//    repeat(100) {
//        parserCojen.parseList(yamlSequenceOfStudents.reader())
//    }
    repeat(100) {
        parserCojen.parseObject(yamlStudent.reader())
    }
    repeat(100) {
        parserCojen2.parseObject(yamlString.reader())
    }
}
