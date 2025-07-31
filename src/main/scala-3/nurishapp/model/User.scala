package nurishapp.model

import scalafx.beans.property.{ObjectProperty, StringProperty}
import java.time.LocalDate

case class User(
                 id: Option[Int],
                 usernameS: String,
                 passwordS: String,
                 emailS: String,
                 createdAtD: LocalDate
               ) {
  // ScalaFX properties
  val username = new StringProperty(this, "username", usernameS)
  val password = new StringProperty(this, "password", passwordS)
  val email = new StringProperty(this, "email", emailS)
  val createdAt = new ObjectProperty[LocalDate](this, "createdAt", createdAtD)

  // Convenience methods to get current values
  def getCurrentValues: User = copy(
    id = id,
    usernameS = username.value,
    passwordS = password.value,
    emailS = email.value,
    createdAtD = createdAt.value
  )
}

object User {
  def apply(
             id: Option[Int],
             username: String,
             password: String,
             email: String,
             createdAt: LocalDate
           ): User = new User(id, username, password, email, createdAt)
}
