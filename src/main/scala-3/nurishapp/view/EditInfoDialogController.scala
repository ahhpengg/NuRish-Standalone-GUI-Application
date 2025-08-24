package nurishapp.view

import javafx.fxml.FXML
import javafx.scene.control.{Button, Label, TextField}
import javafx.stage.Stage
import nurishapp.util.{AuthenticationUtil, DateUtil, SessionManager, ValidationUtil}
import nurishapp.model.User
import scalafx.scene.control.Alert

class EditInfoDialogController {

  @FXML private var usernameField: TextField = _
  @FXML private var emailField: TextField = _
  @FXML private var messageLabel: Label = _
  @FXML private var confirmBtn: Button = _

  private var stage: Stage = _
  def initStage(st: Stage): Unit = this.stage = st

  @FXML private def initialize(): Unit = {
    // realtime feedback
    usernameField.textProperty().addListener((_, _, newV) =>
      messageLabel.setText(ValidationUtil.validateUsername(newV).getOrElse(""))
    )
    emailField.textProperty().addListener((_, _, newV) =>
      messageLabel.setText(ValidationUtil.validateEmail(newV).getOrElse(""))
    )

    confirmBtn.setOnAction(_ => handleSave())
  }

  def prefillFromSession(): Unit = {
    SessionManager.currentUser.foreach { u =>
      usernameField.setText(u.username.value)
      emailField.setText(u.email.value)
    }
  }

  private def handleSave(): Unit = {
    val username = Option(usernameField.getText).map(_.trim).getOrElse("")
    val email = Option(emailField.getText).map(_.trim).getOrElse("")
    val errors = collection.mutable.ListBuffer[String]()

    ValidationUtil.validateUsername(username).foreach(errors += _)
    ValidationUtil.validateEmail(email).foreach(errors += _)

    // uniqueness checks: allow unchanged values
    val current = SessionManager.currentUser
    current.foreach { u =>
      if (username != u.username.value && AuthenticationUtil.checkUsernameExists(username)) errors += "Username already exists"
      if (email != u.email.value && AuthenticationUtil.checkEmailExists(email)) errors += "Email already exists"
    }

    if (errors.nonEmpty) {
      val alert = new Alert(Alert.AlertType.Error)
      alert.setTitle("Info Changing Errors")
      alert.setHeaderText("Please Fix the Following Errors")
      alert.setContentText(errors.mkString("\n"))
      alert.showAndWait()
      return
    }

    // persist changes
    val saved = SessionManager.currentUser match {
      case Some(u) =>
        val updated = u.getCurrentValues.copy(usernameS = username, emailS = email)
        // save via User.save
        updated.save().map { newU =>
          SessionManager.login(newU)
          true
        }.recover { case _ => false }.getOrElse(false)
      case None => false
    }

    if (saved) {
      val info = new Alert(Alert.AlertType.Information)
      info.setTitle("Profile Changes Saved")
      info.setHeaderText(null)
      info.setContentText("Profile has updated successfully!")
      info.showAndWait()
      if (stage != null) stage.close()
    } else {
      val err = new Alert(Alert.AlertType.Error)
      err.setTitle("Error")
      err.setHeaderText(null)
      err.setContentText("Failed to save changes.")
      err.showAndWait()
    }
  }
}