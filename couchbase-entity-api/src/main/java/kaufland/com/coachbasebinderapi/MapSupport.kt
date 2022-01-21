package kaufland.com.coachbasebinderapi

interface MapSupport {

    fun toMap(): Map<String, Any>

    fun setAll(doc: Map<String, Any?>)
}
