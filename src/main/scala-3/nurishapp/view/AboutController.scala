package nurishapp.view

import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.control.Button
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.{BorderPane, StackPane}
import javafx.scene.Parent
import javafx.stage.Stage

class AboutController {
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

    // Make the image fit to the StackPane
    // Get parent StackPane when it's available
    aboutBg.parentProperty().addListener((_, _, newParent) => {
      if (newParent != null && newParent.isInstanceOf[StackPane]) {
        val stackPane = newParent.asInstanceOf[StackPane]

        // Bind ImageView size to StackPane size
        aboutBg.fitWidthProperty().bind(stackPane.widthProperty())
        aboutBg.fitHeightProperty().bind(stackPane.heightProperty())

        // Set preserve ratio to false to fill completely
        aboutBg.setPreserveRatio(false)
      }
    })
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
