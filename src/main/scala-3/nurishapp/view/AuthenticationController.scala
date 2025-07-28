package nurishapp.view

import nurishapp.util.UserDatabase
import nurishapp.model.User
import java.security.MessageDigest
import java.util.Base64
import java.time.LocalDateTime

object AuthenticationController {
  def login(username: String, password: String): Boolean = {
    val hashedPassword = hashPassword(password)
    UserDatabase.findByUsername(username).exists(_.password == hashedPassword)
  }

  def registerUser(username: String, password: String, email: String): Boolean = {
    try {
      val hashedPassword = hashPassword(password)
      val user = User(
        id = None,
        username = username,
        password = hashedPassword,
        email = email,
        createdAt = LocalDateTime.now()
      )
      UserDatabase.create(user).isDefined
    } catch {
      case _: Exception => false
    }
  }

  private def hashPassword(password: String): String = {
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(password.getBytes("UTF-8"))
    Base64.getEncoder.encodeToString(hash)
  }
}

