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

  def initStage(stage: Stage): Unit = {
    this.stage = stage
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
      // Load HomePage FXML
      val homeLoader = new FXMLLoader(getClass.getResource("/nurishapp.view/HomePage.fxml"))
      val homePage = homeLoader.load[Parent]()
      val homePageController = homeLoader.getController[HomePageController]

      // Get the current scene's root (which should be the BorderPane from RootLayout)
      val scene = backButton.getScene
      val rootLayout = scene.getRoot.asInstanceOf[BorderPane]

      // Set HomePage as the center of RootLayout
      rootLayout.setCenter(homePage)

      // Initialize the home page controller
      if (homePageController != null) {
        homePageController.initStage(stage)
      }

    } catch {
      case e: Exception =>
        println(s"Error navigating back to home page: ${e.getMessage}")
        e.printStackTrace()
    }
  }
}
