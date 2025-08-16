package nurishapp.util
import scalikejdbc.*
import nurishapp.model.User
import scala.util.Try


trait Database {
  val derbyDriverClassname = "org.apache.derby.jdbc.EmbeddedDriver"
  val dbURL = "jdbc:derby:nurishDB;create=true;"

  // Initialize JDBC driver & connection pool
  Class.forName(derbyDriverClassname)
  ConnectionPool.singleton(dbURL, "peng", "peng")

  // Ad-hoc session provider
  given AutoSession = AutoSession

  // Initialize all tables
  def initializeTables(): Unit

  def hasTable(tableName: String): Boolean =
    DB getTable tableName match
      case Some(_) => true
      case None => false
}

// Singleton object for database initialization
object Database extends Database {
  def initializeTables(): Unit = {
    if (!hasTable("users"))
      UserDatabase.initializeTables()
  }
}

object UserDatabase extends Database {
  def initializeTables(): Unit = {
    DB.autoCommit { implicit session =>
      sql"""
        CREATE TABLE IF NOT EXISTS users (
          id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
          username VARCHAR(50) NOT NULL UNIQUE,
          password VARCHAR(255) NOT NULL,
          email VARCHAR(100) NOT NULL,
          created_at DATE NOT NULL,
          PRIMARY KEY (id)
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

  def create(user: User)(implicit session: DBSession = AutoSession): Try[User] = {
    Try {
      sql"""
        INSERT INTO users (username, password, email, created_at)
        VALUES (${user.username}, ${user.password}, ${user.email}, ${user.createdAt})
      """.update.apply()

      findByUsername(user.username.value).getOrElse(
        throw new RuntimeException("Failed to create user")
      )
    }
  }

  def update(user: User)(implicit session: DBSession = AutoSession): Try[User] = {
    Try {
      sql"""
          UPDATE users 
          SET username = ${user.username},
              password = ${user.password},
              email = ${user.email},
              created_at = ${user.createdAt}
          WHERE id = ${user.id.getOrElse(throw new RuntimeException("User ID not found"))}
        """.update.apply()

      findById(user.id.get).getOrElse(
        throw new RuntimeException("Failed to update user")
      )
    }
  }

  def delete(id: Int)(implicit session: DBSession = AutoSession): Try[Boolean] = {
    Try {
      val affected = sql"DELETE FROM users WHERE id = $id".update.apply()
      affected > 0
    }
  }
}

