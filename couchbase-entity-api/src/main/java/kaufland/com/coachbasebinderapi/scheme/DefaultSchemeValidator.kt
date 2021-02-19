package kaufland.com.coachbasebinderapi.scheme

open class DefaultSchemeValidator : SchemeValidator {

    override fun validate(current: List<EntityScheme>, released: List<EntityScheme>, logger: SchemeValidationLogger) {
        val currentMap: Map<String, EntityScheme> = current.map { it.name to it }.toMap()
        val releasedMap: Map<String, EntityScheme> = released.map { it.name to it }.toMap()

        for (entry in releasedMap) {
            if (currentMap[entry.key] != entry.value) {
                validateModelLevel(currentMap[entry.key], entry.value, logger)
            } else {
                logger.info(entry.value, "did not change")
            }
        }
    }

    protected open fun validateModelLevel(current: EntityScheme?, released: EntityScheme, logger: SchemeValidationLogger): Boolean {

        current?.let {

            released.docId?.let {
                if(current?.docId?.scheme != it.scheme){
                    logger.error(released, "forbidden DocId Scheme change")
                }
            }

            val currentFields = current.fields.map { it.dbField to it }.toMap()
            val releasedFields = released.fields.map { it.dbField to it }.toMap()

            for (key in releasedFields.keys) {
                if(currentFields[key] != releasedFields[key]){
                    validateFieldLevel(released, key, logger)
                }
            }

        } ?: modelDeletedDuringValidDeprecationPeriod(released, logger)

        return true
    }

    protected open fun validateFieldLevel(released: EntityScheme, key: String, logger: SchemeValidationLogger) {
        if (released?.deprecatedScheme?.deprecatedFields?.find { it.field == key }?.inUse != false) {
            logger.error(released, "forbidden change on existing field [$key]")
        } else {
            logger.info(released, "allowed change on existing field [$key] since it's deprecated and no longer in use")
        }
    }

    private fun modelDeletedDuringValidDeprecationPeriod(released: EntityScheme, logger: SchemeValidationLogger) {
        if(released?.deprecatedScheme == null || released?.deprecatedScheme.inUse){
            logger.error(released, "forbidden model deletion")
        }else{
            logger.error(released, "allowed model deletion")
        }
    }
}