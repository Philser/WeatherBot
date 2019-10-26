package philser.util

import java.time.*
import java.util.*

class Utils {

    companion object {
        fun epochToLocalDateTime(epoch: Long, epochTimeZone: String): LocalDateTime {
            val dateTime = LocalDateTime.ofEpochSecond(epoch, 0, ZoneOffset.UTC)
            return ZonedDateTime.of(dateTime, ZoneId.of(epochTimeZone)).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime() // Convert UTC to local
        }
    }
}