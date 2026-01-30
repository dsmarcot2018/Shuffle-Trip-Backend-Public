package models.database

// Case class outlining User data point
case class User(userID: String, email: String, name: String, interests: List[String], preferences: List[String])
