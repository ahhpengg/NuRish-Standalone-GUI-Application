package nurishapp.util
import scalikejdbc.*
import nurishapp.model.{Diet, Food, FoodAidApplication, FoodDiet, User}

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
  def setupDB(): Unit = {
    if (!hasTable("users"))
      User.initializeTable()

    if (!hasTable("foods"))
      Food.initializeTable()

    if (!hasTable("diets"))
      Diet.initializeTable()

    if (!hasTable("food_diets"))
      FoodDiet.initializeTable()

    if (!hasTable("food_aid_applications"))
      FoodAidApplication.initializeTable()  
  }
}


