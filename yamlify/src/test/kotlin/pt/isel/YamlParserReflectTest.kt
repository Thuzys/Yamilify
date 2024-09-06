package pt.isel

import org.junit.jupiter.api.assertThrows
import pt.isel.test.Classroom
import pt.isel.test.NewStudent
import pt.isel.test.Student
import pt.isel.test.TestYamlLazyConverter
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class YamlParserReflectTest {
    private val initFileContent = """
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
    birth: 1999-12-12
    """.trimIndent()
    private val alteredFileContent = """
    name: Maria Candida
    nr: 873435
    address:
      street: Rua Rosa
      nr: 78
      city: Lisbon
    from: Lisbon
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
    birth: 1999-12-12
    """.trimIndent()

    private val fileYamlObjectPath =
        Paths
            .get("src", "test", "resources", "YamlObject.txt")
            .absolutePathString()
    private val directoryPath =
        Paths
            .get("src", "test", "resources")
            .absolutePathString()

    @Test
    fun `folder lazy test YamlObject`() {
        val parser = YamlParserReflect.yamlParser(Student::class)
        val iterator = parser.parseFolderLazy(directoryPath).iterator()
        val student = iterator.next()
        assertEquals("Paulo Silva", student.name)
        assertEquals(873435, student.nr)
        assertEquals("Rua Rosa", student.address?.street)
        assertEquals(78, student.address?.nr)
        assertEquals("Lisbon", student.address?.city)
        assertEquals("Liverpool", student.from)
        assertEquals(3, student.grades.size)
        assertEquals("1999-12-12", student.birth.toString())
        File(fileYamlObjectPath).writeText(alteredFileContent)
        iterator.next()
        val thirdStudent = iterator.next()
        assertEquals("Maria Candida", thirdStudent.name)
        assertEquals("Lisbon", thirdStudent.from)
        // revert file changes
        File(fileYamlObjectPath).writeText(initFileContent)
    }

    @Test
    fun `folder eager test YamlObject`() {
        val parser = YamlParserReflect.yamlParser(Student::class)
        val iterator = parser.parseFolderEager(directoryPath).iterator()
        val student = iterator.next()
        assertEquals("Paulo Silva", student.name)
        assertEquals(873435, student.nr)
        assertEquals("Rua Rosa", student.address?.street)
        assertEquals(78, student.address?.nr)
        assertEquals("Lisbon", student.address?.city)
        assertEquals("Liverpool", student.from)
        assertEquals(3, student.grades.size)
        assertEquals("1999-12-12", student.birth.toString())
        File(fileYamlObjectPath).writeText(alteredFileContent)
        iterator.next()
        val thirdStudent = iterator.next()
        assertEquals("Maria Candida", thirdStudent.name)
        assertEquals("Oleiros", thirdStudent.from)
        // revert file changes
        File(fileYamlObjectPath).writeText(initFileContent)
    }

    @Test
    fun `lazy sequence Yaml converter`()  {
        val yaml = """
            -
              name: Maria Candida
              nr: 873435
              address:
                street: Rua Rosa
                nr: 78
                city: Lisbon
              from: Oleiros
            - 
              name: Jose Carioca
              nr: 1214398
              address:
                street: Rua Azul
                nr: 12
                city: Porto
              from: Tamega
        """.trimIndent()
        TestYamlLazyConverter.resetCount()
        val parser = YamlParserReflect.yamlParser(Student::class)
        val seq = parser.parseSequence(yaml.reader())
        assertEquals(0, TestYamlLazyConverter.count)
        val seqIterator = seq.iterator()
        val elem1 = seqIterator.next()
        assertEquals("Maria Candida", elem1.name)
        assertEquals(1, TestYamlLazyConverter.count)
        assertEquals(873435, elem1.nr)
        assertEquals("Rua Rosa", elem1.address?.street)
        assertEquals("Oleiros", elem1.from)
        val elem2 = seqIterator.next()
        assertEquals("Jose Carioca", elem2.name)
        assertEquals(2, TestYamlLazyConverter.count)
    }

    @Test
    fun `test of parse object with Yaml converter`() {
        val yaml = """
            name: Maria Candida
            from: Oleiros
            nr: 873435
            birth: 1999-12-12"""
        val person = YamlParserReflect.yamlParser(Student::class).parseObject(yaml.reader())
        assertEquals("Maria Candida", person.name)
        assertEquals("Oleiros", person.from)
        assertEquals(873435, person.nr)
        assertEquals("1999-12-12", person.birth.toString())
    }

    @Test
    fun `test parse object with different parameter name`() {
        val yaml = """
            name: Maria Candida
            origin: Oleiros
            nr: 873435"""
        val person = YamlParserReflect.yamlParser(Student::class).parseObject(yaml.reader())
        assertEquals("Maria Candida", person.name)
        assertEquals("Oleiros", person.from)
    }

    @Test
    fun `invalid parameter name`() {
        val yaml = """
            name: Maria Candida
            country: Oleiros
            nr: 873435"""
        assertThrows<IllegalArgumentException> {
            YamlParserReflect.yamlParser(Student::class).parseObject(yaml.reader())
        }
    }

    @Test fun parseStudentWithMissingProperties() {
        val yaml = """
                name: Maria Candida
                from: Oleiros"""
        assertThrows<IllegalArgumentException> {
            YamlParserReflect.yamlParser(Student::class).parseObject(yaml.reader())
        }
    }
    @Test fun parseStudent() {
        val yaml = """
                name: Maria Candida
                nr: 873435
                from: Oleiros"""
        val st = YamlParserReflect.yamlParser(Student::class).parseObject(yaml.reader())
        assertEquals("Maria Candida", st.name)
        assertEquals(873435, st.nr)
        assertEquals("Oleiros", st.from)
    }
    @Test fun parseStudentWithAddress() {
        val yaml = """
                name: Maria Candida
                nr: 873435
                address:
                  street: Rua Rosa
                  nr: 78
                  city:
                    name: Lisbon
                    country: Portugal
                from: Oleiros"""
        val st = YamlParserReflect.yamlParser(NewStudent::class).parseObject(yaml.reader())
        assertEquals("Maria Candida", st.name)
        assertEquals(873435, st.nr)
        assertEquals("Oleiros", st.from)
        assertEquals("Rua Rosa", st.address?.street)
        assertEquals(78, st.address?.nr)
        assertEquals("Lisbon", st.address?.city?.name)
        assertEquals("Portugal", st.address?.city?.country)
    }

    @Test fun parseSequenceOfStrings() {
        val yaml = """
            - Ola
            - Maria Carmen
            - Lisboa Capital
        """
        val seq = YamlParserReflect.yamlParser(String::class)
            .parseList(yaml.reader())
            .iterator()
        assertEquals("Ola", seq.next())
        assertEquals("Maria Carmen", seq.next())
        assertEquals("Lisboa Capital", seq.next())
        assertFalse { seq.hasNext() }
    }

    @Test fun parseSequenceOfInts() {
        val yaml = """
            - 1
            - 2
            - 3
        """
        val seq = YamlParserReflect.yamlParser(Int::class)
            .parseList(yaml.reader())
            .iterator()
        assertEquals(1, seq.next())
        assertEquals(2, seq.next())
        assertEquals(3, seq.next())
        assertFalse { seq.hasNext() }
    }
    @Test fun parseSequenceOfStudents(){
        val yaml = """
            -
              name: Maria Candida
              nr: 873435
              from: Oleiros
            - 
              name: Jose Carioca
              nr: 1214398
              from: Tamega
        """
        val seq = YamlParserReflect.yamlParser(Student::class)
            .parseList(yaml.reader())
            .iterator()
        val st1 = seq.next()
        assertEquals("Maria Candida", st1.name)
        assertEquals(873435, st1.nr)
        assertEquals("Oleiros", st1.from)
        val st2 = seq.next()
        assertEquals("Jose Carioca", st2.name)
        assertEquals(1214398, st2.nr)
        assertEquals("Tamega", st2.from)
        assertFalse { seq.hasNext() }
    }
    @Test fun parseSequenceOfStudentsWithAddresses() {
        val yaml = """
            -
              name: Maria Candida
              nr: 873435
              address:
                street: Rua Rosa
                nr: 78
                city: Lisbon
              from: Oleiros
            - 
              name: Jose Carioca
              nr: 1214398
              address:
                street: Rua Azul
                nr: 12
                city: Porto
              from: Tamega
        """
        val seq = YamlParserReflect.yamlParser(Student::class)
            .parseList(yaml.reader())
            .iterator()
        val st1 = seq.next()
        assertEquals("Maria Candida", st1.name)
        assertEquals(873435, st1.nr)
        assertEquals("Oleiros", st1.from)
        assertEquals("Rua Rosa", st1.address?.street)
        assertEquals(78, st1.address?.nr)
        assertEquals("Lisbon", st1.address?.city)
        val st2 = seq.next()
        assertEquals("Jose Carioca", st2.name)
        assertEquals(1214398, st2.nr)
        assertEquals("Tamega", st2.from)
        assertEquals("Rua Azul", st2.address?.street)
        assertEquals(12, st2.address?.nr)
        assertEquals("Porto", st2.address?.city)
        assertFalse { seq.hasNext() }
    }
    @Test fun parseSequenceOfStudentsWithAddressesAndGrades() {
        val seq = YamlParserReflect.yamlParser(Student::class)
            .parseList(yamlSequenceOfStudents.reader())
            .iterator()
        assertStudentsInSequence(seq)
    }

    @Test fun parseClassroom() {
        val yaml = """
          id: i45
          students: $yamlSequenceOfStudents
        """.trimIndent()
        val cr = YamlParserReflect.yamlParser(Classroom::class)
            .parseObject(yaml.reader())
        assertEquals("i45", cr.id)
        assertStudentsInSequence(cr.students.iterator())
    }
    private fun assertStudentsInSequence(seq: Iterator<Student>) {
        val st1 = seq.next()
        assertEquals("Maria Candida", st1.name)
        assertEquals(873435, st1.nr)
        assertEquals("Oleiros", st1.from)
        assertEquals("Rua Rosa", st1.address?.street)
        assertEquals(78, st1.address?.nr)
        assertEquals("Lisbon", st1.address?.city)
        val grades1 = st1.grades.iterator()
        val g1 = grades1.next()
        assertEquals("LAE", g1.subject)
        assertEquals(18, g1.classification)
        val g2 = grades1.next()
        assertEquals("PDM", g2.subject)
        assertEquals(15, g2.classification)
        val g3 = grades1.next()
        assertEquals("PC", g3.subject)
        assertEquals(19, g3.classification)
        assertFalse { grades1.hasNext() }
        val st2 = seq.next()
        assertEquals("Jose Carioca", st2.name)
        assertEquals(1214398, st2.nr)
        assertEquals("Tamega", st2.from)
        assertEquals("Rua Azul", st2.address?.street)
        assertEquals(12, st2.address?.nr)
        assertEquals("Porto", st2.address?.city)
        val grades2 = st2.grades.iterator()
        val g4 = grades2.next()
        assertEquals("TDS", g4.subject)
        assertEquals(20, g4.classification)
        val g5 = grades2.next()
        assertEquals("LAE", g5.subject)
        assertEquals(18, g5.classification)
        assertFalse { grades2.hasNext() }
        assertFalse { seq.hasNext() }
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

