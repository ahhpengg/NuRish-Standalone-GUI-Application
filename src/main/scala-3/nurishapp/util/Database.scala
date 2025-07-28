package nurishapp.util
import scalikejdbc.*
import nurishapp.model.User
import java.time.LocalDateTime


trait Database {
  val derbyDriverClassname = "org.apache.derby.jdbc.EmbeddedDriver"
  val dbURL = "jdbc:derby:nurishDB;create=true;"

  // Initialize JDBC driver & connection pool
  Class.forName(derbyDriverClassname)
  ConnectionPool.singleton(dbURL, "me", "mine")

  // Ad-hoc session provider
  given AutoSession = AutoSession

  // Initialize all tables
  def initializeTables(): Unit
}

// Singleton object for database initialization
object Database extends Database {
  def initializeTables(): Unit = {
    // Initialize all tables through their respective managers
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
          created_at TIMESTAMP NOT NULL,
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
        rs.timestamp("created_at").toLocalDateTime
      )).single.apply()
  }

  def findByUsername(username: String)(implicit session: DBSession = AutoSession): Option[User] = {
    sql"SELECT * FROM users WHERE username = $username"
      .map(rs => User(
        Some(rs.int("id")),
        rs.string("username"),
        rs.string("password"),
        rs.string("email"),
        rs.timestamp("created_at").toLocalDateTime
      )).single.apply()
  }

  def create(user: User)(implicit session: DBSession = AutoSession): Option[User] = {
    sql"""
      INSERT INTO users (username, password, email, created_at)
      VALUES (${user.username}, ${user.password}, ${user.email}, ${user.createdAt})
    """.update.apply()

    findByUsername(user.username)
  }
}
