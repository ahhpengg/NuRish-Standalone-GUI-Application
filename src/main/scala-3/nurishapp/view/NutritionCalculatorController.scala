package nurishapp.view

import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.{Parent, Scene}
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.scene.chart.PieChart
import javafx.stage.{Modality, Stage}
import javafx.collections.FXCollections
import javafx.scene.image.Image
import nurishapp.model.IngredientEntry

import scala.jdk.CollectionConverters.*

class NutritionCalculatorController {

  // ====== FXML ======
  @FXML private var root: javafx.scene.layout.BorderPane = _
  @FXML private var addIngredientBtn: Button = _
  @FXML private var removeLastBtn: Button = _
  @FXML private var clearBtn: Button = _
  @FXML private var calculateBtn: Button = _
  @FXML private var ingredientsBox: VBox = _

  @FXML private var resultsPane: VBox = _
  @FXML private var totalsTitle: Label = _
  @FXML private var totalCaloriesLabel: Label = _
  @FXML private var totalCarbsLabel: Label = _
  @FXML private var totalProteinLabel: Label = _
  @FXML private var totalFatLabel: Label = _
  @FXML private var macroChart: PieChart = _
  @FXML private var addMealBtn: Button = _
  @FXML private var nextBtn: Button = _

  // ====== State ======
  private var stage: Stage = _
  private var rootController: RootLayoutController = _
  private val ingredients = scala.collection.mutable.ArrayBuffer.empty[IngredientEntry]

  def setRootController(root: RootLayoutController): Unit = this.rootController = root
  def initStage(st: Stage): Unit = {
    this.stage = st
    stage.setTitle("NuRish - Nutrition Calculator")
  }

  @FXML private def initialize(): Unit = {
    resultsPane.setVisible(false)
  }

  // ====== UI Handlers ======
  @FXML private def handleAddIngredient(): Unit = {
    val (dlgRoot, ctrl, dlgStage) =
      openDialog[AddIngredientDialogController]("/nurishapp.view/AddIngredientDialog.fxml", "Add Ingredient")
    dlgStage.initOwner(root.getScene.getWindow)
    dlgStage.initModality(Modality.WINDOW_MODAL)
    dlgStage.setResizable(false)
    ctrl.initStage(dlgStage)
    ctrl.prefillFromUser() // loads categories & suggestions with current user filter
    dlgStage.showAndWait()

    ctrl.getResult.foreach { ing =>
      ingredients += ing
      ingredientsBox.getChildren.add(ing.displayNode)
      resultsPane.setVisible(false) // hide old results until re-calc
    }
  }

  @FXML private def handleRemoveLast(): Unit = {
    if (ingredients.nonEmpty) {
      val last = ingredients.remove(ingredients.size - 1)
      ingredientsBox.getChildren.remove(last.displayNode)
      resultsPane.setVisible(false)
    }
  }

  @FXML private def handleClearAll(): Unit = {
    ingredients.clear()
    ingredientsBox.getChildren.clear()
    resultsPane.setVisible(false)
  }

  @FXML private def handleCalculate(): Unit = {
    if (ingredients.isEmpty) {
      showAlert(Alert.AlertType.WARNING, "No ingredients", "Please add at least one ingredient before calculating.")
      return
    }
    val totCal = ingredients.map(_.totalCalories).sum
    val totC   = ingredients.map(_.totalCarbs).sum
    val totP   = ingredients.map(_.totalProtein).sum
    val totF   = ingredients.map(_.totalFat).sum

    totalsTitle.setText("Total nutrition")
    totalCaloriesLabel.setText(f"✔ Calories: $totCal%.0f kcal")
    totalCarbsLabel.setText(f"✔ Carbs: $totC%.1f g")
    totalProteinLabel.setText(f"✔ Protein: $totP%.1f g")
    totalFatLabel.setText(f"✔ Fat: $totF%.1f g")

    val data = FXCollections.observableArrayList[PieChart.Data](
      new PieChart.Data("Carbs",   Math.max(totC, 0.0)),
      new PieChart.Data("Protein", Math.max(totP, 0.0)),
      new PieChart.Data("Fat",     Math.max(totF, 0.0))
    )
    macroChart.setData(data)
    labelPieWithPercent(macroChart)
    resultsPane.setVisible(true)
  }

  @FXML private def handleAddMeal(): Unit = {
    // Need totals & then insert as a single Food row owned by the user
    if (!resultsPane.isVisible) {
      showAlert(Alert.AlertType.INFORMATION, "Nothing to add", "Please calculate nutrition of your meal first.")
      return
    }
    val (dlgRoot, ctrl, dlgStage) =
      openDialog[SaveMealDialogController]("/nurishapp.view/SaveMealDialog.fxml", "Save Meal")
    dlgStage.initOwner(root.getScene.getWindow)
    dlgStage.initModality(Modality.WINDOW_MODAL)
    dlgStage.setResizable(false)

    // pass totals
    val totCal = ingredients.map(_.totalCalories).sum
    val totC   = ingredients.map(_.totalCarbs).sum
    val totP   = ingredients.map(_.totalProtein).sum
    val totF   = ingredients.map(_.totalFat).sum
    ctrl.init(totCal, totC, totP, totF)
    ctrl.initStage(dlgStage)

    dlgStage.showAndWait()
    ctrl.getSaved.foreach { savedName =>
      showAlert(Alert.AlertType.INFORMATION, "Added!", s""""$savedName" was added into your account.""")
      handleResetForNext()
    }
  }

  @FXML private def handleResetForNext(): Unit = {
    ingredients.clear()
    ingredientsBox.getChildren.clear()
    resultsPane.setVisible(false)
  }

  // ====== Helpers ======
  private def openDialog[C](fxml: String, title: String): (Parent, C, Stage) = {
    val loader = new FXMLLoader(getClass.getResource(fxml))
    val root: Parent = loader.load()
    val ctrl = loader.getController.asInstanceOf[C]
    val st = new Stage()
    st.setTitle(title)
    st.getIcons.add(new Image(getClass.getResourceAsStream("/images/logo.png")))
    st.setScene(new Scene(root))
    (root, ctrl, st)
  }

  private def labelPieWithPercent(chart: PieChart): Unit = {
    // Update labels like "Carbs (34%)"
    val total = chart.getData.asScala.map(_.getPieValue).sum
    if (total <= 0) return
    chart.getData.asScala.foreach { d =>
      val pct = Math.round(d.getPieValue / total * 100.0)
      d.setName(s"${d.getName} ($pct%)")
    }
    chart.setLegendVisible(true) // legend shows the same names
  }

  private def showAlert(t: Alert.AlertType, title: String, msg: String): Unit = {
    val a = new Alert(t); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg)
    a.getDialogPane.setMinWidth(520); a.getDialogPane.setMinHeight(220); a.setResizable(true)
    a.showAndWait(); ()
  }
}

