@file:Suppress("UNCHECKED_CAST")

package com.schwarz.crystalapi.util

import com.schwarz.crystalapi.mapify.IMapifyable
import org.apache.commons.codec.binary.Base64
import java.io.*
import java.util.*

class SerializableMapifyable<T : Serializable> : IMapifyable<T?> {
    override fun fromMap(map: Map<String, Any>): T? = (map.get("serial") as? String)?.let { serializableFromMapValue<T>(it) }

    override fun toMap(obj: T?): Map<String, Any> = obj?.let { serializableToMapValue(it) }?.let { mapOf("serial" to it) }
        ?: mapOf()
}

private fun <T : Serializable> serializableToMapValue(obj: T?) = obj?.let {
    ByteArrayOutputStream().apply {
        ObjectOutputStream(this).writeObject(obj)
    }.toByteArray()?.let { Base64().encodeAsString(it) }
}

private fun <T : Serializable> serializableFromMapValue(value: String): T? =
    ByteArrayInputStream(Base64().decode(value)).let {
        ObjectInputStream(it).readObject()
    } as? T
