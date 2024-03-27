package com.schwarz.crystalapi.schema

interface DatabaseRecord<out T>

class CMField<T : Any>(val name: String, val path: String) : DatabaseRecord<T>

class CMList<T : Any>(val name: String, val path: String) : DatabaseRecord<T>

class CMObject<out T : Schema>(val element: T, val path: String) : DatabaseRecord<T>

class CMObjectList<out T : Schema>(val element: T, val name: String, val path: String) : DatabaseRecord<T>
