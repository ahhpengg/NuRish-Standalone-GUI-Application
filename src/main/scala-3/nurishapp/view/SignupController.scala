package nurishapp.view

import javafx.fxml.FXML
import javafx.scene.control.{Button, Hyperlink, Label, PasswordField, SplitPane, TextField}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.layout.AnchorPane
import nurishapp.util.{AuthenticationUtil, ValidationUtil}
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

    // Realtime validation (only one field at a time shown on messageLabel)
    usernameField.textProperty().addListener((_, _, newValue) =>
      messageLabel.setText(ValidationUtil.validateUsername(newValue).getOrElse(""))
    )
    emailField.textProperty().addListener((_, _, newValue) =>
      messageLabel.setText(ValidationUtil.validateEmail(newValue).getOrElse(""))
    )
    passwordField.textProperty().addListener((_, _, newValue) =>
      messageLabel.setText(ValidationUtil.validatePassword(newValue).getOrElse(""))
    )
  }

  @FXML
  private def handleSignUp(): Unit = {
    val username = usernameField.getText.trim
    val email = emailField.getText.trim
    val password = passwordField.getText
    val confirmPassword = confirmPasswordField.getText

    val errorMessages = collection.mutable.ListBuffer[String]()

    ValidationUtil.validateUsername(username).foreach(err => errorMessages += err)
    ValidationUtil.validateEmail(email).foreach(err => errorMessages += err)
    ValidationUtil.validatePassword(password).foreach(err => errorMessages += err)

    if (password != confirmPassword) {
      errorMessages += "Passwords do not match"
    }

    val currentWindow = Option(stage)
      .orElse(Option(signupPane).map(_.getScene.getWindow))
      .getOrElse(throw new RuntimeException("Unable to get current window"))

    if (errorMessages.nonEmpty) {
      val alert = new Alert(Alert.AlertType.Error) {
        initOwner(currentWindow)
        title = "Sign Up Error"
        headerText = "Please correct the following errors:"
        contentText = errorMessages.map("• " + _).mkString("\n")
        resizable = true
      }

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
