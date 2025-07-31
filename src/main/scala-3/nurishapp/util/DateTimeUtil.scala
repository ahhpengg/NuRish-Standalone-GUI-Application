package nurishapp.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object DateTimeUtil:
  val DATETIME_PATTERN = "dd.MM.yyyy HH:mm"
  val DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_PATTERN)

  extension (datetime: LocalDateTime)
    /**
     * Returns the datetime as a formatted String using DATETIME_PATTERN.
     *
     * @return formatted string or null if datetime is null
     */
    def asString: String =
      if datetime == null then null
      else DATETIME_FORMATTER.format(datetime)

  extension (data: String)
    /**
     * Converts a String in the format of DATETIME_PATTERN to a LocalDateTime object.
     *
     * @return Some(LocalDateTime) if successful, None if parsing failed
     */
    def parseLocalDateTime: Option[LocalDateTime] =
      try
        Option(LocalDateTime.parse(data, DATETIME_FORMATTER))
      catch
        case _: DateTimeParseException => None

    def isValid: Boolean =
      parseLocalDateTime.isDefined

