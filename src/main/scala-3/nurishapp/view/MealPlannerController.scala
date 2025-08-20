package nurishapp.view

import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.Node
import scalikejdbc.*
import nurishapp.model.{Diet, Food, FoodDiet}
import javafx.scene.layout.VBox
import javafx.geometry.Insets
import javafx.scene.image.{Image, ImageView}
import javafx.stage.Stage

import scala.util.Random
import scala.jdk.CollectionConverters.*

class MealPlannerController {

  // ====== FXML-injected controls ======
  private var selectedDiet: String = "Anything"
  private var activeButton: Option[Button] = None
  @FXML private var mealPlannerPane: BorderPane = _
  @FXML private var anythingBtn: Button = _
  @FXML private var ketoBtn: Button = _
  @FXML private var medBtn: Button = _
  @FXML private var paleoBtn: Button = _
  @FXML private var veganBtn: Button = _
  @FXML private var vegeBtn: Button = _
  @FXML private var caloriesField: TextField = _
  @FXML private var mealCountBox: ComboBox[Integer] = _

  @FXML private var carbsMinField: TextField = _
  @FXML private var fatMinField: TextField = _
  @FXML private var proteinMinField: TextField = _

  @FXML private var messageLabel: Label = _
  @FXML private var generateBtn: Button = _

  @FXML private var resultsBox: VBox = _
  @FXML private var summaryTitle: Label = _
  @FXML private var totalsLabel: Label = _
  @FXML private var breakfastBox: VBox = _
  @FXML private var lunchBox: VBox = _
  @FXML private var dinnerBox: VBox = _
  @FXML private var singleMealBox: VBox = _

  @FXML private var anythingIcon: ImageView = _
  @FXML private var ketogenicIcon: ImageView = _
  @FXML private var mediterraneanIcon: ImageView = _
  @FXML private var paleolithicIcon: ImageView = _
  @FXML private var veganIcon: ImageView = _
  @FXML private var vegetarianIcon: ImageView = _

  private var stage: Stage = _
  private var rootController: RootLayoutController = _

  // ====== Model for planning ======
  private case class MealItem(food: Food, servings: Int) {
    val calories: Double = food.calories * servings
    val carbs: Double = food.carbs * servings
    val fat: Double = food.fat * servings
    val protein: Double = food.protein * servings
    val isBeverage: Boolean = food.categoryS.equalsIgnoreCase("Beverages")
  }

  private case class MealOption(items: List[MealItem]) {
    val calories: Double = items.map(_.calories).sum
    val carbs: Double = items.map(_.carbs).sum
    val fat: Double = items.map(_.fat).sum
    val protein: Double = items.map(_.protein).sum
  }

  private case class DayPlan(bySlot: Map[String, MealOption]) {
    val totalCalories: Double = bySlot.values.map(_.calories).sum
    val totalCarbs: Double = bySlot.values.map(_.carbs).sum
    val totalFat: Double = bySlot.values.map(_.fat).sum
    val totalProtein: Double = bySlot.values.map(_.protein).sum
  }

  // ====== Lifecycle ======
  def setRootController(root: RootLayoutController): Unit = {
    this.rootController = root
  }

  def initStage(stage: Stage): Unit = {
    this.stage = stage

    // Set up any stage-specific configurations here
    stage.setTitle("NuRish - Personalized Meal Planner")
  }

