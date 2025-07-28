package nurishapp.model

case class User(
                 id: Option[Int],
                 username: String,
                 password: String,
                 email: String,
                 createdAt: java.time.LocalDateTime
               )

