package nurishapp.view

import javafx.fxml.FXML
import javafx.scene.control.{Button, Label, PasswordField}
import javafx.stage.Stage
import nurishapp.util.{SessionManager, ValidationUtil}
import nurishapp.model.User
import scalafx.scene.control.Alert

import java.security.MessageDigest
import java.util.Base64

class ChangePasswordDialogController {

  @FXML private var oldField: PasswordField = _
  @FXML private var newField: PasswordField = _
  @FXML private var confirmField: PasswordField = _
  @FXML private var messageLabel: Label = _
  @FXML private var confirmBtn: Button = _

  private var stage: Stage = _

  def initStage(st: Stage): Unit = this.stage = st

  @FXML private def initialize(): Unit = {
    oldField.textProperty().addListener((_, _, newV) => messageLabel.setText(""))
    newField.textProperty().addListener((_, _, newV) => messageLabel.setText(ValidationUtil.validatePassword(newV).getOrElse("")))
    confirmField.textProperty().addListener((_, _, newV) => messageLabel.setText(""))
    confirmBtn.setOnAction(_ => handleChange())
  }

  private def hashPassword(p: String): String = {
    val md = MessageDigest.getInstance("SHA-256")
    val hash = md.digest(p.getBytes("UTF-8"))
    Base64.getEncoder.encodeToString(hash)
  }

  private def handleChange(): Unit = {
    val oldP = Option(oldField.getText).getOrElse("")
    val newP = Option(newField.getText).getOrElse("")
    val conf = Option(confirmField.getText).getOrElse("")

    val errors = collection.mutable.ListBuffer[String]()
    if (oldP.isEmpty) errors += "Old password is required"
    ValidationUtil.validatePassword(newP).foreach(errors += _)
    if (newP != conf) errors += "New password must match with confirmation"

    val currentOpt = SessionManager.currentUser
    if (currentOpt.isEmpty) {
      errors += "No user logged in"
    }

    // verify old password
    currentOpt.foreach { u =>
      val hashedOld = hashPassword(oldP)
      if (u.password.value != hashedOld) errors += "Old password is incorrect"
    }

    if (errors.nonEmpty) {
      val alert = new Alert(Alert.AlertType.Error)
      alert.setTitle("Password Changing Errors")
      alert.setHeaderText("Please fix the following")
      alert.setContentText(errors.mkString("\n"))
      alert.showAndWait()
      return
    }

    // persist new password
    val success = currentOpt.exists { u =>
      val newHashed = hashPassword(newP)
      val updated = u.getCurrentValues.copy(passwordS = newHashed)
      updated.save().map { nu =>
        SessionManager.login(nu)
        true
      }.recover { case _ => false }.getOrElse(false)
    }

    if (success) {
      val info = new Alert(Alert.AlertType.Information)
      info.setTitle("Password Changes Saved")
      info.setHeaderText(null)
      info.setContentText("Your password has updated successfully!")
      info.showAndWait()
      if (stage != null) stage.close()
    } else {
      val err = new Alert(Alert.AlertType.Error)
      err.setTitle("Error")
      err.setHeaderText(null)
      err.setContentText("Failed to change password.")
      err.showAndWait()
    }
  }
}
