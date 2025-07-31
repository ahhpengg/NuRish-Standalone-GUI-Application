package nurishapp.view

import javafx.fxml.FXML
import javafx.scene.control.{Button, Hyperlink, Label, PasswordField, TextField}
import javafx.scene.image.ImageView
import javafx.scene.image.Image
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.layout.AnchorPane
import nurishapp.util.AuthenticationUtil
import scala.util.{Success, Failure}

class LoginController {
  @FXML private var loginPane: AnchorPane = _
  @FXML private var usernameField: TextField = _
  @FXML private var passwordField: PasswordField = _
  @FXML private var messageLabel: Label = _
  @FXML private var loginButton: Button = _
  @FXML private var signupLink: Hyperlink = _


  @FXML
  def initialize(): Unit = {
    // Set up login button action
    loginButton.setOnAction(_ => handleLogin())

    // Initial setup of the message label (hidden until needed)
    messageLabel.setText("")
  }

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
        passwordField.setText("") // Clear password field for security

      case Failure(exception) =>
        messageLabel.setText("Login error: " + exception.getMessage)
        println(s"Login error: ${exception.getMessage}") // For debugging
    }
  }


  @FXML
  private def handleSignUp(): Unit = {
    val root = FXMLLoader.load[Parent](getClass.getResource("/fxml/Signup.fxml"))
    val stage = loginPane.getScene.getWindow.asInstanceOf[Stage]
    stage.setScene(new Scene(root))
  }
}

