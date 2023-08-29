package com.paoapps.fifi.serialization

import kotlinx.datetime.LocalTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

//object LocalTimeSerializer: KSerializer<LocalTime> {
//    override val descriptor = PrimitiveSerialDescriptor("LocalTime", PrimitiveKind.STRING)
//
//    override fun serialize(encoder: Encoder, value: LocalTime) {
//        encoder.encodeString(value.format())
//    }
//
//    override fun deserialize(decoder: Decoder): LocalTime {
//        val parts = decoder.decodeString().split(":")
//        val hours = parts[0].toInt()
//        val minutes = parts[1].toInt()
//        return LocalTime(hours, minutes)
//    }
//}
