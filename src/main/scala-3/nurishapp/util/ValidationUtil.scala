package nurishapp.util

import java.time.LocalDate
import scala.util.Try

object ValidationUtil {
  def validateUsername(username: String): Option[String] = {
    val s = Option(username).getOrElse("").trim
    if (s.isEmpty) Some("Username cannot be empty")
    else if (!s.matches("^[a-zA-Z0-9_]{3,20}$")) Some("Username must be 3–20 characters and only letters/numbers/underscore")
    else None
  }

  def validateEmail(email: String): Option[String] = {
    val s = Option(email).getOrElse("").trim
    if (s.isEmpty) Some("Email cannot be empty")
    else if (!s.matches("^[A-Za-z0-9+_.-]+@(.+)$")) Some("Please enter a valid email address")
    else None
  }

  def validatePassword(pw: String): Option[String] = {
    val s = Option(pw).getOrElse("")
    if (s.isEmpty) Some("Password cannot be empty")
    else if (s.length < 8) Some("Password must be at least 8 characters long")
    else {
      val missing = collection.mutable.ListBuffer[String]()
      if (!s.matches(".*[A-Z].*")) missing += "uppercase letter"
      if (!s.matches(".*[a-z].*")) missing += "lowercase letter"
      if (!s.matches(".*[0-9].*")) missing += "number"
      if (missing.nonEmpty) Some(s"Password must contain at least one ${missing.mkString(", ")}")
      else None
    }
  }

  def parseDob(s: String): Option[LocalDate] =
    Try(nurishapp.util.DateUtil.parseLocalDate(s)).toOption.flatten.orElse(Try(LocalDate.parse(s)).toOption)
}
