package nurishapp.model

import javafx.scene.layout.HBox
import javafx.scene.control.Label

case class IngredientEntry(food: Food, servings: Double) {
  val totalCalories: Double = food.calories * servings
  val totalCarbs: Double    = food.carbs * servings
  val totalProtein: Double  = food.protein * servings
  val totalFat: Double      = food.fat * servings
  val displayNode: HBox     = renderRow()

  private def renderRow(): HBox = {
    val row = new HBox(8.0)
    val name = new Label(s"${food.nameS}")
    name.setStyle("-fx-font-weight: bold;")

    val unitStr = s"${servings.formatted(if (servings.isValidInt) "%.0f" else "%.1f")} ${food.servingUnitS}"
    val qty  = new Label(s"$unitStr")
    val nutr = new Label(f"= ${totalCalories}%.0f calories · ${totalCarbs}%.1fg Carbs · ${totalProtein}%.1fg Protein · ${totalFat}%.1fg Fat")

    row.getChildren.addAll(name, new Label("—"), qty, new Label(" "), nutr)
    row
  }
}
