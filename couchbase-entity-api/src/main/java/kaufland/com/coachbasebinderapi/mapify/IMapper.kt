package kaufland.com.coachbasebinderapi.mapify

interface IMapper<T> {

    fun fromMap(obj: T, map: Map<String, Any>)

    fun toMap(obj: T): Map<String, Any>
}
