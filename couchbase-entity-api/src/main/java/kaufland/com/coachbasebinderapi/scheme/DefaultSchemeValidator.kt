package kaufland.com.coachbasebinderapi.scheme

class DefaultSchemeValidator : SchemeValidator {

    override fun validate(current: List<EntityScheme>, released: List<EntityScheme>, releasedVersion: String): Boolean {
        val currentMap: Map<String, EntityScheme> = current.map { it.name to it }.toMap()
        val releasedMap: Map<String, EntityScheme> = released.map { it.name to it }.toMap()

        var result = true
        for (entry in releasedMap) {
            if (currentMap[entry.key] != entry.value) {
                result = result && validateLowLevel(currentMap[entry.key], entry.value)
            } else {
                println("${entry.key} did not changed [OK]")
            }
        }

        return result
    }

    fun validateLowLevel(current: EntityScheme?, released: EntityScheme?): Boolean {
        return true
    }
}