package models.handlers

import models.input.{FilterInputValues, InputValues, NameAndAddress}

import javax.inject.Inject
import play.api.Configuration
import com.typesafe.config.ConfigFactory
import models.database.{Activity, Trip}
import okhttp3.{OkHttpClient, Request, Response}
import play.api.libs.json._

import scala.util.Random


class ApiHandler @Inject() (config: Configuration) {
  implicit val myDataHandler: DatabaseHandler = new DatabaseHandler(new Configuration(ConfigFactory.load("private-variables.conf")))

  /*
   * Builds the url for requests made to the Yelp API
   *
   * @param input: InputValues = An InputValues case class containing the below variables
   *    latitude: Float = The latitude of the trip
   *    longitude: Float = The longitude of the trip
   *    radius: Int = The search range for the trip
   * @param term: String = The term to search for activities by
   * @param offset: Int = A random integer to give variance to trip selections
   *
   * @return String = A url to be used for making requests to the Yelp API
   */
  private def termUrlBuilder(input: InputValues, term: String, offset: Int): String = {
    val defaultUrl: String = "https://api.yelp.com/v3/businesses/search?"
    val url: String = defaultUrl + s"latitude=${input.latitude}" + s"&longitude=${input.longitude}" + "&term=" + term + s"&radius=${input.radius}" + s"&sort_by=best_match&limit=1&offset=$offset"
    url
  }

  /*
   * Builds the request to be made to the Yelp API
   *
   * @param input: InputValues = An InputValues case class containing the below variables
   *    latitude: Float = The latitude of the trip
   *    longitude: Float = The longitude of the trip
   *    radius: Int = The search range for the trip
   * @param term: String = The term to search for activities by
   * @param offset: Int = A random integer to give variance to trip selections
   *
   * @return Request = A Request object to be used in calls to the Yelp API
   */
  private def termRequestBuilder(input: InputValues, term: String, offset: Int): Request = {
    val request: Request = new Request.Builder()
      .url(termUrlBuilder(input, term, offset))
      .get()
      .addHeader("accept", "application/json")
      .addHeader("Authorization", config.get[String]("API_KEY"))
      .build()

    request
  }

  /*
   * Gets the response body from calls to the Yelp API
   *
   * @param input: InputValues = An InputValues case class containing the below variables
   *    terms: List[String] = **Unused in this function**
   *    latitude: Float = The latitude of the trip
   *    longitude: Float = The longitude of the trip
   *    radius: Int = The search range for the trip
   * @param term: String = The term to search for activities by
   * @param offset: Int = A random integer to give variance to trip selections
   *
   * @return String = The response body returned by calls to the Yelp API
   */
  private def getResponseBody(input: InputValues, term: String, offset: Int): String = {
    val client: OkHttpClient = new OkHttpClient()
    val request: Request = termRequestBuilder(input, term, offset)
    val response: Response = client.newCall(request).execute()
    response.body().string()
  }

  /*
   * Gets a random number used for more variance in activities
   *
   * @param max: Int = The maximum value for the random offset (cannot be greater than 1000 due to Yelp API limits)
   *
   * @return Int = A random offset used for randomizing activities
   */
  private def getOffset(max: Int = 1000): Int = {
    val rand: Random = new scala.util.Random
    (rand.nextInt() % max).abs
  }

  /*
   * Gets a new maximum for calculating offsets when original offset is too large
   *
   * @param responseBody: String = The response body from Yelp API calls
   *
   * @return Int = A new maximum to be used when calculating offsets
   */
  private def findNewMax(responseBody: String): Int = {
    var newMax: Int = 0
    if (!responseBody.charAt(29).isDigit)
      newMax = responseBody.charAt(28).asDigit
    else if (!responseBody.charAt(30).isDigit)
      newMax = responseBody.charAt(28).asDigit * 10 + responseBody.charAt(29).asDigit
    else
      newMax = responseBody.charAt(28).asDigit * 100 + responseBody.charAt(29).asDigit * 10 + responseBody.charAt(30).asDigit

    newMax
  }

  private def filterBusinessesFromRadius(input: InputValues, term: String, newMax: Int): JsValue = {
    var count = 0
    val offset = getOffset(newMax)
    while (count < 20) {
      val newResponseBody: String = getResponseBody(input, term, offset + count)
      val newJsonValue: JsValue = Json.parse(newResponseBody)
      if((newJsonValue \ "businesses").as[Seq[JsObject]].nonEmpty){
        if (((newJsonValue \ "businesses").as[Seq[JsObject]].head \ "distance").as[Float] < input.radius) {
          return newJsonValue
        }
      }

      count += 1
    }
    val errorResponseBody: String = "{\"error\": 404, \"description\": \"No results found in radius for " + term + "\"}"
    Json.parse(errorResponseBody)
  }

