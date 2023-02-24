package com.github.alexzhirkevich.customqrgenerator

import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import java.net.URLEncoder

fun interface QrData {

    fun encode() : String

    @Serializable
    data class Text(val value : String) : QrData {
        override fun encode(): String = value
    }

    @Serializable
    data class Url(val url : String) : QrData {
        override fun encode(): String = url
    }

    data class Email(
        val email: String,
        val copyTo: String? = null,
        val subject: String? = null,
        val body: String? = null
    ) : QrData {
        override fun encode(): String = buildString {
            append("mailto:$email")

            if (listOf(copyTo, subject, body).any { it.isNullOrEmpty().not() }) {
                append("?")
            }
            val querries = buildList<String> {
                if (copyTo.isNullOrEmpty().not()) {
                    add("cc=$copyTo")
                }
                if (subject.isNullOrEmpty().not()) {
                    add("subject=${escape(subject!!)}")
                }
                if (body.isNullOrEmpty().not()) {
                    add("body=${escape(body!!)}")
                }
            }
            append(querries.joinToString(separator = "&"))
        }

        private fun escape(text: String) = URLEncoder.encode(text, Charsets.UTF_8.name())
            .replace("+", " ")

    }

    @Serializable
    data class GeoPos(
        val lat : Float,
        val lon : Float
    ) : QrData {
        override fun encode(): String = "GEO:$lat,$lon"
    }

    @Serializable
    data class Bookmark(
        val url : String,
        val title : String
    ) : QrData {
        override fun encode(): String  = buildString{
            append("MEBKM:")
            append("URL:$url;")
            append("TITLE:$title;")
            append(";")
        }
    }

    @Serializable
    data class Wifi(
        val authentication: Authentication?=null,
        val ssid: String? = null,
        val psk: String? = null,
        val hidden: Boolean = false,
    ) : QrData {

        enum class Authentication {
            WEP,
            WPA,
            OPEN {
                override fun toString(): String {
                    return "nopass"
                }
            }
        }

        override fun encode(): String = buildString{
            append("WIFI:")
            if (ssid!= null)
                append("S:${escape(ssid)};")

            if (authentication != null)
                append("T:${authentication};")

            if (psk != null)
                append("P:${escape(psk)};")

            append("H:$hidden;")
        }
        internal companion object {
            fun escape(text: String): String {
                return text.replace("\\", "\\\\")
                    .replace(",", "\\,")
                    .replace(";", "\\;")
                    .replace(".", "\\.")
                    .replace("\"", "\\\"")
                    .replace("'", "\\'")
            }
        }
    }

    @Serializable
    data class EnterpriseWifi(
        val ssid: String? = null,
        val psk: String? = null,
        val hidden: Boolean = false,
        val user : String? = null,
        val eap : String?=null,
        val phase : String?=null,
    ) : QrData {
        override fun encode(): String = buildString{
            append("WIFI:")
            if (ssid!= null)
                append("S:${Wifi.escape(ssid)};")

            if (user != null)
                append("U:${Wifi.escape(user)};")

            if (psk != null)
                append("P:${Wifi.escape(psk)};")

            if (eap != null)
                append("E:${Wifi.escape(eap)};")

            if (phase != null)
                append("PH:${Wifi.escape(phase)};")

            append("H:$hidden;")
        }
    }

    @Serializable
    data class Phone(val phoneNumber: String) : QrData {
        override fun encode(): String = "TEL:$phoneNumber"
    }

    @Serializable
    data class SMS(
        val phoneNumber: String,
        val subject : String,
        val isMMS : Boolean
    ) : QrData {
        override fun encode(): String = "${if (isMMS) "MMS" else "SMS"}:" +
                "$phoneNumber${if (subject.isNotEmpty()) ":$subject" else ""}"
    }

    @Serializable
    data class BizCard(
        val firstName : String? = null,
        val secondName : String? = null,
        val job : String? = null,
        val company : String? = null,
        val address : String? = null,
        val phone : String? = null,
        val email : String? = null,
    ) : QrData{
        override fun encode(): String  = buildString {
            append("BIZCARD:")
            if (firstName != null)
                append("N:$firstName;")

            if (secondName != null)
                append("X:$secondName;")

            if (job != null)
                append("T:$job;")

            if (company != null)
                append("C:$company;")

            if (address != null)
                append("A:$address;")

            if (phone != null)
                append("B:$phone;")

            if (email != null)
                append("E:$email;")

            append(";")
        }
    }

    @Serializable
    data class VCard(
        val name: String? = null,
        val company: String? = null,
        val title: String? = null,
        val phoneNumber: String? = null,
        val email: String? = null,
        val address: String? = null,
        val website: String? = null,
        val note: String? = null,
    ) : QrData {

        override fun encode(): String = buildString {
            append("BEGIN:VCARD\n")
            append("VERSION:3.0\n")
            if (name != null)
                append("N:$name\n")

            if (company != null)
                append("ORG:$company\n")

            if (title != null)
                append("TITLE$title\n")

            if (phoneNumber != null)
                append("TEL:$phoneNumber\n")

            if (website != null)
                append("URL:$website\n")

            if (email != null)
                append("EMAIL:$email\n")

            if (address != null)
                append("ADR:$address\n")

            if (note != null) {
                append("NOTE:$note\n")

                append("END:VCARD")
            }
        }
    }

    @Serializable
    data class MeCard(
        val name: String? = null,
        val address: String? = null,
        val phoneNumber: String? = null,
        val email: String? = null,
    ) : QrData {
        override fun encode(): String = buildString{
            append("MECARD:")
            if (name != null)
                append("N:$name;")

            if (address != null)
                append("ADR:$address;")

            if (phoneNumber != null)
                append("TEL:$phoneNumber;")

            if (email != null)
                append("EMAIL:$email;")

            append(";")
        }
    }

    @Serializable
    data class YouTube(val videoId : String) : QrData {
        override fun encode(): String = "YOUTUBE:$videoId"
    }

    @Serializable
    data class Event(
        val uid: String? = null,
        val stamp: String? = null,
        val organizer: String? = null,
        val start: String? = null,
        val end: String? = null,
        val summary: String? = null,
    ) : QrData {
        override fun encode(): String = buildString {
            append("BEGIN:VEVENT\n")
            if (uid != null)
                append("UID:$uid\n")
            if (stamp != null)
                append("DTSTAMP:$stamp\n")
            if (organizer != null)
                append("ORGANIZER:$organizer\n")

            if (start != null)
                append("DTSTART:$start\n")

            if (end != null)
                append("DTEND:$end\n")
            if (summary != null)
                append("SUMMARY:$summary\n")

            append("END:VEVENT")
        }
    }

    @Serializable
    data class GooglePlay(val appPackage : String) : QrData {
        override fun encode(): String = "{{{market://details?id=%$appPackage}}}"
    }

    companion object : SerializationProvider {
        override val defaultSerializersModule by lazy(LazyThreadSafetyMode.NONE) {
            SerializersModule {
                polymorphic(QrData::class) {
                    subclass(Text::class)
                    subclass(Url::class)
                    subclass(Email::class)
                    subclass(GeoPos::class)
                    subclass(Bookmark::class)
                    subclass(Wifi::class)
                    subclass(EnterpriseWifi::class)
                    subclass(Phone::class)
                    subclass(SMS::class)
                    subclass(BizCard::class)
                    subclass(VCard::class)
                    subclass(MeCard::class)
                    subclass(YouTube::class)
                    subclass(Event::class)
                    subclass(GooglePlay::class)
                }
            }
        }
    }
}