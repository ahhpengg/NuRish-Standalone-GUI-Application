package nurishapp

import nurishapp.util.Database
import scalafx.application.JFXApp3

object MainApp extends JFXApp3{
  Database.initializeTables()


  override def start(): Unit = ???
}
