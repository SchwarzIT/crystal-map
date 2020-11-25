package com.kaufland.model.entity

import kaufland.com.coachbasebinderapi.Entity

class EntityHolder(val dbName: String, val modifierOpen: Boolean, val entityType : Entity.Type) : BaseEntityHolder()
