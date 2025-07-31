package nurishapp.util

import nurishapp.model.User
import java.time.LocalDate
import java.util.Base64
import java.security.MessageDigest
import scala.util.Try

object AuthenticationUtil {
  def login(username: String, password: String): Try[Boolean] = {
    Try {
      val hashedPassword = hashPassword(password)
      UserDatabase.findByUsername(username).exists(_.password.value == hashedPassword)
    }
  }

  def registerUser(username: String, password: String, email: String): Try[User] = {
    val hashedPassword = hashPassword(password)
    val user = User(
      id = None,
      username = username,
      password = hashedPassword,
      email = email,
      createdAt = LocalDate.now()
    )
    UserDatabase.create(user)
  }

  private def hashPassword(password: String): String = {
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(password.getBytes("UTF-8"))
    Base64.getEncoder.encodeToString(hash)
  }
}


