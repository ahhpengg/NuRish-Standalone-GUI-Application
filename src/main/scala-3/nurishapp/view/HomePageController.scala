package nurishapp.view

import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.layout.StackPane
import javafx.scene.media.{Media, MediaPlayer, MediaView}
import javafx.stage.Stage
import javafx.scene.image.{Image, ImageView}

import scala.util.{Failure, Success, Try}

class HomePageController {
  @FXML private var homePage: StackPane = _
  @FXML private var videoPane: StackPane = _
  @FXML private var logoImage: ImageView = _
  private var mediaPlayer: MediaPlayer = _
  private var mediaView: MediaView = _
  private var stage: Stage = _

  private var rootController: RootLayoutController = _

  def setRootController(controller: RootLayoutController): Unit = {
    this.rootController = controller
  }

  def initStage(stage: Stage): Unit = {
    this.stage = stage

    // Set up any stage-specific configurations here
    stage.setTitle("NuRish - Home")
  }

  @FXML
  def initialize(): Unit = {
    Try {
      val videoUrl = getClass.getResource("/videos/homepage_bg.mp4")
      if (videoUrl == null) throw new RuntimeException("Video file not found")

      val video = new Media(videoUrl.toExternalForm)
      mediaPlayer = new MediaPlayer(video)
      mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE)
      mediaPlayer.setMute(true)

      mediaView = new MediaView(mediaPlayer)

      // Bind to the parent container (which will be the center of BorderPane)
      mediaView.fitWidthProperty().bind(homePage.widthProperty())
      mediaView.fitHeightProperty().bind(homePage.heightProperty())
      mediaView.setPreserveRatio(false) // This ensures it fills completely without black bars

      videoPane.getChildren.add(mediaView)
      mediaPlayer.play()
    } match {
      case Success(_) => // Video started successfully
      case Failure(e) =>
        println(s"Error initializing video: ${e.getMessage}")
      // Add appropriate error handling here
    }

    // Load the image
    val imageUrl = getClass.getResource("/images/logo.png")
    if (imageUrl != null) {
      logoImage.setImage(new Image(imageUrl.toString))
    }
  }

  def cleanup(): Unit = {
    Option(mediaPlayer).foreach { player =>
      player.stop()
      player.dispose()
    }
  }

  private def showAlert(alertType: AlertType, title: String, message: String): Unit = {
    val alert = new Alert(alertType)
    alert.setTitle(title)
    alert.setHeaderText(null)
    alert.setContentText(message)
    alert.showAndWait()
  }

  @FXML
  def handleMealPlanner(): Unit = {
    if (rootController != null) {
      rootController.setCenterPage("/nurishapp.view/MealPlanner.fxml")
    } else {
      println("RootLayoutController not set in HomePageController")
    }
  }

  @FXML
  def handleFoodSearch(): Unit = {
    if (rootController != null) {
      rootController.setCenterPage("/nurishapp.view/FoodSearch.fxml")
    } else {
      println("RootLayoutController not set in HomePageController")
    }
  }
}
