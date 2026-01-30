package models.database

// Case class outlining Trip data point
case class Trip(rating: Double, owner: String, name: String, description: String, tripID: String, latitude: Double, longitude: Double, radius: Double)