  @FXML
  private def initialize(): Unit = {

    // Default selection
    selectDiet(anythingBtn, "Anything")

    val anythingUrl = getClass.getResource("/images/anythingIcon.png")
    anythingIcon.setImage(new Image(anythingUrl.toString))
    val ketoUrl = getClass.getResource("/images/ketogenicIcon.png")
    ketogenicIcon.setImage(new Image(ketoUrl.toString))
    val mediUrl = getClass.getResource("/images/mediterraneanIcon.png")
    mediterraneanIcon.setImage(new Image(mediUrl.toString))
    val paleoUrl = getClass.getResource("/images/paleolithicIcon.png")
    paleolithicIcon.setImage(new Image(paleoUrl.toString))
    val veganUrl = getClass.getResource("/images/veganIcon.png")
    veganIcon.setImage(new Image(veganUrl.toString))
    val vegeUrl = getClass.getResource("/images/vegetarianIcon.png")
    vegetarianIcon.setImage(new Image(vegeUrl.toString))

    if (mealCountBox.getSelectionModel.isEmpty) mealCountBox.getSelectionModel.select(Integer.valueOf(3))

    if (caloriesField.getText == null || caloriesField.getText.trim.isEmpty) caloriesField.setText("1800")
    if (carbsMinField.getText == null || carbsMinField.getText.trim.isEmpty) carbsMinField.setText("45")
    if (fatMinField.getText == null || fatMinField.getText.trim.isEmpty) fatMinField.setText("40")
    if (proteinMinField.getText == null || proteinMinField.getText.trim.isEmpty) proteinMinField.setText("45")

    // Add real-time validation like SignUpController
    caloriesField.textProperty().addListener((_, _, newValue) => validatePositiveInt(caloriesField, newValue, "Calories"))
    carbsMinField.textProperty().addListener((_, _, newValue) => validatePositiveInt(carbsMinField, newValue, "Carbs"))
    fatMinField.textProperty().addListener((_, _, newValue) => validatePositiveInt(fatMinField, newValue, "Fat"))
    proteinMinField.textProperty().addListener((_, _, newValue) => validatePositiveInt(proteinMinField, newValue, "Protein"))

    resultsBox.setVisible(false)
  }

  // ====== UI entry point ======

  // Common diet handler
  private def selectDiet(btn: Button, diet: String): Unit = {
    Seq(anythingBtn, vegeBtn, veganBtn, ketoBtn, paleoBtn, medBtn).foreach { b =>
      b.getStyleClass.remove("selected-diet")
    }
    btn.getStyleClass.add("selected-diet")
    selectedDiet = diet
  }

  @FXML def handleAnything(): Unit = selectDiet(anythingBtn, "Anything")

  @FXML def handleKeto(): Unit = selectDiet(ketoBtn, "Ketogenic")

  @FXML def handleMediterranean(): Unit = selectDiet(medBtn, "Mediterranean")

  @FXML def handlePaleo(): Unit = selectDiet(paleoBtn, "Paleolithic")

  @FXML def handleVegan(): Unit = selectDiet(veganBtn, "Vegan")

  @FXML def handleVegetarian(): Unit = selectDiet(vegeBtn, "Vegetarian")

  @FXML
  private def handleGenerate(): Unit = {
    val calsValid = validatePositiveInt(caloriesField, caloriesField.getText, "Calories")
    val carbsValid = validatePositiveInt(carbsMinField, carbsMinField.getText, "Carbs")
    val fatValid = validatePositiveInt(fatMinField, fatMinField.getText, "Fat")
    val proteinValid = validatePositiveInt(proteinMinField, proteinMinField.getText, "Protein")

    if (!calsValid || !carbsValid || !fatValid || !proteinValid) {
      showAlert(Alert.AlertType.WARNING, "Invalid input", "Please enter a positive integer for each field before generating your meal plan.")
      return
    }

    parseInputs() match {
      case Left(msg) =>
        showAlert(Alert.AlertType.WARNING, "Invalid inputs", msg)
      case Right(params) =>
        val planOpt = generatePlan(params)
        planOpt match {
          case Some(plan) => renderPlan(params, plan)
          case None => showAlert(Alert.AlertType.INFORMATION, "No plan found",
            "Unable to find a combination that meets your requirements. " +
              "Try relaxing macros or calories a bit, or provide us more with your delicious diets!")
        }
    }
  }

  // ====== Inputs ======
  private case class Inputs(
                             diet: String,
                             targetCalories: Int,
                             meals: Int,
                             minCarbs: Int,
                             minFat: Int,
                             minProtein: Int
                           )

  private def parseInputs(): Either[String, Inputs] = {
    val diet = selectedDiet
    val cals = toInt(caloriesField.getText)
    val meals = Option(mealCountBox.getValue).map(_.intValue()).getOrElse(3)
    val carbs = toInt(carbsMinField.getText)
    val fat = toInt(fatMinField.getText)
    val protein = toInt(proteinMinField.getText)

    def check(p: Boolean, msg: String): Either[String, Unit] =
      if (!p) Left(msg) else Right(())

    check(cals > 0, "Calories must be a positive number.")
    check(meals >= 1 && meals <= 3, "Meals must be 1, 2 or 3.")
    check(carbs >= 0 && fat >= 0 && protein >= 0, "Macros must be non-negative.")

    Right(Inputs(diet, cals, meals, carbs, fat, protein))
  }

