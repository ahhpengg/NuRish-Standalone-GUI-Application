package nurishapp.model

import scalikejdbc.*
import nurishapp.util.Database

case class FoodDiet(
                     foodId: Int,
                     dietId: Int
                   ) extends Database

object FoodDiet extends Database {
  def initializeTable(): Unit = {
    DB.autoCommit { implicit session =>
      sql"""
      CREATE TABLE food_diets (
        food_id INTEGER NOT NULL,
        diet_id INTEGER NOT NULL,
        PRIMARY KEY (food_id, diet_id),
        CONSTRAINT fk_fd_food FOREIGN KEY (food_id) REFERENCES foods(id),
        CONSTRAINT fk_fd_diet FOREIGN KEY (diet_id) REFERENCES diets(id)
      )
      """.execute.apply()
    }
  }

  // --- CRUD ---
  def create(fd: FoodDiet)(implicit session: DBSession = AutoSession): FoodDiet = {
    sql"INSERT INTO food_diets (food_id, diet_id) VALUES (${fd.foodId}, ${fd.dietId})".update.apply()
    fd
  }

  def findByFood(foodId: Int)(implicit session: DBSession = AutoSession): List[FoodDiet] = {
    sql"SELECT * FROM food_diets WHERE food_id = $foodId"
      .map(rs => FoodDiet(rs.int("food_id"), rs.int("diet_id")))
      .list.apply()
  }

  def findByDiet(dietId: Int)(implicit session: DBSession = AutoSession): List[FoodDiet] = {
    sql"SELECT * FROM food_diets WHERE diet_id = $dietId"
      .map(rs => FoodDiet(rs.int("food_id"), rs.int("diet_id")))
      .list.apply()
  }

  def delete(fd: FoodDiet)(implicit session: DBSession = AutoSession): Boolean = {
    sql"DELETE FROM food_diets WHERE food_id = ${fd.foodId} AND diet_id = ${fd.dietId}".update.apply() > 0
  }
}
