package com.schwarz.crystaldemo

import com.schwarz.crystalapi.PersistenceConfig
import com.schwarz.crystalapi.TypeConversionErrorWrapper

open class UnitTestConnector() : PersistenceConfig.Connector {
    override fun deleteDocument(id: String, dbName: String, collection: String) {
        // Do Nothing
    }

    override fun getDocument(
        id: String,
        dbName: String,
        onlyInclude: List<String>?
    ): Map<String, Any>? {
        return null
    }

    override fun getDocuments(
        ids: List<String>,
        dbName: String,
        onlyInclude: List<String>?
    ): List<Map<String, Any>?> {
        return emptyList()
    }

    override fun queryDoc(
        dbName: String,
        queryParams: Map<String, Any>,
        limit: Int?,
        onlyInclude: List<String>?
    ): List<Map<String, Any>> {
        return emptyList()
    }

    override fun upsertDocument(
        document: MutableMap<String, Any>,
        id: String?,
        dbName: String
    ): Map<String, Any> {
        return document
    }

    override fun invokeOnError(errorWrapper: TypeConversionErrorWrapper) {
        throw errorWrapper.exception
    }
}
