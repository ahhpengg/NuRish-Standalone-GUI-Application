package nurishapp.model

import scalafx.beans.property.{ObjectProperty, StringProperty}
import java.time.LocalDate
import scalikejdbc.*
import scala.util.Try
import nurishapp.util.Database

case class FoodAidApplication(
                               id: Option[Int],
                               fullNameIcS: String,
                               icNumberS: String,
                               genderS: String,
                               dobD: LocalDate,
                               contactS: String,
                               signUpAtD: LocalDate,
                               byUserId: Option[Int]
                             ) extends Database {

  // ScalaFX properties 
  val fullNameIc = new StringProperty(this, "fullNameIc", fullNameIcS)
  val icNumber   = new StringProperty(this, "icNumber",   icNumberS)
  val gender     = new StringProperty(this, "gender",     genderS)
  val dob        = new ObjectProperty[LocalDate](this, "dob", dobD)
  val contact    = new StringProperty(this, "contact",    contactS)
  val signUpAt   = new ObjectProperty[LocalDate](this, "signUpAt", signUpAtD)

  def getCurrentValues: FoodAidApplication = copy(
    id = id,
    fullNameIcS = fullNameIc.value,
    icNumberS   = icNumber.value,
    genderS     = gender.value,
    dobD        = dob.value,
    contactS    = contact.value,
    signUpAtD   = signUpAt.value,
    byUserId    = byUserId
  )

  /** Insert new or update existing */
  def save()(implicit session: DBSession = AutoSession): Try[FoodAidApplication] = {
    if (id.isEmpty) {
      Try {
        sql"""
          INSERT INTO food_aid_applications
            (full_name_ic, ic_number, gender, dob, contact, sign_up_at, by_user_id)
          VALUES
            (${fullNameIc.value}, ${icNumber.value}, ${gender.value}, ${dob.value},
             ${contact.value}, ${signUpAt.value}, ${byUserId})
        """.update.apply()
        
        FoodAidApplication.findLatestByUser(byUserId).getOrElse(
          throw new RuntimeException("Failed to create application")
        )
      }
    } else {
      Try {
        sql"""
          UPDATE food_aid_applications
          SET full_name_ic = ${fullNameIc.value},
              ic_number    = ${icNumber.value},
              gender       = ${gender.value},
              dob          = ${dob.value},
              contact      = ${contact.value},
              sign_up_at   = ${signUpAt.value},
              by_user_id   = ${byUserId}
          WHERE id = ${id.get}
        """.update.apply()

        FoodAidApplication.findById(id.get).getOrElse(
          throw new RuntimeException("Failed to update application")
        )
      }
    }
  }

  def delete()(implicit session: DBSession = AutoSession): Try[Boolean] =
    id match {
      case Some(appId) => Try(sql"DELETE FROM food_aid_applications WHERE id = $appId".update.apply() > 0)
      case None        => Try(false)
    }
}

object FoodAidApplication extends Database {

  /** Call once at startup (ignore exception if table already exists) */
  def initializeTable(): Unit = {
    DB.autoCommit { implicit session =>
      sql"""
        CREATE TABLE food_aid_applications (
          id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,
          full_name_ic VARCHAR(120) NOT NULL,
          ic_number    VARCHAR(20)  NOT NULL,
          gender       VARCHAR(16)  NOT NULL,
          dob          DATE         NOT NULL,
          contact      VARCHAR(40)  NOT NULL,
          sign_up_at   DATE         NOT NULL,
          by_user_id   INTEGER
        )
      """.execute.apply()
    }
  }

  def findById(id: Int)(implicit session: DBSession = AutoSession): Option[FoodAidApplication] =
    sql"SELECT * FROM food_aid_applications WHERE id = $id"
      .map(rs => fromRS(rs)).single.apply()

  def findLatestByUser(userId: Option[Int])(implicit session: DBSession = AutoSession): Option[FoodAidApplication] =
    userId match {
      case Some(uid) =>
        sql"""
          SELECT * FROM food_aid_applications
          WHERE by_user_id = $uid
          ORDER BY id DESC
          FETCH FIRST 1 ROWS ONLY
        """.map(rs => fromRS(rs)).single.apply()
      case None => None
    }

  private def fromRS(rs: WrappedResultSet): FoodAidApplication =
    FoodAidApplication(
      id           = Some(rs.int("id")),
      fullNameIcS  = rs.string("full_name_ic"),
      icNumberS    = rs.string("ic_number"),
      genderS      = rs.string("gender"),
      dobD         = rs.date("dob").toLocalDate,
      contactS     = rs.string("contact"),
      signUpAtD    = rs.date("sign_up_at").toLocalDate,
      byUserId     = rs.intOpt("by_user_id")
    )
}