package nurishapp.view

import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.stage.Stage
import javafx.collections.{FXCollections, ObservableList}
import nurishapp.model.{Food, IngredientEntry}
import nurishapp.util.SessionManager
import scalikejdbc.*

import java.util.Locale
import scala.jdk.CollectionConverters.*

class IngredientEditDialogController {

  // ====== FXML ======
  @FXML private var categoryBox: ComboBox[String] = _
  @FXML private var foodBox: ComboBox[String] = _
  @FXML private var nutritionPerServingLabel: Label = _
  @FXML private var servingLabel: Label = _
  @FXML private var servingSpinner: javafx.scene.control.Spinner[Double] = _
  @FXML private var servingUnitValue: Label = _
  @FXML private var addBtn: Button = _
  @FXML private var cancelBtn: Button = _

  // ====== State ======
  private var stage: Stage = _
  private var allFoodNames: List[String] = Nil
  private var selectedFood: Option[Food] = None
  private val noResultText = "Sorry, cannot find the food you are searching for"

  def initStage(st: Stage): Unit = this.stage = st

  def prefillFromUser(): Unit = {
    loadCategories()
    categoryBox.getSelectionModel.select("Any")
    refreshFoodNames()
  }

  def getResult: Option[IngredientEntry] =
    result.asInstanceOf[Option[IngredientEntry]]
  private var result: Option[Any] = None

  @FXML private def initialize(): Unit = {
    // Category -> refresh foods
    categoryBox.getSelectionModel.selectedItemProperty.addListener((_, _, _) => refreshFoodNames())

    // Editable foodBox
    foodBox.setEditable(true)

    // Show all on click if empty
    foodBox.setOnMouseClicked(_ => {
      val t = Option(foodBox.getEditor.getText).map(_.trim).getOrElse("")
      if (t.isEmpty) { updateFoodSuggestions(""); if (!foodBox.isShowing) foodBox.show() }
    })

    // Filter as user types (only when non-empty)
    foodBox.getEditor.textProperty.addListener((_, _, newText) => {
      val text = Option(newText).getOrElse("").trim
      if (text.nonEmpty) {
        updateFoodSuggestions(text)
        if (!foodBox.isShowing) foodBox.show()
      } else {
        foodBox.getItems.clear(); foodBox.hide()
        selectedFood = None
        nutritionPerServingLabel.setText("-")
        servingUnitValue.setText("")
      }
    })

    // On pick: set editor & load per-serving nutrition
    foodBox.getSelectionModel.selectedItemProperty.addListener((_, _, sel) => {
      if (sel != null && sel != noResultText) {
        foodBox.setValue(sel)
        fetchFoodByExactName(sel, Option(categoryBox.getValue).getOrElse("Any")).foreach { f =>
          selectedFood = Some(f)
          nutritionPerServingLabel.setText(
            f"${f.calories}%.0f calories · ${f.carbs}%.1fg Carbs · ${f.protein}%.1fg Protein · ${f.fat}%.1fg Fat"
          )
          servingUnitValue.setText(f.servingUnitS)
        }
      }
    })

    // Serving spinner (min 0.5)
    servingSpinner.setValueFactory(
      new SpinnerValueFactory.DoubleSpinnerValueFactory(0.5, 100.0, 1.0, 0.5).asInstanceOf[SpinnerValueFactory[Double]]
    )
  }

  // ====== Actions ======
  @FXML private def handleAdd(): Unit = {
    val name = Option(foodBox.getValue).map(_.trim).getOrElse("")
    if (name.isEmpty) {
      showAlert(Alert.AlertType.WARNING, "Missing food", "Please choose a food.")
      return
    }
    val foodOpt = selectedFood.orElse(fetchFoodByExactName(name, Option(categoryBox.getValue).getOrElse("Any")))
    foodOpt match {
      case None =>
        showAlert(Alert.AlertType.INFORMATION, "Not found", s""""$name" is not in your foods.""")
      case Some(food) =>
        val servings = Option(servingSpinner.getValue).map(_.toDouble).getOrElse(1.0)
        val entry = IngredientEntry(food, servings) // type path only
        result = Some(entry)
        stage.close()
    }
  }

