package pt.isel

class DummyYamlConverter: YamlSerializer<String> {
    companion object {
        var count = 0
        fun resetCount(){
            count = 0
        }
    }
    override fun strConverter(str: String): String {
        count++
        return str
    }

}