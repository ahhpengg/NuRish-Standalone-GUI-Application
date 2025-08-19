package nurishapp.util

import nurishapp.model.User
import scala.util.{Try, Success, Failure}

object SessionManager {
  private var _currentUser: Option[User] = None

  /** Get the current logged-in user (read-only access) */
  def currentUser: Option[User] = _currentUser

  /** Log in a user (sets the session) */
  def login(user: User): Unit = {
    // store a "snapshot" of the latest values
    _currentUser = Some(user.getCurrentValues)
  }

  /** Log out the user */
  def logout(): Unit = {
    _currentUser = None
  }

  /** Refresh the current user from DB */
  def refresh(): Unit = {
    _currentUser = _currentUser.flatMap(u => u.id.flatMap(User.findById))
  }

  /** Save any changes made to current user */
  def saveChanges(): Try[User] = {
    _currentUser match {
      case Some(user) =>
        val updated = user.getCurrentValues.save()
        updated.foreach(u => _currentUser = Some(u))
        updated
      case None =>
        Failure(new RuntimeException("No user logged in"))
    }
  }
}