  @FXML private def handleCancel(): Unit = {
    result = None
    stage.close()
  }

  // ====== DB helpers ======
  private def loadCategories(): Unit = DB readOnly { implicit s =>
    val sql =
      sql"""
         SELECT DISTINCT f.category
         FROM foods f
         WHERE ${userFilterSql("f")}
         ORDER BY 1
      """
    val items = ("Any" :: sql.map(_.string(1)).list.apply()).distinct.asJava
    categoryBox.setItems(FXCollections.observableArrayList(items))
  }

  private def refreshFoodNames(): Unit = DB readOnly { implicit s =>
    val cat = Option(categoryBox.getValue).getOrElse("Any")
    val base =
      if (cat == "Any")
        sql"""SELECT f.name FROM foods f WHERE ${userFilterSql("f")} ORDER BY f.name"""
      else
        sql"""SELECT f.name FROM foods f WHERE ${userFilterSql("f")} AND f.category = ${cat} ORDER BY f.name"""
    allFoodNames = base.map(_.string(1)).list.apply().distinct
    updateFoodSuggestions(Option(foodBox.getEditor.getText).getOrElse(""))
  }

  private def fetchFoodByExactName(name: String, category: String): Option[Food] = DB readOnly { implicit s =>
    val conds = Seq(
      Some(sqls"${userFilterSql("f")}"),
      Some(sqls"LOWER(f.name) = ${name.toLowerCase(Locale.ROOT)}"),
      if (category != "Any") Some(sqls"f.category = ${category}") else None
    ).flatten
    val whereClause = sqls.where(sqls.joinWithAnd(conds: _*))
    sql"""SELECT f.* FROM foods f $whereClause""".map(rsToFood).single.apply()
  }

  private def userFilterSql(alias: String): SQLSyntax = {
    val a = SQLSyntax.createUnsafely(alias)
    SessionManager.currentUserId match {
      case Some(uid) => sqls"($a.recipes_by_user_id IS NULL OR $a.recipes_by_user_id = $uid)"
      case None      => sqls"($a.recipes_by_user_id IS NULL)"
    }
  }

  private def rsToFood(rs: WrappedResultSet): Food =
    Food(
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
    )

  // ====== Suggestions ======
  private def updateFoodSuggestions(filter: String): Unit = {
    val filtered =
      if (filter.trim.isEmpty) allFoodNames
      else {
        val f = filter.toLowerCase(Locale.ROOT)
        allFoodNames.filter(_.toLowerCase(Locale.ROOT).contains(f))
      }

    val items: ObservableList[String] =
      if (filtered.isEmpty) FXCollections.observableArrayList(java.util.List.of(noResultText))
      else FXCollections.observableArrayList(filtered.asJava)

    foodBox.setItems(items)

    if (filtered.isEmpty) {
      foodBox.setCellFactory(_ => new ListCell[String] {
        override def updateItem(item: String, empty: Boolean): Unit = {
          super.updateItem(item, empty)
          if (empty || item == null) setText(null)
          else { setText(item); setDisable(true); setStyle("-fx-opacity: 0.7;") }
        }
      })
    } else {
      foodBox.setCellFactory(_ => new ListCell[String] {
        override def updateItem(item: String, empty: Boolean): Unit = {
          super.updateItem(item, empty)
          setText(if (empty) null else item)
        }
      })
    }
  }

  private def showAlert(t: Alert.AlertType, title: String, msg: String): Unit = {
    val a = new Alert(t); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg)
    a.getDialogPane.setMinWidth(480); a.getDialogPane.setMinHeight(200); a.setResizable(true)
    a.showAndWait(); ()
  }
}