  /*
   * Gets a list of activities that will be displayed to the user
   *
   * @param input: InputValues = A case class containing the below variables
   *    terms: List[String] = A list of terms to request by
   *    latitude: Float = **Unused in this function**
   *    longitude: Float = **Unused in this function**
   *    radius: Int = **Unused in this function**
   *
   * @return List[JsValue] = A list of activities in json format that will be delivered to the frontend
   */
  def termRequestBusinesses(input: InputValues): JsValue = {
    for(term <- input.terms) {
      val responseBody: String = getResponseBody(input, term, getOffset())

      if ((responseBody indexOf "[]") != 15) {
        val jsonValue: JsValue = Json.parse(responseBody)
        if (((jsonValue \ "businesses").as[Seq[JsObject]].head \ "distance").as[Float] < input.radius) {
          return jsonValue
        }
        /*if (((jsonValue \ "businesses").as[Seq[JsObject]].head \ "is_closed").as[Boolean] == false) {
          return jsonValue
        }*/
        else {
          return filterBusinessesFromRadius(input, term, findNewMax(responseBody))
        }
      } else {
        val newMax = findNewMax(responseBody)

        if(newMax != 0){
          return filterBusinessesFromRadius(input, term, newMax)
        }
      }
    }
    /*var activityList: List[Activity] = List[Activity]()
    for(activity <- returnList){
      try {
        activityList = Activity(((activity \ "businesses").as[Seq[JsObject]].head \ "id").as[String], ((activity \ "businesses").as[Seq[JsObject]].head \ "rating").as[Double]) :: activityList
      } catch {
        case e: JsResultException => println(e)
      }
    }*/
    //val chars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')
    //myDataHandler.saveTrip(Trip(4.5, "Drew", randomStringFromCharList(12, chars), ""), activityList)
    val errorResponseBody: String = "{\"error\": 404, \"description\": \"No results found\"}"
    Json.parse(errorResponseBody)
  }

  /*
  private def randomStringFromCharList(length: Int, chars: Seq[Char]): String = {
    val r = new scala.util.Random
    val sb = new StringBuilder
    for (i <- 1 to length) {
      val randomNum = r.nextInt(chars.length)
      sb.append(chars(randomNum))
    }
    sb.toString
  }
  */


  /*
   * Gets a list of activities that will be displayed to the user based on a
   * random set from a large list of terms.
   *
   * @param input: FilterInputValues = A case class containing the below variables
   *    terms: List[String] = A list of terms to be filtered
   *    latitude: Float = **Unused in this function**
   *    longitude: Float = **Unused in this function**
   *    radius: Int = **Unused in this function**
   *    count: Int = The number of terms to request for
   *
   * @return List[JsValue] = A list of activities in json format that will be delivered to the frontend
   */
  def filterTerms(input: FilterInputValues): List[JsValue] = {
    var returnList: List[JsValue] = List[JsValue]()
    val friendPrefs: List[String] = myDataHandler.getNeighborPreferences(input.userID)

    for(terms <- input.terms){
      var newTerms = Random.shuffle(terms)
      if ((Random.nextInt() % 100) < input.chance) {
        val tempTerms = for (a <- friendPrefs if terms.contains(a)) yield a
        if(tempTerms.nonEmpty){
          newTerms = tempTerms
        }
      }
      returnList = termRequestBusinesses(InputValues(newTerms, input.latitude, input.longitude, input.radius)) :: returnList
    }

    returnList
  }

  private def businessRequestUrlBuilder(activityID: String): String = {
    "https://api.yelp.com/v3/businesses/" + s"${activityID}"
  }

  private def businessRequestBuilder(activityID: String): Request = {
    val request: Request = new Request.Builder()
      .url(businessRequestUrlBuilder(activityID))
      .get()
      .addHeader("accept", "application/json")
      .addHeader("Authorization", config.get[String]("API_KEY"))
      .build()

    request
  }

  private def getBusinessResponseBody(activityID: String): String = {
    val client: OkHttpClient = new OkHttpClient()
    val request: Request = businessRequestBuilder(activityID)
    val response: Response = client.newCall(request).execute()
    response.body().string()
  }

  def getBusiness(activityID: String): JsValue = {
    Json.parse(getBusinessResponseBody(activityID))
  }

  def getCategories(activityIDs: List[String]): List[String] = {
    var categoriesList: List[String] = List[String]()
    for(activityID <- activityIDs){
      val business: JsValue = getBusiness(activityID)
      for(category <- (business \ "categories").as[Seq[JsObject]]){
        categoriesList = (category \ "alias").as[String] :: categoriesList
      }
    }

    categoriesList
  }

  private def businessAddressRequestUrlBuilder(input: NameAndAddress): String = {
    "https://api.yelp.com/v3/businesses/matches?name=" + input.name + "&address1=" + input.address + "&city=" + input.city + "&state=" + input.state + "&country=" + input.country + "&limit=1&match_threshold=default"
  }

  private def businessAddressRequestBuilder(input: NameAndAddress): Request = {
    val request: Request = new Request.Builder()
      .url(businessAddressRequestUrlBuilder(input))
      .get()
      .addHeader("accept", "application/json")
      .addHeader("Authorization", config.get[String]("API_KEY"))
      .build()

    request
  }

  private def getBusinessAddressResponseBody(input: NameAndAddress): String = {
    val client: OkHttpClient = new OkHttpClient()
    val request: Request = businessAddressRequestBuilder(input)
    val response: Response = client.newCall(request).execute()
    response.body().string()
  }

  def getBusinessByAddress(input: NameAndAddress): JsValue = {
    Json.parse(getBusinessAddressResponseBody(input))
  }
}
