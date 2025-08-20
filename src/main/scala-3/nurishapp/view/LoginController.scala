package nurishapp.view

import javafx.fxml.FXML
import javafx.scene.control.{Button, Hyperlink, Label, PasswordField, SplitPane, TextField}
import javafx.scene.image.ImageView
import javafx.scene.image.Image
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.layout.{AnchorPane, BorderPane}
import nurishapp.model.User
import nurishapp.util.{AuthenticationUtil, SessionManager}

import scala.util.{Failure, Success, Try}

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
        // Get the user after successful login
        Try(User.findByUsername(username)) match {
          case Success(Some(user: User)) =>
            try {
              // Save user session
              SessionManager.login(user)

              // Load the RootLayout first
              val rootLoader = new FXMLLoader(getClass.getResource("/nurishapp.view/RootLayout.fxml"))
              val rootLayout = rootLoader.load[BorderPane]()
              val rootController = rootLoader.getController[RootLayoutController]

              // Load the HomePage FXML
              val homeLoader = new FXMLLoader(getClass.getResource("/nurishapp.view/HomePage.fxml"))
              val homePage = homeLoader.load[Parent]()
              val homePageController = homeLoader.getController[HomePageController]

              // Link HomePageController with RootLayoutController
              homePageController.setRootController(rootController)

              // Set HomePage as the center of RootLayout
              rootLayout.setCenter(homePage)

              // Ensure menu bar remains visible (force layout update)
              rootLayout.requestLayout()

              // Get the current stage
              val stage = loginPane.getScene.getWindow.asInstanceOf[Stage]

              // Create and set new scene
              val scene = new Scene(rootLayout)
              stage.setScene(scene)

              // IMPORTANT: pass the Stage into RootLayoutController
              rootController.initStage(stage)

              stage.setTitle("NuRish - Home")
              println("Successfully logged in as " + user.username.get + "!")

            } catch {
              case e: Exception =>
                messageLabel.setText("Error loading home page")
                e.printStackTrace() // For debugging
            }
          case Success(None) =>
            messageLabel.setText("User not found after login")
          case Failure(e) =>
            messageLabel.setText("Error retrieving user: " + e.getMessage)
        }

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