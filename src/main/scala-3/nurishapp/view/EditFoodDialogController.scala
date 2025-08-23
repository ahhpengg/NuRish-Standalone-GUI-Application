package nurishapp.view

import javafx.fxml.FXML
import javafx.scene.control.{Button, ComboBox, Label, TextField}
import javafx.stage.Stage
import nurishapp.model.Food
import scalafx.scene.control.Alert
import scalikejdbc.*

import scala.util.Try

class EditFoodDialogController {

  @FXML private var nameField: TextField = _
  @FXML private var categoryBox: ComboBox[String] = _
  @FXML private var servingUnitField: TextField = _
  @FXML private var calLabel: Label = _
  @FXML private var carbLabel: Label = _
  @FXML private var proteinLabel: Label = _
  @FXML private var fatLabel: Label = _
  @FXML private var preferredBox: ComboBox[String] = _
  @FXML private var messageLabel: Label = _
  @FXML private var saveBtn: Button = _

  private var stage: Stage = _
  private var currentFood: Food = _

  def initStage(st: Stage): Unit = this.stage = st

  @FXML private def initialize(): Unit = {
    preferredBox.getItems.setAll("Any", "Breakfast", "Lunch", "Dinner")
    categoryBox.getItems.setAll("Beverages", "Breads & Cereal", "Eggs", "Fruit", "Meat", "Rice & Noodles", "Seafood", "Vegetable", "Others")
    saveBtn.setOnAction(_ => handleSave())
  }

  def prefill(f: Food): Unit = {
    this.currentFood = f
    nameField.setText(f.name.value)
    categoryBox.setValue(f.category.value)
    servingUnitField.setText(f.servingUnit.value)
    calLabel.setText(f"${f.calories}%.2f")
    carbLabel.setText(f"${f.carbs}%.2f")
    proteinLabel.setText(f"${f.protein}%.2f")
    fatLabel.setText(f"${f.fat}%.2f")
    preferredBox.setValue(f.preferredMeal.value)
  }

  private def handleSave(): Unit = {
    val name = Option(nameField.getText).map(_.trim).getOrElse("")
    val category = Option(categoryBox.getValue).map(_.trim).getOrElse("")
    val servingUnit = Option(servingUnitField.getText).map(_.trim).getOrElse("")
    val preferred = Option(preferredBox.getValue).map(_.trim).getOrElse("Any")

    val errors = collection.mutable.ListBuffer[String]()
    if (name.isEmpty) errors += "Name is required"
    if (category.isEmpty) errors += "Category is required"
    if (servingUnit.isEmpty) errors += "Serving unit is required"

    // check duplicate name for same user
    currentFood.id.foreach { id =>
      DB readOnly { implicit s =>
        val exists = sql"""
          SELECT 1 FROM foods WHERE LOWER(name) = ${name.toLowerCase} AND id != $id
        """.map(_.int(1)).single.apply().isDefined
        if (exists) errors += "Food name already exists"
      }
    }

    if (errors.nonEmpty) {
      val alert = new Alert(Alert.AlertType.Error)
      alert.setTitle("Food Updates Error")
      alert.setHeaderText("Please fix the following")
      alert.setContentText(errors.mkString("\n"))
      alert.showAndWait()
      return
    }

    // update DB
    currentFood.id.foreach { id =>
      DB localTx { implicit s =>
        sql"""
          UPDATE foods SET name = $name, category = $category, serving_unit = $servingUnit,
             preferred_meal = $preferred
          WHERE id = $id
        """.update.apply()
      }
    }

    val info = new Alert(Alert.AlertType.Information)
    info.setTitle("Food Updates Saved")
    info.setHeaderText(null)
    info.setContentText("Your food has updated successfully!")
    info.showAndWait()

    if (stage != null) stage.close()
  }
}
