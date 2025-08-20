package nurishapp.view

import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.control.Button
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.{BorderPane, StackPane}
import javafx.scene.Parent
import javafx.stage.Stage

class AboutController {
  @FXML private var aboutPage: StackPane = _
  @FXML private var aboutBg: ImageView = _
  @FXML private var backButton: Button = _

  private var stage: Stage = _
  private var rootController: RootLayoutController = _

  def initStage(stage: Stage): Unit = {
    this.stage = stage

    stage.setTitle("NuRish - About Us")
  }

  // Inject RootLayoutController
  def setRootController(rootController: RootLayoutController): Unit = {
    this.rootController = rootController
  }

  @FXML
  private def initialize(): Unit = {
    // Load the background image
    val imageUrl = getClass.getResource("/images/about_us.png")
    if (imageUrl != null) {
      aboutBg.setImage(new Image(imageUrl.toString))
    }
    aboutBg.fitWidthProperty().bind(aboutPage.widthProperty())
    aboutBg.fitHeightProperty().bind(aboutPage.heightProperty())
    aboutBg.setPreserveRatio(false)
  }

  @FXML
  private def handleBack(): Unit = {
    try {
      if (rootController != null) {
        // Use RootLayoutController to switch back to HomePage
        rootController.setCenterPage("/nurishapp.view/HomePage.fxml")
      } else {
        println("RootLayoutController not set in AboutController")
      }
    } catch {
      case e: Exception =>
        println(s"Error navigating back to home page: ${e.getMessage}")
        e.printStackTrace()
    }
  }
}
