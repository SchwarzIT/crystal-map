package com.schwarz.crystaldemo.customtypes

import com.schwarz.crystalapi.ITypeConverter
import com.schwarz.crystalapi.TypeConverter
import com.schwarz.crystalapi.typeconverters.EnumConverter
import schwarz.fwws.shared.model.CaptureState

@TypeConverter
abstract class CaptureStateConverter : ITypeConverter<CaptureState, String> by EnumConverter(CaptureState::class)
