package nurishapp.model

import scalafx.beans.property.{ObjectProperty, StringProperty}
import java.time.LocalDate
import scalikejdbc.*
import scala.util.Try
import nurishapp.util.Database

case class User(
                 id: Option[Int],
                 usernameS: String,
                 passwordS: String,
                 emailS: String,
                 createdAtD: LocalDate
               ) extends Database {
  // ScalaFX properties
  val username = new StringProperty(this, "username", usernameS)
  val password = new StringProperty(this, "password", passwordS)
  val email = new StringProperty(this, "email", emailS)
  val createdAt = new ObjectProperty[LocalDate](this, "createdAt", createdAtD)

  def getCurrentValues: User = copy(
    id = id,
    usernameS = username.value,
    passwordS = password.value,
    emailS = email.value,
    createdAtD = createdAt.value
  )

  def save()(implicit session: DBSession = AutoSession): Try[User] = {
    if (id.isEmpty) {
      // Create new user
      Try {
        sql"""
          INSERT INTO users (username, password, email, created_at)
          VALUES (${username.value}, ${password.value}, ${email.value}, ${createdAt.value})
        """.update.apply()

        User.findByUsername(username.value).getOrElse(
          throw new RuntimeException("Failed to create user")
        )
      }
    } else {
      // Update existing user
      Try {
        sql"""
          UPDATE users 
          SET username = ${username.value},
              password = ${password.value},
              email = ${email.value},
              created_at = ${createdAt.value}
          WHERE id = ${id.get}
        """.update.apply()

        User.findById(id.get).getOrElse(
          throw new RuntimeException("Failed to update user")
        )
      }
    }
  }

  def delete()(implicit session: DBSession = AutoSession): Try[Boolean] = {
    id match {
      case Some(userId) =>
        Try {
          val affected = sql"DELETE FROM users WHERE id = $userId".update.apply()
          affected > 0
        }
      case None =>
        Try(false)
    }
  }
}

object User extends Database {
  def initializeTable(): Unit = {
    DB.autoCommit { implicit session =>
      sql"""
      CREATE TABLE users (
        id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,
        username VARCHAR(50) NOT NULL UNIQUE,
        password VARCHAR(255) NOT NULL,
        email VARCHAR(100) NOT NULL,
        created_at DATE NOT NULL
      )
    """.execute.apply()
    }
  }


  def findById(id: Int)(implicit session: DBSession = AutoSession): Option[User] = {
    sql"SELECT * FROM users WHERE id = $id"
      .map(rs => User(
        Some(rs.int("id")),
        rs.string("username"),
        rs.string("password"),
        rs.string("email"),
        rs.date("created_at").toLocalDate
      )).single.apply()
  }

  def findByUsername(username: String)(implicit session: DBSession = AutoSession): Option[User] = {
    sql"SELECT * FROM users WHERE username = $username"
      .map(rs => User(
        Some(rs.int("id")),
        rs.string("username"),
        rs.string("password"),
        rs.string("email"),
        rs.date("created_at").toLocalDate
      )).single.apply()
  }

  def findByEmail(email: String)(implicit session: DBSession = AutoSession): Option[User] = {
    sql"SELECT * FROM users WHERE email = $email"
      .map(rs => User(
        Some(rs.int("id")),
        rs.string("username"),
        rs.string("password"),
        rs.string("email"),
        rs.date("created_at").toLocalDate
      )).single.apply()
  }
}
