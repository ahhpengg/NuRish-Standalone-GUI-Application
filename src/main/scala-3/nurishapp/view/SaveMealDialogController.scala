package nurishapp.view

import javafx.fxml.FXML
import javafx.scene.control._
import javafx.stage.Stage
import javafx.collections.FXCollections
import nurishapp.util.SessionManager
import scalikejdbc._

import scala.jdk.CollectionConverters._

class SaveMealDialogController {

  // ====== FXML ======
  @FXML private var nameField: TextField = _
  @FXML private var categoryBox: ComboBox[String] = _
  @FXML private var servingUnitField: TextField = _
  @FXML private var preferredMealBox: ComboBox[String] = _

  // ====== State ======
  private var stage: Stage = _
  private var totals: (Double, Double, Double, Double) = (0,0,0,0) // (cal, C, P, F)
  private var savedName: Option[String] = None

  def init(cal: Double, carbs: Double, protein: Double, fat: Double): Unit = {
    totals = (cal, carbs, protein, fat)
    loadCategories()
    preferredMealBox.setItems(FXCollections.observableArrayList(java.util.List.of("Any","Breakfast","Lunch","Dinner")))
    preferredMealBox.getSelectionModel.select("Any")
  }

  def getSaved: Option[String] = savedName

  @FXML private def initialize(): Unit = {}
  def initStage(st: Stage): Unit = { this.stage = st }

  // ====== Actions ======
  @FXML private def handleConfirm(): Unit = {
    val rawName = Option(nameField.getText).map(_.trim).getOrElse("")
    val category = Option(categoryBox.getValue).map(_.trim).filter(_.nonEmpty).getOrElse("Others")
    val unit = Option(servingUnitField.getText).map(_.trim).getOrElse("")
    val preferred = Option(preferredMealBox.getValue).getOrElse("Any")

    if (rawName.isEmpty) { warn("Missing name", "Please provide a name."); return }
    if (unit.isEmpty)    { warn("Missing serving unit", "Please provide a serving unit."); return }

    val usernameSuffix = SessionManager.currentUser.map(_.username.value).getOrElse("guest")
    val finalName = s"$rawName (by $usernameSuffix)"

    // duplicate check under user view 
    if (existsFoodName(finalName)) {
      warn("Duplicate name", s""""$finalName" already exists. Please choose another name.""")
      return
    }

    val (cal, c, p, f) = totals
    val ok = insertFood(finalName, category, unit, preferred, cal, c, p, f)
    if (ok) {
      savedName = Some(finalName)
      stage.close()
    } else {
      warn("Error", "Failed to save your meal. Please try again.")
    }
  }

  @FXML private def handleCancel(): Unit = {
    savedName = None
    stage.close()
  }

  // ====== DB ======
  private def loadCategories(): Unit = DB readOnly { implicit s =>
    val sql =
      sql"""SELECT DISTINCT f.category FROM foods f WHERE ${userFilterSql("f")} ORDER BY 1"""
    val cats = sql.map(_.string(1)).list.apply()
    categoryBox.setItems(FXCollections.observableArrayList(("Others" :: cats).distinct.asJava))
    categoryBox.getSelectionModel.select("Others")
  }

  private def existsFoodName(name: String): Boolean = DB readOnly { implicit session =>
    sql"""
    SELECT 1 FROM foods
    WHERE name = $name
    FETCH FIRST 1 ROWS ONLY
  """.map(_.int(1)).single.apply().nonEmpty
  }

  private def insertFood(name: String, category: String, unit: String, pref: String,
                         cal: Double, carbs: Double, protein: Double, fat: Double): Boolean = DB localTx { implicit s =>
    val uidOpt = SessionManager.currentUserId
    val cols = sqls"name, category, serving_unit, calories, carbs, protein, fat, preferred_meal, recipes_by_user_id"
    val vals = sqls"${name}, ${category}, ${unit}, ${cal}, ${carbs}, ${protein}, ${fat}, ${pref}, ${uidOpt}"
    sql"INSERT INTO foods ($cols) VALUES ($vals)".update.apply() > 0
  }

  private def userFilterSql(alias: String): SQLSyntax = {
    val a = SQLSyntax.createUnsafely(alias)
    SessionManager.currentUserId match {
      case Some(uid) => sqls"($a.recipes_by_user_id IS NULL OR $a.recipes_by_user_id = $uid)"
      case None      => sqls"($a.recipes_by_user_id IS NULL)"
    }
  }

  private def warn(t: String, m: String): Unit = {
    val a = new Alert(Alert.AlertType.WARNING); a.setTitle(t); a.setHeaderText(null); a.setContentText(m)
    a.getDialogPane.setMinWidth(460); a.setResizable(true); a.showAndWait(); ()
  }
}
