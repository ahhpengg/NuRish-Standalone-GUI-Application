package nurishapp.view

import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.image.{Image, ImageView}
import javafx.scene.chart.PieChart
import javafx.scene.layout.VBox
import scalikejdbc.*
import nurishapp.model.Food
import javafx.collections.FXCollections
import javafx.stage.Stage

import java.util.Locale
import scala.jdk.CollectionConverters.*
import nurishapp.util.SessionManager

class FoodSearchController {

  // ====== FXML ======
  @FXML private var foodSearchPane: javafx.scene.layout.BorderPane = _
  @FXML private var categoryBox: ComboBox[String] = _
  @FXML private var foodBox: ComboBox[String] = _
  @FXML private var searchBtn: Button = _

  @FXML private var resultsPane: VBox = _
  @FXML private var foodImage: ImageView = _
  @FXML private var nameValue: Label = _
  @FXML private var categoryValue: Label = _
  @FXML private var servingUnitValue: Label = _
  @FXML private var caloriesValue: Label = _
  @FXML private var carbsValue: Label = _
  @FXML private var proteinValue: Label = _
  @FXML private var fatValue: Label = _
  @FXML private var macroChart: PieChart = _

  // ====== State ======
  private var stage: Stage = _
  private var rootController: RootLayoutController = _
  private var allFoodNames: List[String] = Nil
  private val noResultText = "Sorry, cannot find the food you are searching for"
  
  def refreshForCurrentUser(): Unit = {
    loadCategories()
    refreshFoodNames()
  }

  def setRootController(root: RootLayoutController): Unit = {
    this.rootController = root
  }

  def initStage(stage: Stage): Unit = {
    this.stage = stage
    refreshForCurrentUser()
    
    stage.setTitle("NuRish - Food Search")
  }

  @FXML
  private def initialize(): Unit = {

    // Category default handled after setCurrentUserId when categories are known
    categoryBox.setPromptText("All")
    categoryBox.getSelectionModel.selectedItemProperty.addListener((_, _, _) => {
      refreshFoodNames()
    })

    // Make foodBox behave like TextField with suggestions
    foodBox.setEditable(true)
    // Show all on click if empty
    foodBox.setOnMouseClicked(_ => {
      if (Option(foodBox.getEditor.getText).forall(_.trim.isEmpty)) {
        updateFoodSuggestions("")
        if (!foodBox.isShowing) foodBox.show()
      }
    })
    // Filter as user types
    foodBox.getEditor.textProperty.addListener((_, _, newText) => {
      val text = Option(newText).map(_.trim).getOrElse("")
      if (text.isEmpty) {
        foodBox.getItems.clear()
        foodBox.hide()
      } else {
        updateFoodSuggestions(text)
        if (!foodBox.isShowing) foodBox.show()
      }
    })

    // When user picks an item, auto-fill editor safely
    foodBox.getSelectionModel.selectedItemProperty.addListener((_, _, sel) => {
      if (sel != null && sel != noResultText) {
        foodBox.setValue(sel) // <-- no more IllegalArgumentException
      }
    })

    resultsPane.setVisible(false)

  }

  // ====== UI handlers ======
  @FXML
  private def handleSearch(): Unit = {
    val q = Option(foodBox.getEditor.getText).map(_.trim).getOrElse("")
    if (q.isEmpty) {
      showAlert(Alert.AlertType.WARNING, "Missing food", "Please choose a food to search.")
      return
    }

    val cat = Option(categoryBox.getValue).filter(_.nonEmpty).getOrElse("All")

    fetchFoodByExactName(q, cat) match {
      case Some(food) => renderFood(food)
      case None       => showAlert(Alert.AlertType.INFORMATION, "Not found",
        s""""$q" does not exist in your foods. Try selecting from the dropdown.""")
    }
    // Clear input & suggestions after search attempt
    foodBox.getEditor.clear()
    foodBox.setValue(null)
    foodBox.getItems.clear()
    foodBox.hide()
  }

  // ====== DB ======
  private def loadCategories(): Unit = DB readOnly { implicit s =>
    val catSql =
      sql"""
         SELECT DISTINCT f.category
         FROM foods f
         WHERE ${userFilterSql("f")}
         ORDER BY 1
       """
    val cats = catSql.map(_.string(1)).list.apply()
    val items = ("All" :: cats).distinct.asJava
    categoryBox.setItems(FXCollections.observableArrayList(items))
    categoryBox.getSelectionModel.select("All")
  }

  private def refreshFoodNames(): Unit = DB readOnly { implicit s =>
    val cat = Option(categoryBox.getValue).getOrElse("All")
    val sqlBase = if (cat == "All") {
      sql"""
        SELECT f.name FROM foods f
        WHERE ${userFilterSql("f")}
        ORDER BY f.name
      """
    } else {
      sql"""
        SELECT f.name FROM foods f
        WHERE ${userFilterSql("f")} AND f.category = ${cat}
        ORDER BY f.name
      """
    }
    allFoodNames = sqlBase.map(_.string(1)).list.apply().distinct
    updateFoodSuggestions(Option(foodBox.getEditor.getText).getOrElse(""))
  }

