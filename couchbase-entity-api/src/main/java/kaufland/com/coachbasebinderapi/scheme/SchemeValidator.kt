package kaufland.com.coachbasebinderapi.scheme

interface SchemeValidator {

    fun validate(current: List<EntityScheme>, released: List<EntityScheme>, releasedVersion: String) : Boolean
}