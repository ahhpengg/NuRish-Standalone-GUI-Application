package nurishapp.view

import nurishapp.MainApp
import javafx.event.ActionEvent
import javafx.fxml.FXML

@FXML
class RootLayoutController():

  @FXML
  def handleClose(action: ActionEvent): Unit =
    MainApp.stage.close()

