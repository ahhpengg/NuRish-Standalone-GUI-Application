package nurishapp.view

import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.{Parent, Scene}
import javafx.scene.control._
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.{HBox, VBox}
import javafx.stage.{Modality, Stage}
import nurishapp.util.SessionManager
import nurishapp.model.Food
import scalikejdbc._
import java.time.format.DateTimeFormatter
import scala.jdk.CollectionConverters._
import scala.util.Try

class ProfileController {

  @FXML private var profileIcon: ImageView = _
  @FXML private var avatarStack: javafx.scene.layout.StackPane = _
  @FXML private var usernameValue: Label = _
  @FXML private var emailValue: Label = _
  @FXML private var createdAtValue: Label = _
  @FXML private var editInfoBtn: Button = _
  @FXML private var changePasswordBtn: Button = _
  @FXML private var recipesBox: VBox = _

  private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  private var stage: Stage = _
  private var rootController: RootLayoutController = _

  def setRootController(root: RootLayoutController): Unit = this.rootController = root

  def initStage(st: Stage): Unit = {
    this.stage = st
    stage.setTitle("NuRish - Profile")
  }

  @FXML private def initialize(): Unit = {
    val imageUrl = getClass.getResource("/images/profile_icon.png")
    if (imageUrl != null) {
      profileIcon.setImage(new Image(imageUrl.toString))
    }
    // load current user (if any)
    refreshProfileDisplay()
    // populate recipes
    refreshRecipesList()
  }

  def refreshProfileDisplay(): Unit = {
    SessionManager.currentUser.foreach { u =>
      // username/email/created
      usernameValue.setText(u.username.value)
      emailValue.setText(u.email.value)
      createdAtValue.setText(u.createdAt.value.format(dateFmt))
    }
  }

  @FXML
  private def openEditInfoDialog(): Unit = {
    val loader = new FXMLLoader(getClass.getResource("/nurishapp.view/EditInfoDialog.fxml"))
    val root = loader.load[Parent]()
    val ctrl = loader.getController.asInstanceOf[EditInfoDialogController]
    ctrl.prefillFromSession()

    val st = new Stage()
    st.initModality(Modality.APPLICATION_MODAL)
    st.setTitle("Edit Info")
    st.getIcons.add(new Image(getClass.getResourceAsStream("/images/logo.png")))
    st.setScene(new Scene(root))
    ctrl.initStage(st)
    st.showAndWait()

    // refresh display (if user saved)
    refreshProfileDisplay()
  }

  @FXML
  private def openChangePasswordDialog(): Unit = {
    val loader = new FXMLLoader(getClass.getResource("/nurishapp.view/ChangePasswordDialog.fxml"))
    val root = loader.load[Parent]()
    val ctrl = loader.getController.asInstanceOf[ChangePasswordDialogController]

    val st = new Stage()
    st.initModality(Modality.APPLICATION_MODAL)
    st.setTitle("Change Password")
    st.getIcons.add(new Image(getClass.getResourceAsStream("/images/logo.png")))
    st.setScene(new Scene(root))
    ctrl.initStage(st)
    st.showAndWait()
  }

  // ===== Recipes list handling =====
  def refreshRecipesList(): Unit = {
    recipesBox.getChildren.clear()
    val userIdOpt = SessionManager.currentUserId
    userIdOpt.foreach { uid =>
      DB readOnly { implicit s =>
        val foods = sql"SELECT * FROM foods WHERE recipes_by_user_id = $uid ORDER BY name"
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
          )).list.apply()

        foods.foreach { f =>
          val label = new Label(f.name.value)
          val editBtn = new Button("Edit")
          val delBtn = new Button("Remove")

          // Add CSS classes to buttons
          editBtn.getStyleClass.add("recipe-edit-btn")
          delBtn.getStyleClass.add("recipe-delete-btn")

          editBtn.setOnAction(_ => openEditFoodDialog(f))
          delBtn.setOnAction(_ => {
            val confirm = new Alert(Alert.AlertType.CONFIRMATION)
            confirm.setTitle("Confirm Delete")
            confirm.setHeaderText(null)
            confirm.setContentText(s"Delete ${f.name.value}?")
            val res = confirm.showAndWait()
            if (res.isPresent && res.get() == ButtonType.OK) {
              DB localTx { implicit s =>
                sql"DELETE FROM foods WHERE id = ${f.id.get}".update.apply()
              }
              refreshRecipesList()
            }
          })

          val hb = new HBox(10, label, editBtn, delBtn)
          hb.setStyle("-fx-padding: 6; -fx-alignment: center-left;")
          recipesBox.getChildren.add(hb)
        }
      }
    }
  }

  private def openEditFoodDialog(food: Food): Unit = {
    val loader = new FXMLLoader(getClass.getResource("/nurishapp.view/EditFoodDialog.fxml"))
    val root = loader.load[Parent]()
    val ctrl = loader.getController.asInstanceOf[EditFoodDialogController]
    ctrl.prefill(food)

    val st = new Stage()
    st.initModality(Modality.APPLICATION_MODAL)
    st.setTitle(s"Edit ${food.nameS}")
    st.getIcons.add(new Image(getClass.getResourceAsStream("/images/logo.png")))
    st.setScene(new Scene(root))
    ctrl.initStage(st)
    st.showAndWait()

    // refresh the recipes list and profile display
    refreshRecipesList()
    refreshProfileDisplay()
  }
}
