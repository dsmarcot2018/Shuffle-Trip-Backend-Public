package handlers

import controllers.DatabaseController
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.libs.json._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.Format.GenericFormat
import play.api.mvc.Result

import scala.concurrent.Future

class DatabaseHandlerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {
  "DatabaseHandler saveTrip()" should {
    val controller = new DatabaseController(stubControllerComponents())

    "return an error message if the trip owner does not exist" in {
      val createTripResult: Future[Result] = controller.createTrip().apply(FakeRequest(POST, controllers.routes.DatabaseController.createTrip().url)
        .withJsonBody(Json.obj("rating" -> 3.5, "owner" -> "MyMainManJimbob", "name" -> "hehe trip time", "description" -> "fun trip", "tripID" -> "1234", "latitude" -> 40, "longitude" -> 50, "radius" -> 100, "activities" -> List[String]("EyyfzA0bjLodQHgZhk3GpQ"), "ratings" -> List[Float](4.5.toFloat))))

      status(createTripResult) mustBe OK
      contentType(createTripResult) mustBe Some("application/json")
      contentAsString(createTripResult) must include("Error, owner does not exist")
    }
  }

  "DatabaseHandler updateNeighbors" should {
    val controller = new DatabaseController(stubControllerComponents())

    "make two users neighbors if they are not already" in {
      controller.createUser().apply(FakeRequest(POST, controllers.routes.DatabaseController.createUser().url)
        .withJsonBody(Json.obj("userID" -> "Jimbob", "email" -> "hello@gmail.com", "name" -> "Jim bob", "interests" -> List[String](), "preferences" -> List[String]())))
      val createTrip1Result: Future[Result] = controller.createTrip().apply(FakeRequest(POST, controllers.routes.DatabaseController.createTrip().url)
        .withJsonBody(Json.obj("rating" -> 3.5, "owner" -> "Jimbob", "name" -> "hehe trip time", "description" -> "fun trip", "tripID" -> "1234", "latitude" -> 40, "longitude" -> 50, "radius" -> 100, "activities" -> List[String]("EyyfzA0bjLodQHgZhk3GpQ"), "ratings" -> List[Float](4.5.toFloat))))
      controller.createUser().apply(FakeRequest(POST, controllers.routes.DatabaseController.createUser().url)
        .withJsonBody(Json.obj("userID" -> "Jimbobfriend", "email" -> "hi@gmail.com", "name" -> "Jim bob friend", "interests" -> List[String](), "preferences" -> List[String]())))
      val createTrip2Result: Future[Result] = controller.createTrip().apply(FakeRequest(POST, controllers.routes.DatabaseController.createTrip().url)
        .withJsonBody(Json.obj("rating" -> 3.5, "owner" -> "Jimbobfriend", "name" -> "hehe trip time", "description" -> "fun trip", "tripID" -> "1234", "latitude" -> 40, "longitude" -> 50, "radius" -> 100, "activities" -> List[String]("EyyfzA0bjLodQHgZhk3GpQ"), "ratings" -> List[Float](4.5.toFloat))))

      status(createTrip1Result) mustBe OK
      contentType(createTrip1Result) mustBe Some("application/json")

      status(createTrip2Result) mustBe OK
      contentType(createTrip2Result) mustBe Some("application/json")
    }

    "update the neighbor strength between two users if they are already neighbors" in {
      val createTrip1Result: Future[Result] = controller.createTrip().apply(FakeRequest(POST, controllers.routes.DatabaseController.createTrip().url)
        .withJsonBody(Json.obj("rating" -> 3.5, "owner" -> "Jimbob", "name" -> "hehe trip time 2", "description" -> "fun trip", "tripID" -> "4321", "latitude" -> 50, "longitude" -> 40, "radius" -> 100, "activities" -> List[String]("sh9sPakaQqVYc3hOtV-x6g"), "ratings" -> List[Float](4.5.toFloat))))
      val createTrip2Result: Future[Result] = controller.createTrip().apply(FakeRequest(POST, controllers.routes.DatabaseController.createTrip().url)
        .withJsonBody(Json.obj("rating" -> 3.5, "owner" -> "Jimbobfriend", "name" -> "hehe trip time 2", "description" -> "fun trip", "tripID" -> "4321", "latitude" -> 50, "longitude" -> 40, "radius" -> 100, "activities" -> List[String]("sh9sPakaQqVYc3hOtV-x6g"), "ratings" -> List[Float](4.5.toFloat))))
      controller.deleteUser().apply(FakeRequest(POST, controllers.routes.DatabaseController.deleteUser().url)
        .withJsonBody(Json.obj("userID" -> "Jimbob")))
      controller.deleteUser().apply(FakeRequest(POST, controllers.routes.DatabaseController.deleteUser().url)
        .withJsonBody(Json.obj("userID" -> "Jimbobfriend")))
      controller.deleteTrip().apply(FakeRequest(POST, controllers.routes.DatabaseController.deleteTrip().url)
        .withJsonBody(Json.obj( "tripID" -> "1234")))
      controller.deleteTrip().apply(FakeRequest(POST, controllers.routes.DatabaseController.deleteTrip().url)
        .withJsonBody(Json.obj( "tripID" -> "4321")))
      controller.deleteActivity().apply(FakeRequest(POST, controllers.routes.DatabaseController.deleteActivity().url)
        .withJsonBody(Json.obj("activityID" -> "EyyfzA0bjLodQHgZhk3GpQ")))
      controller.deleteActivity().apply(FakeRequest(POST, controllers.routes.DatabaseController.deleteActivity().url)
        .withJsonBody(Json.obj("activityID" -> "sh9sPakaQqVYc3hOtV-x6g")))

      status(createTrip1Result) mustBe OK
      contentType(createTrip1Result) mustBe Some("application/json")

      status(createTrip2Result) mustBe OK
      contentType(createTrip2Result) mustBe Some("application/json")
    }
  }
}
