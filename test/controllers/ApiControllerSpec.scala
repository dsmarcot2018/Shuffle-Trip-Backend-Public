package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.libs.json._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.Format.GenericFormat
import play.api.mvc.Result

import scala.concurrent.Future

class ApiControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {
  "ApiController POST" should {
    val apiController = new ApiController(stubControllerComponents())
    val databaseController = new DatabaseController(stubControllerComponents())

    "return yelp api data to the client in requestApiData()" in {
      val result: Future[Result] = apiController.requestApiData().apply(FakeRequest(POST, controllers.routes.ApiController.requestApiData().url)
        .withJsonBody(Json.obj("terms" -> List("lunch","park","dinner"), "latitude" -> 44.4749, "longitude" -> -73.2121, "radius" -> 40000)))

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsString(result) must include("categories")
    }

    "returns a 400 error when a bad request is made in requestApiData()" in {
      val result: Future[Result] = apiController.requestApiData().apply(FakeRequest(POST, controllers.routes.ApiController.requestApiData().url)
        .withJsonBody(Json.obj()))

      status(result) mustBe 400
    }

    "return yelp api data to the client in filteredRequestApiData()" in {
      databaseController.createUser().apply(FakeRequest(POST, controllers.routes.DatabaseController.createUser().url)
        .withJsonBody(Json.obj("userID" -> "Jimbob", "email" -> "hello@gmail.com", "name" -> "Jim bob", "interests" -> List[String](), "preferences" -> List[String]())))
      databaseController.createUser().apply(FakeRequest(POST, controllers.routes.DatabaseController.createUser().url)
        .withJsonBody(Json.obj("userID" -> "Jimbobfriend", "email" -> "hi@gmail.com", "name" -> "Jim bob friend", "interests" -> List[String](), "preferences" -> List[String]("parks"))))
      databaseController.makeFriend().apply(FakeRequest(POST, controllers.routes.DatabaseController.makeFriend().url)
        .withJsonBody(Json.obj("user1" -> "Jimbob", "user2" -> "Jimbobfriend")))

      val result: Future[Result] = apiController.filteredRequestApiData().apply(FakeRequest(POST, controllers.routes.ApiController.filteredRequestApiData().url)
        .withJsonBody(Json.obj("terms" -> List(List("lunch", "park"), List("dinner", "lunch"), List("museum")), "latitude" -> 40.7128, "longitude" -> -74.0060, "radius" -> 40000, "userID" -> "Jimbob", "chance" -> 0)))

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsString(result) must include("categories")
    }

    "returns a 400 error when a bad request is made in filteredRequestApiData()" in {
      val result: Future[Result] = apiController.filteredRequestApiData().apply(FakeRequest(POST, controllers.routes.ApiController.filteredRequestApiData().url)
        .withJsonBody(Json.obj()))

      status(result) mustBe 400
    }

    "return park when looking for multiple terms in filteredRequestApiData()" in {
      val result: Future[Result] = apiController.filteredRequestApiData().apply(FakeRequest(POST, controllers.routes.ApiController.filteredRequestApiData().url)
        .withJsonBody(Json.obj("terms" -> List(List("lunch", "dinner", "parks")), "latitude" -> 44.4749, "longitude" -> -73.2121, "radius" -> 40000, "userID" -> "Jimbob", "chance" -> 100)))

      databaseController.deleteUser().apply(FakeRequest(POST, controllers.routes.DatabaseController.deleteUser().url)
        .withJsonBody(Json.obj("username" -> "Jimbob")))
      databaseController.deleteUser().apply(FakeRequest(POST, controllers.routes.DatabaseController.deleteUser().url)
        .withJsonBody(Json.obj("username" -> "Jimbobfriend")))

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsString(result) must include("categories")
      contentAsString(result) must include("park")
    }

    "return yelp api data to the client in requestBusinessByID()" in {
      val result: Future[Result] = apiController.requestBusinessByID().apply(FakeRequest(POST, controllers.routes.ApiController.requestBusinessByID().url)
        .withJsonBody(Json.obj("activityID" -> "EyyfzA0bjLodQHgZhk3GpQ")))

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsString(result) must include("Crescent Beach")
    }

    "returns a 400 error when a bad request is made in requestBusinessByID()" in {
      val result: Future[Result] = apiController.requestBusinessByID().apply(FakeRequest(POST, controllers.routes.ApiController.requestBusinessByID().url)
        .withJsonBody(Json.obj()))

      status(result) mustBe 400
    }

    "return yelp api data to the clint in requestBusinessByAddress" in {
      val result: Future[Result] = apiController.requestBusinessByAddress().apply(FakeRequest(POST, controllers.routes.ApiController.requestBusinessByAddress().url)
        .withJsonBody(Json.obj("name" -> "Crescent Beach", "address" -> "2 Crescent Beach Dr", "city" -> "Burlington", "state" -> "VT", "country" -> "US")))

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsString(result) must include("Crescent Beach")
      contentAsString(result) must include("EyyfzA0bjLodQHgZhk3GpQ")
    }

    "returns a 400 error when a bad request is made in requestBusinessByAddress()" in {
      val result: Future[Result] = apiController.requestBusinessByAddress().apply(FakeRequest(POST, controllers.routes.ApiController.requestBusinessByAddress().url)
        .withJsonBody(Json.obj()))

      status(result) mustBe 400
    }

    "returns a 404 error when a business cannot be found in requestBusinessByAddress()" in {
      val result: Future[Result] = apiController.requestBusinessByAddress().apply(FakeRequest(POST, controllers.routes.ApiController.requestBusinessByAddress().url)
        .withJsonBody(Json.obj("state" -> "VT", "country" -> "US", "name" -> "Skiff Hall", "city" -> "Burlington", "address" -> "163 S Willard St")))

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsString(result) must include("No results found")
    }
  }
}
