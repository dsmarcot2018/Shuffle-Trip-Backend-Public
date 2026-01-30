package models.input

//case class SaveTrip(rating: Float, owner: String, name: String, description: String, tripID: String, latitude: Double, longitude: Double, radius: Double, activities: List[String], ratings: List[Float])
case class SaveTrip(tripID: String, owner: String, longitude: Double, rating: Float, radius: Double, ratings: List[Float], latitude: Double, description: String, name: String, activities: List[String])