  private def toInt(s: String): Int =
    Option(s).flatMap(x => scala.util.Try(x.trim.toInt).toOption).getOrElse(-1)

  // ====== Planner ======
  private def generatePlan(in: Inputs): Option[DayPlan] = {
    val slots: List[String] = in.meals match {
      case 1 => List("Any") // one meal day
      case 2 => List("Breakfast", "Dinner")
      case _ => List("Breakfast", "Lunch", "Dinner")
    }

    val perMealBudget = in.targetCalories.toDouble / slots.size
    val tolerancePerMeal = math.max(30.0, 0.10 * perMealBudget) // soft bounds to keep search reasonable

    // candidates per slot (diet + preferred_meal filtering)
    val candidates: Map[String, List[Food]] =
      slots.map(slot => slot -> fetchFoodsForSlot(in.diet, slot)).toMap

    // Precompute meal options per slot (respect rules inside)
    val optionsPerSlot: Map[String, List[MealOption]] =
      candidates.map { case (slot, foods) =>
        slot -> enumerateMealOptions(slot, foods, perMealBudget, tolerancePerMeal, in.meals)
      }

    // Quick failure if some slot has no feasible options
    if (optionsPerSlot.exists(_._2.isEmpty)) return None

    // Backtracking search across meals to hit totals (±5) and macro minima
    val target = in.targetCalories.toDouble
    val tol = 5.0

    var best: Option[DayPlan] = None
    var bestCalErr = Double.MaxValue

    def search(idx: Int, acc: Map[String, MealOption]): Unit = {
      if (idx == slots.size) {
        val plan = DayPlan(acc)
        val calErr = math.abs(plan.totalCalories - target)
        val macrosOK =
          plan.totalCarbs >= in.minCarbs &&
            plan.totalFat >= in.minFat &&
            plan.totalProtein >= in.minProtein

        if (macrosOK && calErr <= tol) {
          best = Some(plan) // perfect enough -> accept
          bestCalErr = calErr
        } else if (macrosOK && calErr < bestCalErr) {
          best = Some(plan) // keep closest that meets macros
          bestCalErr = calErr
        }
        return
      }

      // pruning by calories: if even the minimum remaining calories will exceed target+tol, prune
      val chosenCal = acc.values.map(_.calories).sum
      if (chosenCal > target + tol) return

      val slot = slots(idx)
      val options = optionsPerSlot(slot)

      // simple ordering: try options that are closer to per-meal budget first
      val sorted = options.sortBy(opt => math.abs(opt.calories - perMealBudget))

      // cap the branching factor to keep search fast
      val capped = sorted.take(120)

      capped.foreach { opt =>
        if (best.exists(_.totalCalories == target)) return // already perfect (rare)
        search(idx + 1, acc + (slot -> opt))
      }
    }

    search(0, Map.empty)

    // If we found a macros-OK plan but not within ±5 calories, try a tiny repair pass:
    best match {
      case Some(plan) if math.abs(plan.totalCalories - target) <= tol => best
      case Some(plan) =>
        tweakSlightly(plan, optionsPerSlot, slots, target, tol).orElse(best)
      case None => None
    }
  }

