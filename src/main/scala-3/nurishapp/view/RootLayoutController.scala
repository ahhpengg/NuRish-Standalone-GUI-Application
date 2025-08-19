package nurishapp.view

import javafx.event.ActionEvent
import javafx.fxml.{FXML, FXMLLoader}
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.{Alert, ButtonType}
import javafx.scene.{Parent, Scene}
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import nurishapp.MainApp
import nurishapp.util.SessionManager
import nurishapp.model.User

@FXML
class RootLayoutController():
  @FXML private var rootLayout: BorderPane = _

  private var stage: Stage = _
  
  @FXML
  private def handleClose(): Unit = {
    // Show confirmation dialog
    val alert = new Alert(AlertType.CONFIRMATION)
    alert.setTitle("Exit Application")
    alert.setHeaderText("Confirm Exit")
    alert.setContentText("Are you sure you want to exit the application?")

    val result = alert.showAndWait()

    if (result.isPresent && result.get() == ButtonType.OK) {
      try {
        // Cleanup any resources if needed
        SessionManager.logout()

        // Close the application
        if (stage != null) {
          MainApp.stage.close()
        }
      } catch {
        case e: Exception =>
          e.printStackTrace()
          // Force close even if there's an error
          if (stage != null) {
            MainApp.stage.close()
          }
      }
    }
  }

  @FXML
  private def handleBack(): Unit = {
    // Always navigate back to home page
    try {
      loadHomePage()
    } catch {
      case e: Exception =>
        showAlert(AlertType.ERROR, "Navigation Error", s"Error navigating to home page: ${e.getMessage}")
        e.printStackTrace()
    }
  }

  @FXML
  private def handleProfile(): Unit = {
    SessionManager.currentUser match {
      case Some(user) =>
        try {
          // Load user details page as center content
          val loader = new FXMLLoader(getClass.getResource("/nurishapp.view/Profile.fxml"))
          val profilePage = loader.load[Parent]()
          val profileController = loader.getController[ProfileController]

          // Set user details as the center content
          rootLayout.setCenter(profilePage)

          // Initialize the controller if it exists
          if (profileController != null) {
            profileController.initStage(stage)
            profileController.setUser(user) // Pass the current user
          }
        } catch {
          case e: Exception =>
            showAlert(AlertType.ERROR, "Load Error", s"Error loading user details: ${e.getMessage}")
            e.printStackTrace()
        }
      case None =>
        showAlert(AlertType.WARNING, "Not Logged In", "Please log in to view account details")
        loadLoginPage()
    }
  }

  @FXML
  private def handleLogOut(): Unit = {
    // Show confirmation dialog
    val alert = new Alert(AlertType.CONFIRMATION)
    alert.setTitle("Log Out")
    alert.setHeaderText("Confirm Log Out")
    alert.setContentText("Are you sure you want to log out?")

    val result = alert.showAndWait()

    if (result.isPresent && result.get() == ButtonType.OK) {
      try {
        // Clear session
        SessionManager.logout()

        // Navigate back to login page
        loadLoginPage()

      } catch {
        case e: Exception =>
          showAlert(AlertType.ERROR, "Logout Error", s"Error during logout: ${e.getMessage}")
          e.printStackTrace()
      }
    }
  }

  @FXML
  private def handleAbout(): Unit = {
    try {
      // Load about page as center content
      val loader = new FXMLLoader(getClass.getResource("/nurishapp.view/About.fxml"))
      val aboutPage = loader.load[Parent]()
      val aboutController = loader.getController[AboutController]

      // Set about page as the center content
      rootLayout.setCenter(aboutPage)

      // Initialize the controller if it exists
      if (aboutController != null) {
        aboutController.initStage(stage)
      }
    } catch {
      case e: Exception =>
        showAlert(AlertType.ERROR, "Load Error", s"Error loading about page: ${e.getMessage}")
        e.printStackTrace()
    }
  }

  // Helper methods
  private def loadHomePage(): Unit = {
    try {
      val homeLoader = new FXMLLoader(getClass.getResource("/nurishapp.view/HomePage.fxml"))
      val homePage = homeLoader.load[Parent]()
      val homePageController = homeLoader.getController[HomePageController]

      rootLayout.setCenter(homePage)

      if (homePageController != null) {
        homePageController.initStage(stage)
      }
    } catch {
      case e: Exception =>
        showAlert(AlertType.ERROR, "Load Error", s"Error loading home page: ${e.getMessage}")
        e.printStackTrace()
    }
  }

  private def loadLoginPage(): Unit = {
    try {
      val loginLoader = new FXMLLoader(getClass.getResource("/nurishapp.view/Login.fxml"))
      val loginPage = loginLoader.load[Parent]()
      val loginController = loginLoader.getController[LoginController]

      // Replace entire scene with login page (no menu bar for login)
      val scene = new Scene(loginPage)
      stage.setScene(scene)

      if (loginController != null) {
        loginController.initStage(stage)
      }
    } catch {
      case e: Exception =>
        showAlert(AlertType.ERROR, "Load Error", s"Error loading login page: ${e.getMessage}")
        e.printStackTrace()
    }
  }

  private def showAlert(alertType: AlertType, title: String, message: String): Unit = {
    val alert = new Alert(alertType)
    alert.setTitle(title)
    alert.setHeaderText(null)
    alert.setContentText(message)
    alert.showAndWait()
  }



