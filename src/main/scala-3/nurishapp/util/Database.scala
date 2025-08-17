package nurishapp.util
import scalikejdbc.*
import nurishapp.model.User

trait Database {
  val derbyDriverClassname = "org.apache.derby.jdbc.EmbeddedDriver"
  val dbURL = "jdbc:derby:nurishDB;create=true;"

  // Initialize JDBC driver & connection pool
  Class.forName(derbyDriverClassname)
  ConnectionPool.singleton(dbURL, "peng", "peng")

  // Ad-hoc session provider
  given AutoSession = AutoSession

  def hasTable(tableName: String): Boolean =
    DB getTable tableName match
      case Some(_) => true
      case None => false
}

// Singleton object for database initialization
object Database extends Database {
  def setupDB() = {
    if (!hasTable("users"))
      User.initializeTable()
  }
}


