package com.github.alexzhirkevich.customqrgenerator

interface QrData {

    fun encode() : String

    data class Text(val value : String) : QrData {
        override fun encode(): String = value
    }

    data class Url(val url : String) : QrData {
        override fun encode(): String = url
    }

    data class Email(val email: String) : QrData {
        override fun encode(): String = "MAILTO: $email";
    }

    data class GeoPos(
        val lat : Float,
        val lon : Float
    ) : QrData {
        override fun encode(): String = "GEO:$lat,$lon"
    }

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

    data class Wifi(
        val authentication: String? = null,
        val ssid: String? = null,
        val psk: String? = null,
        val hidden: Boolean = false,
    ) : QrData {
        override fun encode(): String = buildString{
            append("WIFI:")
            if (ssid!= null) {
                append("S:${escape(ssid)};")
            }
            if (authentication != null) {
                append("T:${authentication};")
            }
            if (psk != null) {
                append("P:${escape(psk)};")
            }
            append("H:$hidden;")
        }
        internal companion object {
            fun escape(text: String): String? {
                return text.replace("\\", "\\\\")
                    .replace(",", "\\,")
                    .replace(";", "\\;")
                    .replace(".", "\\.")
                    .replace("\"", "\\\"")
                    .replace("'", "\\'")
            }
        }
    }

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
            if (ssid!= null) {
                append("S:${Wifi.escape(ssid)};")
            }
            if (user != null){
                append("U:${Wifi.escape(user)};")
            }
            if (psk != null) {
                append("P:${Wifi.escape(psk)};")
            }
            if (eap != null) {
                append("E:${Wifi.escape(eap)};")
            }
            if (phase != null){
                append("PH:${Wifi.escape(phase)};")
            }
            append("H:$hidden;")
        }
    }

    data class Phone(val phoneNumber: String) : QrData {
        override fun encode(): String = "TEL:$phoneNumber"
    }

    data class SMS(
        val phoneNumber: String,
        val subject : String,
        val isMMS : Boolean
        ) : QrData {
        override fun encode(): String = "${if (isMMS) "MMS" else "SMS"}:" +
                "$phoneNumber${if (subject.isNotEmpty()) ":$subject" else ""}"
    }

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
            if (firstName != null) {
                append("N:$firstName;")
            }
            if (secondName != null) {
                append("X:$secondName;")
            }
            if (job != null) {
                append("T:$job;")
            }
            if (company != null) {
                append("C:$company;")
            }
            if (address != null) {
                append("A:$address;")
            }
            if (phone != null) {
                append("B:$phone;")
            }
            if (email != null) {
                append("E:$email;")
            }
            append(";")
        }
    }

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

        override fun encode(): String  = buildString{
            append("BEGIN:VCARD")
            append("\n")
            append("VERSION:3.0")
            append("\n")
            if (name != null) {
                append("N:$name\n")
            }
            if (company != null) {
                append("ORG:$company\n")
            }
            if (title != null) {
                append("TITLE$title\n")
            }
            if (phoneNumber != null) {
                append("TEL:$phoneNumber\n")
            }
            if (website != null) {
                append("URL:$website\n")
            }
            if (email != null) {
                append("EMAIL:$email\n")
            }
            if (address != null) {
                append("ADR:$address\n")
            }
            if (note != null) {
               append("NOTE:$note\n")
            }
            append("END:VCARD")
        }
    }

    data class MeCard(
        val name: String? = null,
        val address: String? = null,
        val phoneNumber: String? = null,
        val email: String? = null,
    ) : QrData {
        override fun encode(): String = buildString{
            append("MECARD:")
            if (name != null) {
                append("N:$name;")
            }
            if (address != null) {
                append("ADR:$address;")
            }
            if (phoneNumber != null) {
                append("TEL:$phoneNumber;")
            }
            if (email != null) {
                append("EMAIL:$email;")
            }
            append(";")
        }
    }

    data class YouTube(val videoId : String) : QrData {
        override fun encode(): String = "YOUTUBE:$videoId"
    }

    data class Event(
        val uid: String? = null,
        val stamp: String? = null,
        val organizer: String? = null,
        val start: String? = null,
        val end: String? = null,
        val summary: String? = null,
    ) : QrData {
        override fun encode(): String = buildString{
            append("BEGIN:VEVENT\n")
            if (uid != null) {
                append("UID:$uid\n")
            } else if (stamp != null) {
                append("DTSTAMP:$stamp\n")
            } else if (organizer != null) {
                append("ORGANIZER:$organizer\n")
            } else if (start != null) {
               append("DTSTART:$start\n")
            } else if (end != null) {
                append("DTEND:$end\n")
            } else if (summary != null) {
                append("SUMMARY:$summary\n")
            }
            append("\nEND:VEVENT");
        }
    }

    data class GooglePlay(val appPackage : String) : QrData {
        override fun encode(): String = "{{{market://details?id=%$appPackage}}}"
    }
}