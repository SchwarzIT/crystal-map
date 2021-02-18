package kaufland.com.coachbasebinderapi.scheme

interface SchemeValidationLogger {

    fun info(entityScheme: EntityScheme, message : String)

    fun error(entityScheme: EntityScheme, message : String)

    fun warning(entityScheme: EntityScheme, message : String)


}