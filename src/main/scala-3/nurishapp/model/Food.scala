package nurishapp.model

import scalafx.beans.property.StringProperty
import scalikejdbc.*
import nurishapp.util.Database

// ---------------- FOOD ----------------
case class Food(
                 id: Option[Int],
                 nameS: String,
                 categoryS: String,
                 servingUnitS: String,
                 calories: Double,
                 carbs: Double,
                 protein: Double,
                 fat: Double,
                 preferredMealS: String,   // Any, Breakfast, Lunch, Dinner
                 recipesByUserId: Option[Int] // FK -> users.id (nullable)
               ) extends Database {

  val name = new StringProperty(this, "name", nameS)
  val category = new StringProperty(this, "category", categoryS)
  val servingUnit = new StringProperty(this, "servingUnit", servingUnitS)
  val preferredMeal = new StringProperty(this, "preferredMeal", preferredMealS)

  def getCurrentValues: Food = copy(
    id = id,
    nameS = name.value,
    categoryS = category.value,
    servingUnitS = servingUnit.value,
    preferredMealS = preferredMeal.value,
    recipesByUserId = recipesByUserId
  )
}

object Food extends Database {

  def initializeTable(): Unit = {
    DB.autoCommit { implicit session =>
      sql"""
      CREATE TABLE foods (
        id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
        name VARCHAR(100) NOT NULL,
        category VARCHAR(50) NOT NULL,
        serving_unit VARCHAR(50) NOT NULL,
        calories DOUBLE NOT NULL,
        carbs DOUBLE NOT NULL,
        protein DOUBLE NOT NULL,
        fat DOUBLE NOT NULL,
        preferred_meal VARCHAR(20) NOT NULL,
        recipes_by_user_id INTEGER,
        CONSTRAINT fk_food_user FOREIGN KEY (recipes_by_user_id) REFERENCES users(id)
      )
      """.execute.apply()
    }
  }

  // --- CRUD methods ---
  def create(food: Food)(implicit session: DBSession = AutoSession): Food = {
    val id = sql"""
      INSERT INTO foods (name, category, serving_unit, calories, carbs, protein, fat, preferred_meal, recipes_by_user_id)
      VALUES (${food.nameS}, ${food.categoryS}, ${food.servingUnitS}, ${food.calories}, ${food.carbs}, ${food.protein}, ${food.fat}, ${food.preferredMealS}, ${food.recipesByUserId})
    """.updateAndReturnGeneratedKey.apply().toInt
    food.copy(id = Some(id))
  }

  def findById(id: Int)(implicit session: DBSession = AutoSession): Option[Food] = {
    sql"SELECT * FROM foods WHERE id = $id"
      .map(rs => Food(
        Some(rs.int("id")),
        rs.string("name"),
        rs.string("category"),
        rs.string("serving_unit"),
        rs.double("calories"),
        rs.double("carbs"),
        rs.double("protein"),
        rs.double("fat"),
        rs.string("preferred_meal"),
        rs.intOpt("recipes_by_user_id")
      )).single.apply()
  }

  def delete(id: Int)(implicit session: DBSession = AutoSession): Boolean = {
    sql"DELETE FROM foods WHERE id = $id".update.apply() > 0
  }
}

