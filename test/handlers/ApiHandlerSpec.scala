package handlers

import controllers.{ApiController, DatabaseController}
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.libs.json._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.Format.GenericFormat
import play.api.mvc.Result

import scala.concurrent.Future

class ApiHandlerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {
  "ApiHandler filterBusinessesFromRadius()" should {
    val apiController = new ApiController(stubControllerComponents())
    val databaseController = new DatabaseController(stubControllerComponents())

    "respond with a 404 error when businesses cannot be found" in {
      databaseController.createUser().apply(FakeRequest(POST, controllers.routes.DatabaseController.createUser().url)
        .withJsonBody(Json.obj("userID" -> "Jimbob", "email" -> "hello@gmail.com", "name" -> "Jim bob", "interests" -> List[String](), "preferences" -> List[String]())))
      val result: Future[Result] = apiController.filteredRequestApiData().apply(FakeRequest(POST, controllers.routes.ApiController.filteredRequestApiData().url)
        .withJsonBody(Json.obj("terms" -> List(List("zorbing")), "latitude" -> 44.4749, "longitude" -> -73.2121, "radius" -> 40000, "userID" -> "Jimbob", "chance" -> 0)))

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsString(result) must include("No results found")
      contentAsString(result) must include("404")
    }

    "respond with a 404 error when there are no businesses in radius" in {
      val result: Future[Result] = apiController.filteredRequestApiData().apply(FakeRequest(POST, controllers.routes.ApiController.filteredRequestApiData().url)
        .withJsonBody(Json.obj("terms" -> List(List("food")), "latitude" -> 44.4749, "longitude" -> -73.2121, "radius" -> 0, "userID" -> "Jimbob", "chance" -> 0)))
      databaseController.deleteUser().apply(FakeRequest(POST, controllers.routes.DatabaseController.deleteUser().url)
        .withJsonBody(Json.obj("username" -> "Jimbob")))

      status(result) mustBe OK
      contentType(result) mustBe Some("application/json")
      contentAsString(result) must include("No results found in radius for food")
      contentAsString(result) must include("404")
    }
  }
}
