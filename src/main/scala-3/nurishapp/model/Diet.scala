package nurishapp.model

import scalafx.beans.property.StringProperty
import scalikejdbc.*
import nurishapp.util.Database

case class Diet(
                 id: Option[Int],
                 nameS: String
               ) extends Database {
  val name = new StringProperty(this, "name", nameS)

  def getCurrentValues: Diet = copy(id = id, nameS = name.value)
}

object Diet extends Database {
  def initializeTable(): Unit = {
    DB.autoCommit { implicit session =>
      sql"""
      CREATE TABLE diets (
        id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
        name VARCHAR(50) NOT NULL UNIQUE
      )
      """.execute.apply()
    }
  }

  // --- CRUD ---
  def create(diet: Diet)(implicit session: DBSession = AutoSession): Diet = {
    val id = sql"INSERT INTO diets (name) VALUES (${diet.nameS})"
      .updateAndReturnGeneratedKey.apply().toInt
    diet.copy(id = Some(id))
  }

  def findById(id: Int)(implicit session: DBSession = AutoSession): Option[Diet] = {
    sql"SELECT * FROM diets WHERE id = $id"
      .map(rs => Diet(Some(rs.int("id")), rs.string("name")))
      .single.apply()
  }

  def delete(id: Int)(implicit session: DBSession = AutoSession): Boolean = {
    sql"DELETE FROM diets WHERE id = $id".update.apply() > 0
  }
}
