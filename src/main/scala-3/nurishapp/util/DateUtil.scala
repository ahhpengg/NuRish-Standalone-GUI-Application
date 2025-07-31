package nurishapp.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object DateUtil:
  val DATE_PATTERN = "dd.MM.yyyy"
  val DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN)

  extension (date: LocalDate)
    /**
     * Returns the given date as a well formatted String using DATE_PATTERN.
     *
     * @return formatted string or null if date is null
     */
    def asString: String =
      if date == null then null
      else DATE_FORMATTER.format(date)

  extension (data: String)
    /**
     * Converts a String in the format of DATE_PATTERN to a LocalDate object.
     *
     * @return Some(LocalDate) if successful, None if parsing failed
     */
    def parseLocalDate: Option[LocalDate] =
      try
        Option(LocalDate.parse(data, DATE_FORMATTER))
      catch
        case _: DateTimeParseException => None

    def isValid: Boolean =
      parseLocalDate.isDefined
