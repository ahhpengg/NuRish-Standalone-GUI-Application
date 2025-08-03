package nurishapp

import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import javafx.fxml.FXMLLoader
import javafx.scene.control.SplitPane
import scalafx.Includes.*
import nurishapp.view.LoginController

object MainApp extends JFXApp3 {
  override def start(): Unit = {
    val loader = new FXMLLoader(getClass.getResource("/nurishapp.view/Login.fxml"))
    val root = loader.load[SplitPane]()
    val controller = loader.getController[LoginController]

    stage = new PrimaryStage {
      title = "NuRish"
      scene = new Scene(root)
    }

    // Initialize the controller with the stage
    controller.initStage(stage)

    stage.show()
  }
}

