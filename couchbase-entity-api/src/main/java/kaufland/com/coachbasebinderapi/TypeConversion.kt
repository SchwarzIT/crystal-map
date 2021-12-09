package kaufland.com.coachbasebinderapi

interface TypeConversion {
    fun read(value: Any?): Any?
    fun write(value: Any?): Any?
}