  private def fetchFoodByExactName(name: String, category: String): Option[Food] = DB readOnly { implicit s =>
    // Always filter by user condition and exact name
    val conditions = Seq(
      Some(sqls"${userFilterSql("f")}"), // this expands into your user filter
      Some(sqls"LOWER(f.name) = ${name.toLowerCase(Locale.ROOT)}"),
      if (category != "All") Some(sqls"f.category = ${category}") else None
    ).flatten

    val whereClause = sqls.where(sqls.joinWithAnd(conditions: _*))

    sql"""
    SELECT f.* FROM foods f
    $whereClause
  """.map(rsToFood).single.apply()
  }

  // Only allow system foods (NULL) or foods created by the current user
  private def userFilterSql(alias: String): SQLSyntax = {
    val a = SQLSyntax.createUnsafely(alias) // safe because 'alias' is a hardcoded string like "f"
    SessionManager.currentUserId match {
      case Some(uid) =>
        // show system foods (NULL) OR foods created by this user
        sqls"($a.recipes_by_user_id IS NULL OR $a.recipes_by_user_id = $uid)"
      case None =>
        // no user logged in -> only system foods
        sqls"($a.recipes_by_user_id IS NULL)"
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

    if (filtered.isEmpty) {
      foodBox.setItems(FXCollections.observableArrayList(noResultText))

      // Grey out "no results" and prevent selecting
      foodBox.setCellFactory(_ => new ListCell[String] {
        override def updateItem(item: String, empty: Boolean): Unit = {
          super.updateItem(item, empty)
          if (empty || item == null) {
            setText(null)
          } else {
            setText(item)
            setDisable(true) // disables clicking
            setStyle("-fx-text-fill: grey; -fx-font-style: italic;")
          }
        }
      })

      foodBox.getSelectionModel.clearSelection() // prevent auto-select
    } else {
      foodBox.setItems(FXCollections.observableArrayList(filtered.asJava))
      foodBox.setCellFactory(_ => new ListCell[String] {
        override def updateItem(item: String, empty: Boolean): Unit = {
          super.updateItem(item, empty)
          setText(if (empty || item == null) null else item)
        }
      })
    }
  }

  // ====== Render result ======
  private def renderFood(f: Food): Unit = {
    nameValue.setText(f.nameS)
    categoryValue.setText(f.categoryS)
    servingUnitValue.setText(f.servingUnitS)
    caloriesValue.setText(f.calories.formatted("%.0f"))
    carbsValue.setText(f.carbs.formatted("%.2f"))
    proteinValue.setText(f.protein.formatted("%.2f"))
    fatValue.setText(f.fat.formatted("%.2f"))

    loadFoodImage(f)

    // Compute total for percentage
    val total = Math.max(f.carbs, 0.0) + Math.max(f.protein, 0.0) + Math.max(f.fat, 0.0)

    val data = FXCollections.observableArrayList[PieChart.Data](
      new PieChart.Data(
        s"Carbs (${if (total > 0) (f.carbs / total * 100).formatted("%.1f") else "0"}%)",
        Math.max(f.carbs, 0.0)
      ),
      new PieChart.Data(
        s"Protein (${if (total > 0) (f.protein / total * 100).formatted("%.1f") else "0"}%)",
        Math.max(f.protein, 0.0)
      ),
      new PieChart.Data(
        s"Fat (${if (total > 0) (f.fat / total * 100).formatted("%.1f") else "0"}%)",
        Math.max(f.fat, 0.0)
      )
    )
    macroChart.setData(data)

    resultsPane.setVisible(true)
  }

  private def loadFoodImage(f: Food): Unit = {
    // Try by id, then by slugified name, else placeholder
    def toSlug(s: String): String =
      s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "")

    val candidates = List(
      f.id.map(i => s"/images/foods/$i.png"),
      Some(s"/images/foods/${toSlug(f.nameS)}.png"),
      Some("/images/foods/null.png")
    ).flatten

    val imgOpt = candidates.view
      .map(getClass.getResource)
      .find(_ != null)
      .map(u => new Image(u.toString, true))

    foodImage.setImage(imgOpt.orNull)
  }

  // ====== Alerts ======
  private def showAlert(kind: Alert.AlertType, title: String, msg: String): Unit = {
    val a = new Alert(kind)
    a.setTitle(title)
    a.setHeaderText(null)
    a.setContentText(msg)
    a.getDialogPane.setMinWidth(520)
    a.getDialogPane.setMinHeight(220)
    a.setResizable(true)
    a.showAndWait()
    ()
  }
}