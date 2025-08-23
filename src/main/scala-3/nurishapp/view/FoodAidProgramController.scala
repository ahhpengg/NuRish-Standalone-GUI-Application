package nurishapp.view

import javafx.fxml.FXML
import javafx.scene.control.{Button, Hyperlink, Label}
import javafx.scene.image.{Image, ImageView}
import javafx.stage.{Modality, Stage}
import javafx.scene.{Parent, Scene}
import javafx.fxml.FXMLLoader
import javafx.geometry.Pos
import javafx.scene.layout.StackPane
import nurishapp.util.SessionManager

import java.net.URI
import scala.util.Try

class FoodAidProgramController {

  // FXML
  @FXML private var titleLabel: Label = _
  @FXML private var programByOrgLabel: Label = _
  @FXML private var imageContainer: StackPane = _
  @FXML private var posterImage: ImageView = _
  @FXML private var dateValue: Label = _
  @FXML private var timeValue: Label = _
  @FXML private var venueValue: Label = _
  @FXML private var websiteLink: Hyperlink = _
  @FXML private var signUpBtn: Button = _

  // data carrier for food aid program
  case class ProgramData(
                          programByOrg: String,
                          date: String,
                          time: String,
                          venue: String,
                          website: String,
                          posterResource: String
                        )

  private var currentProgram: Option[ProgramData] = None
  private var stage: Stage = _
  private var rootController: RootLayoutController = _

  def setRootController(root: RootLayoutController): Unit = this.rootController = root

  def initStage(st: Stage): Unit = {
    this.stage = st
    stage.setTitle("NuRish - Food Aid Program")
  }

  @FXML private def initialize(): Unit = {
    // place to change details of program if changes are needed
    val demo = ProgramData(
      programByOrg = "The Food Distribution Program by HOPE Worldwide Malaysia",
      date = "Every 3rd Saturday of the month",
      time = "11:00 AM – 4:00 PM",
      venue = "Sentul Free Clinic of HOPE",
      website = "https://amazingseniors.my/event/87",
      posterResource = "/images/food_aid_poster.png"
    )
    setProgram(demo)
    posterImage.fitWidthProperty().bind(imageContainer.widthProperty())
    posterImage.fitHeightProperty().bind(imageContainer.heightProperty())
  }

  def setProgram(p: ProgramData): Unit = {
    currentProgram = Some(p)
    programByOrgLabel.setText(p.programByOrg)
    dateValue.setText(p.date)
    timeValue.setText(p.time)
    venueValue.setText(p.venue)
    websiteLink.setText(p.website)

    // load poster from resources
    val imgOpt =
      Option(getClass.getResourceAsStream(p.posterResource)).map(is => new Image(is))
        .orElse(Try(new Image(p.posterResource, true)).toOption) // fallback if absolute URL
    imgOpt.foreach(posterImage.setImage)
  }

  @FXML
  private def openPosterPreview(): Unit = {
    val img = posterImage.getImage
    if (img == null) return

    val stage = new Stage()
    stage.initModality(Modality.APPLICATION_MODAL)
    stage.setTitle("Poster Preview")
    stage.getIcons.add(new Image(getClass.getResourceAsStream("/images/logo.png")))

    // Poster image
    val iv = new ImageView(img)
    iv.setPreserveRatio(true)
    iv.setFitWidth(900) // initial zoom

    // Background image
    val bgImg = new Image(getClass.getResourceAsStream("/images/poster_preview_bg.png"))
    val bgView = new ImageView(bgImg)
    bgView.setFitWidth(980)
    bgView.setFitHeight(720)
    bgView.setPreserveRatio(false) // fill stage

    // Container: background + poster
    val stack = new StackPane()
    stack.getChildren.addAll(bgView, iv)
    stack.setAlignment(Pos.CENTER)

    val scroll = new javafx.scene.control.ScrollPane(stack)
    scroll.setPannable(true)

    // wheel zoom for poster
    scroll.setOnScroll(e => {
      val delta = if (e.getDeltaY > 0) 1.1 else 0.9
      iv.setFitWidth(Math.max(300, iv.getFitWidth * delta))
    })

    val scene = new Scene(scroll, 980, 720)
    stage.setScene(scene)
    stack.prefWidthProperty().bind(stage.getScene.widthProperty())
    stack.prefHeightProperty().bind(stage.getScene.heightProperty())
    bgView.fitWidthProperty().bind(scene.widthProperty())
    bgView.fitHeightProperty().bind(scene.heightProperty())
    stage.showAndWait()
  }

  @FXML
  private def openWebsite(): Unit = {
    val url = websiteLink.getText
    Try(java.awt.Desktop.getDesktop.browse(new URI(url)))
  }

  @FXML
  private def openSignUpDialog(): Unit = {
    if (SessionManager.currentUserId.isEmpty) {
      val a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION)
      a.setTitle("Log in required")
      a.setHeaderText(null)
      a.setContentText("Please log in to sign up for this program.")
      a.showAndWait();
      return
    }

    val loader = new FXMLLoader(getClass.getResource("/nurishapp.view/FoodAidSignUpDialog.fxml"))
    val root: Parent = loader.load()
    val ctrl = loader.getController.asInstanceOf[FoodAidSignUpDialogController]

    val st = new Stage()
    st.initModality(Modality.WINDOW_MODAL)
    st.setTitle("Food Aid Program Sign Up")
    st.getIcons.add(new Image(getClass.getResourceAsStream("/images/logo.png")))
    st.setScene(new Scene(root))
    ctrl.initStage(st) // so the controller can close the dialog
    st.showAndWait()
  }
}