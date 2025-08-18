package nurishapp

import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import javafx.fxml.FXMLLoader
import javafx.scene.control.SplitPane
import nurishapp.util.Database
import scalafx.Includes.*
import nurishapp.view.LoginController
import scalafx.scene.image.Image

object MainApp extends JFXApp3 {
  Database.setupDB()
  override def start(): Unit = {
    val loader = new FXMLLoader(getClass.getResource("/nurishapp.view/Login.fxml"))
    val root = loader.load[SplitPane]()
    val controller = loader.getController[LoginController]

    stage = new PrimaryStage {
      title = "NuRish"
      icons += new Image(getClass.getResource("/images/logo.png").toExternalForm)
      scene = new Scene(root)
    }

    // Initialize the controller with the stage
    controller.initStage(stage)

    stage.show()
  }
}

