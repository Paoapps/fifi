import kotlinx.datetime.LocalTime

//package com.paoapps.fifi.domain
//
//import com.paoapps.fifi.serialization.LocalTimeSerializer
//import kotlinx.serialization.KSerializer
//import kotlinx.serialization.Serializable
//import kotlinx.serialization.descriptors.PrimitiveKind
//import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
//import kotlinx.serialization.encoding.Decoder
//import kotlinx.serialization.encoding.Encoder
//
//
//@Serializable(with = LocalTimeSerializer::class)
//data class LocalTime(val hour: Int, val minute: Int): Comparable<LocalTime> {
//
//    private val minutesSinceMidnight get() = hour * 60 + minute
//
//    fun format(): String {
//        var hours = "${hour}"
//        if (hour < 10) {
//            hours = "0$hours"
//        }
//
//        var minutes = "${minute}"
//        if (minute < 10) {
//            minutes = "0$minutes"
//        }
//
//        return "$hours:$minutes"
//    }
//
//    companion object {
//        fun parse(value: String): LocalTime {
//            val parts = value.split(":").map { it.toInt() }
//            if (parts.size != 2) {
//                throw IllegalArgumentException("Invalid time $value")
//            }
//
//            return LocalTime(parts[0], parts[1])
//        }
//    }
//
//    override fun compareTo(other: LocalTime) = minutesSinceMidnight.compareTo(other.minutesSinceMidnight)
//}
