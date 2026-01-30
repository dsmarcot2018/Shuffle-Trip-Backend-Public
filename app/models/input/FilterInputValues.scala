package models.input

case class FilterInputValues(terms: List[List[String]], latitude: Float, longitude: Float, radius: Int, userID: String, chance: Int = 10)
