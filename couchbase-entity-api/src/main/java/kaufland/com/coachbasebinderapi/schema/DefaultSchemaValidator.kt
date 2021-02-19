package kaufland.com.coachbasebinderapi.schema

open class DefaultSchemaValidator : SchemaValidator {

    override fun validate(current: List<EntitySchema>, released: List<EntitySchema>, logger: SchemaValidationLogger) {
        val currentMap: Map<String, EntitySchema> = current.map { it.name to it }.toMap()
        val releasedMap: Map<String, EntitySchema> = released.map { it.name to it }.toMap()

        for (entry in releasedMap) {
            if (currentMap[entry.key] != entry.value) {
                validateModelLevel(currentMap[entry.key], entry.value, logger)
            } else {
                logger.info(entry.value, "did not change")
            }
        }
    }

    protected open fun validateModelLevel(current: EntitySchema?, released: EntitySchema, logger: SchemaValidationLogger): Boolean {

        current?.let {

            released.docId?.let {
                if(current?.docId?.scheme != it.scheme){
                    logger.error(released, "forbidden DocId Schema change")
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

    protected open fun validateFieldLevel(released: EntitySchema, key: String, logger: SchemaValidationLogger) {
        if (released?.deprecatedSchema?.deprecatedFields?.find { it.field == key }?.inUse != false) {
            logger.error(released, "forbidden change on existing field [$key]")
        } else {
            logger.info(released, "allowed change on existing field [$key] since it's deprecated and no longer in use")
        }
    }

    private fun modelDeletedDuringValidDeprecationPeriod(released: EntitySchema, logger: SchemaValidationLogger) {
        if(released?.deprecatedSchema == null || released?.deprecatedSchema.inUse){
            logger.error(released, "forbidden model deletion")
        }else{
            logger.error(released, "allowed model deletion")
        }
    }
}