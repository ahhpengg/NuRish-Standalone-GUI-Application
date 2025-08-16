package nurishapp.view

import javafx.fxml.FXML
import javafx.scene.control.{Button, Hyperlink, Label, PasswordField, SplitPane, TextField}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.layout.AnchorPane
import nurishapp.util.AuthenticationUtil
import nurishapp.model.User
import scala.util.{Failure, Success, Try}
import scalafx.scene.control.Alert

class SignupController {
  @FXML private var signupPane: SplitPane = _
  @FXML private var usernameField: TextField = _
  @FXML private var emailField: TextField = _
  @FXML private var passwordField: PasswordField = _
  @FXML private var confirmPasswordField: PasswordField = _
  @FXML private var messageLabel: Label = _
  @FXML private var signupButton: Button = _
  @FXML private var loginLink: Hyperlink = _
  @FXML private var signupImage: ImageView = _
  @FXML private var imageContainer: AnchorPane = _

  private var stage: Stage = _

  def initStage(stage: Stage): Unit = {
    this.stage = stage

    imageContainer.prefWidthProperty().bind(stage.getScene.widthProperty())
    imageContainer.prefHeightProperty().bind(stage.getScene.heightProperty())
  }

  @FXML
  private def initialize(): Unit = {

    val imageUrl = getClass.getResource("/images/nurish_signup.png")
    signupImage.setImage(new Image(imageUrl.toString))
    signupImage.fitWidthProperty().bind(imageContainer.widthProperty())
    signupImage.fitHeightProperty().bind(imageContainer.heightProperty())

    // Add listeners for real-time validation
    usernameField.textProperty().addListener((_, _, newValue) => validateUsername(newValue))
    emailField.textProperty().addListener((_, _, newValue) => validateEmail(newValue))
    passwordField.textProperty().addListener((_, _, newValue) => validatePassword(newValue))
  }

  private def validateUsername(username: String): Boolean = {
    if (username.isEmpty) {
      messageLabel.setText("Username cannot be empty")
      false
    } else if (!username.matches("^[a-zA-Z0-9_]{3,20}$")) {
      messageLabel.setText("Username must be 3-20 characters long and contain only letters, numbers, and underscores")
      false
    } else {
      messageLabel.setText("")
      true
    }
  }

  private def validateEmail(email: String): Boolean = {
    if (email.isEmpty) {
      messageLabel.setText("Email cannot be empty")
      false
    } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
      messageLabel.setText("Please enter a valid email address")
      false
    } else {
      messageLabel.setText("")
      true
    }
  }

  private def validatePassword(password: String): Boolean = {
    if (password.isEmpty) {
      messageLabel.setText("Password cannot be empty")
      false
    } else if (password.length < 8) {
      messageLabel.setText("Password must be at least 8 characters long")
      false
    } else {
      // Check all conditions at once
      val missingRequirements = new StringBuilder()

      if (!password.matches(".*[A-Z].*"))
        missingRequirements.append("uppercase letter")

      if (!password.matches(".*[a-z].*")) {
        if (missingRequirements.nonEmpty) missingRequirements.append(", ")
        missingRequirements.append("lowercase letter")
      }

      if (!password.matches(".*[0-9].*")) {
        if (missingRequirements.nonEmpty) missingRequirements.append(", ")
        missingRequirements.append("number")
      }

      if (missingRequirements.nonEmpty) {
        messageLabel.setText(s"Password must contain at least one ${missingRequirements.toString}")
        false
      } else {
        messageLabel.setText("")
        true
      }
    }
  }


  @FXML
  private def handleSignUp(): Unit = {
    val username = usernameField.getText.trim
    val email = emailField.getText.trim
    val password = passwordField.getText
    val confirmPassword = confirmPasswordField.getText

    var errorMessage = StringBuilder()

    // Validate username and add specific error message
    if (!validateUsername(username)) {
      errorMessage.append("• " + messageLabel.getText + "\n")  // Add bullet point
    }

    // Validate email and add specific error message
    if (!validateEmail(email)) {
      errorMessage.append("• " + messageLabel.getText + "\n")
    }

    // Validate password and add specific error message
    if (!validatePassword(password)) {
      errorMessage.append("• " + messageLabel.getText + "\n")
    }

    // Check password confirmation
    if (password != confirmPassword) {
      errorMessage.append("• Passwords do not match\n")
    }

    // Get the current window safely
    val currentWindow = Option(stage)
      .orElse(Option(signupPane).map(_.getScene.getWindow))
      .getOrElse(throw new RuntimeException("Unable to get current window"))

    // Show validation errors if any
    if (errorMessage.nonEmpty) {
      val alert = new Alert(Alert.AlertType.Error):
        initOwner(currentWindow)
        title = "Sign Up Error"
        headerText = "Please correct the following errors:"
        contentText = errorMessage.toString
        resizable = true

      val dialogPane = alert.getDialogPane
      dialogPane.setPrefWidth(550)
      dialogPane.setPrefHeight(300)

      alert.showAndWait()
      return
    }

    // Attempt to register the user
    Try {
      AuthenticationUtil.registerUser(username, password, email) match {
        case Success(user) =>
          // Show success alert
          val alert = new Alert(Alert.AlertType.Information):
            initOwner(currentWindow)
            title = "Registration Successful"
            headerText = "Account Created Successfully"
            contentText = "Your account has been created. You will now be redirected to the login page."
          alert.showAndWait()
          handleLogIn() // Redirect to login page after clicking OK

        case Failure(exception) =>
          val alert = new Alert(Alert.AlertType.Error):
            initOwner(currentWindow)
            title = "Registration Error"
            headerText = "Registration Failed"
            contentText = s"Error: ${exception.getMessage}"
          alert.showAndWait()
      }
    } recover {
      case e: Exception =>
        val alert = new Alert(Alert.AlertType.Error):
          initOwner(currentWindow)
          title = "System Error"
          headerText = "Registration Failed"
          contentText = "An unexpected error occurred during registration"
        alert.showAndWait()
        e.printStackTrace() // For debugging
    }
  }


  @FXML
  private def handleLogIn(): Unit = {
    try {
      // Load the login FXML
      val loader = new FXMLLoader(getClass.getResource("/nurishapp.view/Login.fxml"))
      val root = loader.load[Parent]()

      // Get the current stage from any available scene component
      val currentStage = Option(signupPane)
        .map(_.getScene.getWindow.asInstanceOf[Stage])
        .orElse(Option(usernameField).map(_.getScene.getWindow.asInstanceOf[Stage]))
        .orElse(Option(stage))
        .getOrElse(throw new RuntimeException("Unable to get current stage"))

      // Set the new scene
      currentStage.setScene(new Scene(root))
    } catch {
      case e: Exception =>
        e.printStackTrace()
        val alert = new Alert(Alert.AlertType.Error) {
          title = "Navigation Error"
          headerText = "Error Loading Login Page"
          contentText = "Unable to load the login page. Please try again."
        }
        alert.showAndWait()
    }
  }


}
