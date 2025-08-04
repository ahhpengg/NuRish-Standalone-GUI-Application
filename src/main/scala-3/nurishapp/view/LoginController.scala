package nurishapp.view

import javafx.fxml.FXML
import javafx.scene.control.{Button, Hyperlink, Label, PasswordField, SplitPane, TextField}
import javafx.scene.image.ImageView
import javafx.scene.image.Image
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.layout.AnchorPane
import nurishapp.util.AuthenticationUtil
import scala.util.{Failure, Success}

class LoginController {
  @FXML private var loginPane: SplitPane = _
  @FXML private var usernameField: TextField = _
  @FXML private var passwordField: PasswordField = _
  @FXML private var messageLabel: Label = _
  @FXML private var loginButton: Button = _
  @FXML private var signupLink: Hyperlink = _
  @FXML private var loginImage: ImageView = _
  @FXML private var imageContainer: AnchorPane = _

  private var stage: Stage = _

  // handles application-specific initialization setup
  def initStage(stage: Stage): Unit = {
    this.stage = stage

    // Create a scene if not already set
    if (stage.getScene == null) {
      val scene = new Scene(loginPane)
      stage.setScene(scene)
    }
    
    // Make the scene resize with the window
    imageContainer.prefWidthProperty().bind(loginPane.widthProperty())
    imageContainer.prefHeightProperty().bind(loginPane.heightProperty())
  }

  // handles FXML component setup
  @FXML
  private def initialize(): Unit = {
    
    // Load the image
    val imageUrl = getClass.getResource("/images/nurish_login.png")
    loginImage.setImage(new Image(imageUrl.toString))
    loginImage.fitWidthProperty().bind(imageContainer.widthProperty())
    loginImage.fitHeightProperty().bind(imageContainer.heightProperty())
  }

  @FXML
  private def handleLogin(): Unit = {
    val username = usernameField.getText
    val password = passwordField.getText

    if (username.isEmpty || password.isEmpty) {
      messageLabel.setText("Please enter both username and password")
      return
    }

    AuthenticationUtil.login(username, password) match {
      case Success(true) =>
        messageLabel.setText("Login successful!")
      // Add your navigation logic here

      case Success(false) =>
        messageLabel.setText("Invalid Username/Password!")
        passwordField.setText("") // Clear the password field for security

      case Failure(exception) =>
        messageLabel.setText("Login error: " + exception.getMessage)
        println(s"Login error: ${exception.getMessage}") // For debugging
    }
  }


  @FXML
  private def handleSignUp(): Unit = {
    try {
      val loader = new FXMLLoader(getClass.getResource("/nurishapp.view/Signup.fxml"))
      val root = loader.load[Parent]()
      val signupController = loader.getController[SignupController]
      val stage = loginPane.getScene.getWindow.asInstanceOf[Stage]
      val scene = new Scene(root)
      stage.setScene(scene)
      signupController.initStage(stage)  // Initialize the stage after setting the scene
    } catch {
      case e: Exception =>
        messageLabel.setText("Error loading signup page")
        e.printStackTrace() // For debugging
    }
  }


}