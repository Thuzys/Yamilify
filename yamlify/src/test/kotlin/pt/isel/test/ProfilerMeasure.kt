package pt.isel.test

import pt.isel.YamlParserReflect

fun main() {
    val parser = YamlParserReflect.yamlParser(Student::class)
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

    repeat(100) {
        parser.parseList(yamlSequenceOfStudents.reader())
    }
    repeat(100) {
        parser.parseObject(yamlStudent.reader())
    }
}

const val yamlSequenceOfStudents = """
            -
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
            - 
              name: Jose Carioca
              nr: 1214398
              address:
                street: Rua Azul
                nr: 12
                city: Porto
              from: Tamega
              grades:
                -
                  subject: TDS
                  classification: 20
                - 
                  subject: LAE
                  classification: 18
        """