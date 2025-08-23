package nurishapp.view

import javafx.fxml.FXML
import javafx.scene.control.{Alert, Button, ComboBox, TextField}
import javafx.stage.Stage
import javafx.scene.control.Alert.AlertType

import nurishapp.model.FoodAidApplication
import nurishapp.util.{SessionManager, DateUtil}

import java.time.LocalDate
import scala.util.Try

class FoodAidSignUpDialogController {

  // FXML
  @FXML private var fullNameField: TextField = _
  @FXML private var icNumberField: TextField = _
  @FXML private var genderBox: ComboBox[String] = _
  @FXML private var dobField: TextField = _
  @FXML private var contactField: TextField = _
  @FXML private var confirmBtn: Button = _
  @FXML private var cancelBtn: Button = _

  // dialog stage
  private var stage: Stage = _

  def initStage(st: Stage): Unit = this.stage = st

  @FXML private def initialize(): Unit = {
    genderBox.getItems.setAll("Male", "Female", "Other")
  }

  @FXML
  private def handleConfirm(): Unit = {
    // must be signed in
    val userIdOpt = SessionManager.currentUserId
    if (userIdOpt.isEmpty) {
      warn("Sign in required", "Please sign in before signing up.")
      return
    }

    val fullName = optTrim(fullNameField.getText)
    val ic = optTrim(icNumberField.getText)
    val gender = Option(genderBox.getValue).map(_.trim)
    val dobStr = optTrim(dobField.getText)
    val contact = optTrim(contactField.getText)

    // validations
    if (fullName.isEmpty) {
      warn("Invalid", "Full Name (as IC) is required."); return
    }
    if (ic.isEmpty || !ic.get.matches("""\d{12}""")) {
      warn("Invalid", "IC number must be exactly 12 digits (no dashes)."); return
    }
    if (gender.isEmpty) {
      warn("Invalid", "Please select Gender."); return
    }
    if (dobStr.isEmpty) {
      warn("Invalid", "Date of Birth is required."); return
    }

    val dob: LocalDate = parseDob(dobStr.get).getOrElse {
      warn("Invalid", "Date of Birth must be like YYYY-MM-DD.")
      return
    }

    if (contact.isEmpty || !isValidPhone(contact.get)) {
      warn("Invalid", "Contact number format looks wrong."); return
    }

    // build + save
    val app = FoodAidApplication(
      id = None,
      fullNameIcS = fullName.get,
      icNumberS = ic.get,
      genderS = gender.get,
      dobD = dob,
      contactS = contact.get,
      signUpAtD = LocalDate.now(), // or use DateUtil.today() if you have it
      byUserId = userIdOpt
    )

    app.save().fold(
      _ => warn("Error", "Failed to submit. Please try again."),
      _ => {
        info("Thank you!", "Thanks for sign up! We will contact you for further enquiries if you are qualified.")
        if (stage != null) stage.close()
      }
    )
  }

  @FXML
  private def handleCancel(): Unit = if (stage != null) stage.close()

  // === Helpers ===
  private def optTrim(s: String): Option[String] = Option(s).map(_.trim).filter(_.nonEmpty)

  private def parseDob(s: String): Option[LocalDate] = {
    DateUtil.parseLocalDate(s).orElse {
      Try(LocalDate.parse(s)).toOption
    }
  }

  private def isValidPhone(s: String): Boolean =
    s.matches("""\+?\d[ \d-]{6,}""")

  private def warn(t: String, m: String): Unit = {
    val a = new Alert(AlertType.WARNING);
    a.setTitle(t);
    a.setHeaderText(null);
    a.setContentText(m);
    a.showAndWait();
    ()
  }

  private def info(t: String, m: String): Unit = {
    val a = new Alert(AlertType.INFORMATION);
    a.setTitle(t);
    a.setHeaderText(null);
    a.setContentText(m);
    a.showAndWait();
    ()
  }
}