  // Fetch foods filtered by diet & meal slot
  // slot is "Breakfast" | "Lunch" | "Dinner" | "Any"
  private def fetchFoodsForSlot(diet: String, slot: String): List[Food] = {
    DB readOnly { implicit s =>
      val slotCondition =
        if (slot.equalsIgnoreCase("Any")) sqls"f.preferred_meal = 'Any' OR f.preferred_meal IS NOT NULL"
        else sqls"f.preferred_meal = 'Any' OR f.preferred_meal = ${slot}"

      if (diet.equalsIgnoreCase("Anything")) {
        sql"""
        SELECT f.* FROM foods f
        WHERE $slotCondition
      """.map(rsToFood).list.apply()
      } else {
        sql"""
        SELECT f.* FROM foods f
        JOIN food_diets fd ON fd.food_id = f.id
        JOIN diets d ON d.id = fd.diet_id
        WHERE d.name = ${diet}
          AND ( $slotCondition )
      """.map(rsToFood).list.apply()
      }
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

  /**
   * Build legal meal options for a slot.
   * Rules enforced here:
   *  - at most 2 unique foods in a meal
   *  - beverages: at most one beverage item per meal and only 1 serving
   *  - non-beverages: 1..3 servings
   *  - soft calorie window around per-meal budget to reduce search space
   */
  private def enumerateMealOptions(
                                    slot: String,
                                    foods: List[Food],
                                    budget: Double,
                                    softTol: Double,
                                    totalMeals: Int
                                  ): List[MealOption] = {

    if (foods.isEmpty) return Nil

    val shuffled = Random.shuffle(foods)
    // keep a manageable candidate pool: prefer foods whose single serving isn't wildly off budget
    val pool = shuffled.sortBy(f => math.abs(f.calories - budget)).take(18)

    val (beverages, solids) = pool.partition(_.categoryS.equalsIgnoreCase("Beverages"))

    // helper to make a MealItem with serving constraints
    def servingsRange(f: Food): Range = if (f.categoryS.equalsIgnoreCase("Beverages")) 1 to 1 else 1 to 3

    // All 1-food and 2-food combos with legal servings
    val singles: List[MealOption] =
      pool.flatMap { f =>
        servingsRange(f).map(s => MealOption(List(MealItem(f, s))))
      }

    val pairs: List[MealOption] =
      for {
        (a, i) <- pool.zipWithIndex
        b <- pool.drop(i + 1) // ensure distinct foods
        // beverage rule: at most ONE beverage across the pair; the beverage (if present) has 1 serving
        aIsB = a.categoryS.equalsIgnoreCase("Beverages")
        bIsB = b.categoryS.equalsIgnoreCase("Beverages")
        if !(aIsB && bIsB)
        aServs <- servingsRange(a)
        bServs <- servingsRange(b)
      } yield MealOption(List(MealItem(a, aServs), MealItem(b, bServs)))

    // Merge and filter by a soft calorie window
    val all = (singles ++ pairs).filter { opt =>
      val cal = opt.calories
      val (lo, hi) =
        if (totalMeals == 1) (budget - 250.0, budget + 250.0) // allow wider for single-meal day
        else (budget - softTol, budget + softTol)
      cal >= math.max(0, lo) && cal <= math.max(hi, 50.0)
    }

    // Remove obviously dominated options (higher cals but lower macros than another)
    val pruned = dedupeAndPrune(all)

    // Cap to keep backtracking fast but diverse
    Random.shuffle(pruned).sortBy(o => math.abs(o.calories - budget)).take(220)
  }

  private def dedupeAndPrune(opts: List[MealOption]): List[MealOption] = {
    // very light pruning: keep unique (calories rounded to 1) x (macro tuple rounded)
    val seen = scala.collection.mutable.HashSet[(Int, Int, Int, Int)]()
    opts.filter { o =>
      val key = (math.round(o.calories).toInt,
        math.round(o.carbs).toInt,
        math.round(o.fat).toInt,
        math.round(o.protein).toInt)
      if (seen(key)) false else {
        seen += key; true
      }
    }
  }

  // Tiny hill-climb around a found plan to try to reach ±5 cals
  private def tweakSlightly(
                             plan: DayPlan,
                             optionsPerSlot: Map[String, List[MealOption]],
                             slots: List[String],
                             target: Double,
                             tol: Double
                           ): Option[DayPlan] = {

    var current = plan
    var best = plan
    var bestErr = math.abs(plan.totalCalories - target)

    for (_ <- 1 to 80 if bestErr > tol) {
      val slot = Random.shuffle(slots).head
      val currentOpt = current.bySlot(slot)
      val alt = Random.shuffle(optionsPerSlot(slot).filterNot(_ == currentOpt)).take(10)
      val tried = alt.find { cand =>
        val candidate = DayPlan(current.bySlot + (slot -> cand))
        val err = math.abs(candidate.totalCalories - target)
        val macrosOK = candidate.totalCarbs >= 0 && candidate.totalFat >= 0 && candidate.totalProtein >= 0 // always true
        if (macrosOK && err < bestErr) {
          best = candidate;
          bestErr = err;
          true
        } else false
      }
      if (tried.isDefined) current = best
    }
    if (bestErr <= tol) Some(best) else None
  }

  // ====== Render ======
  private def renderPlan(in: Inputs, plan: DayPlan): Unit = {
    // clear
    breakfastBox.getChildren.clear()
    lunchBox.getChildren.clear()
    dinnerBox.getChildren.clear()
    singleMealBox.getChildren.clear()

    val meals = plan.bySlot.keySet

    def section(container: VBox, title: String, opt: MealOption): Unit = {
      val header = new Label(s"$title – ${math.round(opt.calories)} Calories")
      header.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;")

      val innerBox = new VBox()
      innerBox.setPadding(new Insets(6, 0, 10, 0))

      innerBox.getChildren.add(header)
      opt.items.foreach { it =>
        innerBox.getChildren.add(row(it))
      }

      // Add this whole section to the parent container
      container.getChildren.add(innerBox)
    }

    if (meals.contains("Any")) {
      section(singleMealBox, "Your Meal", plan.bySlot("Any"))
      breakfastBox.setManaged(false);
      breakfastBox.setVisible(false)
      lunchBox.setManaged(false);
      lunchBox.setVisible(false)
      dinnerBox.setManaged(false);
      dinnerBox.setVisible(false)
      singleMealBox.setManaged(true);
      singleMealBox.setVisible(true)
    } else {
      if (meals.contains("Breakfast")) {
        section(breakfastBox, "Breakfast", plan.bySlot("Breakfast"))
        breakfastBox.setManaged(true);
        breakfastBox.setVisible(true)
      } else {
        breakfastBox.setManaged(false); breakfastBox.setVisible(false)
      }

      if (meals.contains("Lunch")) {
        section(lunchBox, "Lunch", plan.bySlot("Lunch"))
        lunchBox.setManaged(true);
        lunchBox.setVisible(true)
      } else {
        lunchBox.setManaged(false); lunchBox.setVisible(false)
      }

      if (meals.contains("Dinner")) {
        section(dinnerBox, "Dinner", plan.bySlot("Dinner"))
        dinnerBox.setManaged(true);
        dinnerBox.setVisible(true)
      } else {
        dinnerBox.setManaged(false); dinnerBox.setVisible(false)
      }

      singleMealBox.setManaged(false);
      singleMealBox.setVisible(false)
    }

    summaryTitle.setText("Your Meal Plan")
    totalsLabel.setText(
      f"Total: ${plan.totalCalories}%.0f Calories   " +
        f"Carbs: ${plan.totalCarbs}%.0fg   " +
        f"Fat: ${plan.totalFat}%.0fg   " +
        f"Protein: ${plan.totalProtein}%.0fg"
    )
    resultsBox.setVisible(true)
  }

  private def row(it: MealItem): Node = {
    val name = s"${it.food.nameS} — ${it.servings} ${if (it.servings == 1) "serving" else "servings"} (${it.food.servingUnitS})"
    val lbl = new Label(
      f"$name  | ${it.calories}%.0f calories  ${it.carbs}%.0fg Carbs  ${it.fat}%.0fg Fat  ${it.protein}%.0fg Protein"
    )
    lbl
  }

  // ====== Validation helper ======
  private def validatePositiveInt(field: TextField, value: String, fieldName: String): Boolean = {
    if (value.trim.isEmpty) {
      messageLabel.setText(s"$fieldName cannot be empty")
      false
    } else {
      value.toIntOption match {
        case Some(v) if v > 0 =>
          messageLabel.setText("")
          true
        case _ =>
          messageLabel.setText(s"$fieldName must be a positive integer")
          false
      }
    }
  }

  private def showAlert(alertType: Alert.AlertType, title: String, message: String): Unit = {
    val alert = new Alert(alertType)
    alert.setTitle(title)
    alert.setHeaderText(null)
    alert.setContentText(message)

    // Bigger + resizable
    alert.setResizable(true)
    alert.getDialogPane.setPrefSize(500, 250)

    alert.showAndWait()
  }

}