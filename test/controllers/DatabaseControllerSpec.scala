package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.libs.json._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.Format.GenericFormat
import play.api.mvc.Result

import scala.concurrent.Future

class DatabaseControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting {
  "DatabaseController Activity Functions" should {
    val controller = new DatabaseController(stubControllerComponents())

    "create activities in database" in {
      val createResult: Future[Result] = controller.createActivity().apply(FakeRequest(POST, controllers.routes.DatabaseController.createActivity().url)
        .withJsonBody(Json.obj("activityID" -> "1234", "rating" -> 3.5)))

      status(createResult) mustBe OK
      contentType(createResult) mustBe Some("application/json")
      contentAsString(createResult) must include("nodesCreated=1")
    }

    "returns a 400 error when a bad request is made when creating activities" in {
      val result: Future[Result] = controller.createActivity().apply(FakeRequest(POST, controllers.routes.DatabaseController.createActivity().url)
        .withJsonBody(Json.obj()))

      status(result) mustBe 400
    }

    "get activities in database" in {
      val getResult: Future[Result] = controller.getActivity().apply(FakeRequest(POST, controllers.routes.DatabaseController.getActivity().url)
        .withJsonBody(Json.obj("activityID" -> "1234")))

      status(getResult) mustBe OK
      contentType(getResult) mustBe Some("application/json")
      contentAsString(getResult) must include("1234")
      contentAsString(getResult) must include("3.5")
    }

    "returns a 400 error when a bad request is made when getting activities" in {
      val result: Future[Result] = controller.getActivity().apply(FakeRequest(POST, controllers.routes.DatabaseController.getActivity().url)
        .withJsonBody(Json.obj()))

      status(result) mustBe 400
    }

    "delete activities in database" in {
      val delResult: Future[Result] = controller.deleteActivity().apply(FakeRequest(POST, controllers.routes.DatabaseController.deleteActivity().url)
        .withJsonBody(Json.obj("activityID" -> "1234")))

      status(delResult) mustBe OK
      contentType(delResult) mustBe Some("application/json")
      contentAsString(delResult) must include("nodesDeleted=1")
    }

    "returns a 400 error when a bad request is made when deleting activities" in {
      val result: Future[Result] = controller.deleteActivity().apply(FakeRequest(POST, controllers.routes.DatabaseController.deleteActivity().url)
        .withJsonBody(Json.obj()))

      status(result) mustBe 400
    }
  }

  "DatabaseController User Functions" should {
    val controller = new DatabaseController(stubControllerComponents())

    "create users in database" in {
      val createResult: Future[Result] = controller.createUser().apply(FakeRequest(POST, controllers.routes.DatabaseController.createUser().url)
        .withJsonBody(Json.obj("userID" -> "1234", "email" -> "hello@gmail.com", "name" -> "Jimbob", "interests" -> List[String](), "preferences" -> List[String]())))

      status(createResult) mustBe OK
      contentType(createResult) mustBe Some("application/json")
      contentAsString(createResult) must include("nodesCreated=1")
    }

    "returns a 400 error when a bad request is made when creating users" in {
      val result: Future[Result] = controller.createUser().apply(FakeRequest(POST, controllers.routes.DatabaseController.createUser().url)
        .withJsonBody(Json.obj()))

      status(result) mustBe 400
    }

    "get users in database" in {
      val getResult: Future[Result] = controller.getUser().apply(FakeRequest(POST, controllers.routes.DatabaseController.getUser().url)
        .withJsonBody(Json.obj("userID" -> "1234")))

      status(getResult) mustBe OK
      contentType(getResult) mustBe Some("application/json")
      contentAsString(getResult) must include("1234")
      contentAsString(getResult) must include("hello@gmail.com")
      contentAsString(getResult) must include("Jimbob")
    }

    "returns a 400 error when a bad request is made when getting users" in {
      val result: Future[Result] = controller.getUser().apply(FakeRequest(POST, controllers.routes.DatabaseController.getUser().url)
        .withJsonBody(Json.obj()))

      status(result) mustBe 400
    }

    "delete users in database" in {
      val delResult: Future[Result] = controller.deleteUser().apply(FakeRequest(POST, controllers.routes.DatabaseController.deleteUser().url)
        .withJsonBody(Json.obj("userID" -> "1234")))

      status(delResult) mustBe OK
      contentType(delResult) mustBe Some("application/json")
      contentAsString(delResult) must include("nodesDeleted=1")
    }

    "returns a 400 error when a bad request is made when deleting users" in {
      val result: Future[Result] = controller.deleteUser().apply(FakeRequest(POST, controllers.routes.DatabaseController.deleteUser().url)
        .withJsonBody(Json.obj()))

      status(result) mustBe 400
    }
  }

  "DatabaseController Trip Functions" should {
    val controller = new DatabaseController(stubControllerComponents())

    "create trips in database" in {
      val createUserResult: Future[Result] = controller.createUser().apply(FakeRequest(POST, controllers.routes.DatabaseController.createUser().url)
        .withJsonBody(Json.obj("userID" -> "Jimbob", "email" -> "hello@gmail.com", "name" -> "Jim bob", "interests" -> List[String](), "preferences" -> List[String]())))
      val createTripResult: Future[Result] = controller.createTrip().apply(FakeRequest(POST, controllers.routes.DatabaseController.createTrip().url)
        .withJsonBody(Json.obj("rating" -> 3.5, "owner" -> "Jimbob", "name" -> "hehe trip time", "description" -> "fun trip", "tripID" -> "1234", "latitude" -> 40, "longitude" -> 50, "radius" -> 100, "activities" -> List[String]("EyyfzA0bjLodQHgZhk3GpQ"), "ratings" -> List[Float](4.5.toFloat))))

      status(createTripResult) mustBe OK
      contentType(createTripResult) mustBe Some("application/json")

      status(createUserResult) mustBe OK
      contentType(createUserResult) mustBe Some("application/json")
      contentAsString(createUserResult) must include("nodesCreated=1")
    }

    "returns a 400 error when a bad request is made when creating trips" in {
      val result: Future[Result] = controller.createTrip().apply(FakeRequest(POST, controllers.routes.DatabaseController.createTrip().url)
        .withJsonBody(Json.obj()))

      status(result) mustBe 400
    }

    "get trips in database" in {
      val getTripResult: Future[Result] = controller.getTrip().apply(FakeRequest(POST, controllers.routes.DatabaseController.getTrip().url)
        .withJsonBody(Json.obj("tripID" -> "1234")))
      val getActivityResult: Future[Result] = controller.getActivity().apply(FakeRequest(POST, controllers.routes.DatabaseController.getActivity().url)
        .withJsonBody(Json.obj("activityID" -> "EyyfzA0bjLodQHgZhk3GpQ")))

      status(getTripResult) mustBe OK
      contentType(getTripResult) mustBe Some("application/json")
      contentAsString(getTripResult) must include("3.5")
      contentAsString(getTripResult) must include("Jimbob")
      contentAsString(getTripResult) must include("hehe trip time")
      contentAsString(getTripResult) must include("fun trip")
      contentAsString(getTripResult) must include("1234")
      contentAsString(getTripResult) must include("40")
      contentAsString(getTripResult) must include("50")
      contentAsString(getTripResult) must include("100")

      status(getActivityResult) mustBe OK
      contentType(getActivityResult) mustBe Some("application/json")
      contentAsString(getActivityResult) must include("EyyfzA0bjLodQHgZhk3GpQ")
      contentAsString(getActivityResult) must include("4.5")
    }

    "returns a 400 error when a bad request is made when getting trips" in {
      val result: Future[Result] = controller.getTrip().apply(FakeRequest(POST, controllers.routes.DatabaseController.getTrip().url)
        .withJsonBody(Json.obj()))

      status(result) mustBe 400
    }

    "delete trips in database" in {
      val delTripResult: Future[Result] = controller.deleteTrip().apply(FakeRequest(POST, controllers.routes.DatabaseController.deleteTrip().url)
        .withJsonBody(Json.obj("tripID" -> "1234")))
      val delActivityResult: Future[Result] = controller.deleteActivity().apply(FakeRequest(POST, controllers.routes.DatabaseController.deleteActivity().url)
        .withJsonBody(Json.obj("activityID" -> "EyyfzA0bjLodQHgZhk3GpQ")))
      val delUserResult: Future[Result] = controller.deleteUser().apply(FakeRequest(POST, controllers.routes.DatabaseController.deleteUser().url)
        .withJsonBody(Json.obj("userID" -> "Jimbob")))

      status(delTripResult) mustBe OK
      contentType(delTripResult) mustBe Some("application/json")
      contentAsString(delTripResult) must include("nodesDeleted=1")

      status(delActivityResult) mustBe OK
      contentType(delActivityResult) mustBe Some("application/json")
      contentAsString(delActivityResult) must include("nodesDeleted=1")

      status(delUserResult) mustBe OK
      contentType(delUserResult) mustBe Some("application/json")
    }

    "returns a 400 error when a bad request is made when deleting trips" in {
      val result: Future[Result] = controller.deleteTrip().apply(FakeRequest(POST, controllers.routes.DatabaseController.deleteTrip().url)
        .withJsonBody(Json.obj()))

      status(result) mustBe 400
    }
  }

  "DatabaseController Miscellaneous Functions" should {
    val controller = new DatabaseController(stubControllerComponents())

    "make friends between users in database" in {
      val createUser1Result: Future[Result] = controller.createUser().apply(FakeRequest(POST, controllers.routes.DatabaseController.createUser().url)
        .withJsonBody(Json.obj("userID" -> "Jimbob", "email" -> "hello@gmail.com", "name" -> "Jim bob", "interests" -> List[String](), "preferences" -> List[String]())))
      val createUser2Result: Future[Result] = controller.createUser().apply(FakeRequest(POST, controllers.routes.DatabaseController.createUser().url)
        .withJsonBody(Json.obj("userID" -> "Jimbobfriend", "email" -> "hi@gmail.com", "name" -> "Jim bob friend", "interests" -> List[String](), "preferences" -> List[String]())))

      status(createUser1Result) mustBe OK
      contentType(createUser1Result) mustBe Some("application/json")
      contentAsString(createUser1Result) must include("nodesCreated=1")

      status(createUser2Result) mustBe OK
      contentType(createUser2Result) mustBe Some("application/json")
      contentAsString(createUser2Result) must include("nodesCreated=1")

      val createFriendResult: Future[Result] = controller.makeFriend().apply(FakeRequest(POST, controllers.routes.DatabaseController.makeFriend().url)
        .withJsonBody(Json.obj("user1" -> "Jimbob", "user2" -> "Jimbobfriend")))

      status(createFriendResult) mustBe OK
      contentType(createFriendResult) mustBe Some("application/json")
    }

    "returns a 400 error when a bad request is made when making friends" in {
      val result: Future[Result] = controller.makeFriend().apply(FakeRequest(POST, controllers.routes.DatabaseController.makeFriend().url)
        .withJsonBody(Json.obj()))

      status(result) mustBe 400
    }

    "get friends from users in database" in {
      val getFriend1Result: Future[Result] = controller.getFriends().apply(FakeRequest(POST, controllers.routes.DatabaseController.getFriends().url)
        .withJsonBody(Json.obj("userID" -> "Jimbob")))
      val getFriend2Result: Future[Result] = controller.getFriends().apply(FakeRequest(POST, controllers.routes.DatabaseController.getFriends().url)
        .withJsonBody(Json.obj("userID" -> "Jimbobfriend")))

      status(getFriend1Result) mustBe OK
      contentType(getFriend1Result) mustBe Some("application/json")
      contentAsString(getFriend1Result) must include("Jimbobfriend")

      status(getFriend2Result) mustBe OK
      contentType(getFriend2Result) mustBe Some("application/json")
      contentAsString(getFriend2Result) must include("Jimbob")
    }

    "returns a 400 error when a bad request is made when getting friends" in {
      val result: Future[Result] = controller.getFriends().apply(FakeRequest(POST, controllers.routes.DatabaseController.getFriends().url)
        .withJsonBody(Json.obj()))

      status(result) mustBe 400
    }

    "get trips from users in database" in {
      val createTripResult: Future[Result] = controller.createTrip().apply(FakeRequest(POST, controllers.routes.DatabaseController.createTrip().url)
        .withJsonBody(Json.obj("rating" -> 3.5, "owner" -> "Jimbob", "name" -> "hehe trip time", "description" -> "fun trip", "tripID" -> "1234", "latitude" -> 40, "longitude" -> 50, "radius" -> 100, "activities" -> List[String]("EyyfzA0bjLodQHgZhk3GpQ"), "ratings" -> List[Float](4.5.toFloat))))

      status(createTripResult) mustBe OK
      contentType(createTripResult) mustBe Some("application/json")

      val getTripsResult: Future[Result] = controller.getTrips().apply(FakeRequest(POST, controllers.routes.DatabaseController.getTrips().url)
        .withJsonBody(Json.obj("userID" -> "Jimbob")))

      status(getTripsResult) mustBe OK
      contentType(getTripsResult) mustBe Some("application/json")
      contentAsString(getTripsResult) must include("3.5")
      contentAsString(getTripsResult) must include("Jimbob")
      contentAsString(getTripsResult) must include("hehe trip time")
      contentAsString(getTripsResult) must include("fun trip")
      contentAsString(getTripsResult) must include("1234")
      contentAsString(getTripsResult) must include("40")
      contentAsString(getTripsResult) must include("50")
      contentAsString(getTripsResult) must include("100")
    }

    "returns a 400 error when a bad request is made when getting trips from users" in {
      val result: Future[Result] = controller.getTrips().apply(FakeRequest(POST, controllers.routes.DatabaseController.getTrips().url)
        .withJsonBody(Json.obj()))

      status(result) mustBe 400
    }

    "get activities from trips in database" in {
      val getActivitiesResult: Future[Result] = controller.getActivities().apply(FakeRequest(POST, controllers.routes.DatabaseController.getActivities().url)
        .withJsonBody(Json.obj("tripID" -> "1234")))

      status(getActivitiesResult) mustBe OK
      contentType(getActivitiesResult) mustBe Some("application/json")
      contentAsString(getActivitiesResult) must include("4.5")
      contentAsString(getActivitiesResult) must include("EyyfzA0bjLodQHgZhk3GpQ")
    }

    "returns a 400 error when a bad request is made when getting activities from trips" in {
      val result: Future[Result] = controller.getActivities().apply(FakeRequest(POST, controllers.routes.DatabaseController.getActivities().url)
        .withJsonBody(Json.obj()))

      status(result) mustBe 400
    }

    "get friends trips in database" in {
      val getFriendsTripsResult: Future[Result] = controller.getFriendsTrips().apply(FakeRequest(POST, controllers.routes.DatabaseController.getFriendsTrips().url)
        .withJsonBody(Json.obj("userID" -> "Jimbobfriend")))

      status(getFriendsTripsResult) mustBe OK
      contentType(getFriendsTripsResult) mustBe Some("application/json")
      contentAsString(getFriendsTripsResult) must include("EyyfzA0bjLodQHgZhk3GpQ")
      contentAsString(getFriendsTripsResult) must include("3.5")
      contentAsString(getFriendsTripsResult) must include("Jimbob")
      contentAsString(getFriendsTripsResult) must include("hehe trip time")
      contentAsString(getFriendsTripsResult) must include("fun trip")
      contentAsString(getFriendsTripsResult) must include("1234")
      contentAsString(getFriendsTripsResult) must include("40")
      contentAsString(getFriendsTripsResult) must include("50")
      contentAsString(getFriendsTripsResult) must include("100")
    }

    "returns a 400 error when a bad request is made when getting friends trips" in {
      val result: Future[Result] = controller.getFriendsTrips().apply(FakeRequest(POST, controllers.routes.DatabaseController.getFriendsTrips().url)
        .withJsonBody(Json.obj()))

      status(result) mustBe 400
    }

    "update user preferences in database" in {
      val setPreferencesResult: Future[Result] = controller.updatePreferences().apply(FakeRequest(POST, controllers.routes.DatabaseController.updatePreferences().url)
        .withJsonBody(Json.obj("userID" -> "Jimbob", "preferences" -> List[String]("camping", "eating"))))

      status(setPreferencesResult) mustBe OK
      contentType(setPreferencesResult) mustBe Some("application/json")
    }

    "returns a 400 error when a bad request is made when setting preferences" in {
      val result: Future[Result] = controller.updatePreferences().apply(FakeRequest(POST, controllers.routes.DatabaseController.updatePreferences().url)
        .withJsonBody(Json.obj()))

      status(result) mustBe 400
    }

    "get user preferences in database" in {
      val getPreferencesResult: Future[Result] = controller.getPref().apply(FakeRequest(POST, controllers.routes.DatabaseController.getPref().url)
        .withJsonBody(Json.obj("userID" -> "Jimbobfriend")))

      status(getPreferencesResult) mustBe OK
      contentType(getPreferencesResult) mustBe Some("application/json")
      contentAsString(getPreferencesResult) must include("camping")
      contentAsString(getPreferencesResult) must include("eating")
    }

    "returns a 400 error when a bad request is made when getting preferences" in {
      val result: Future[Result] = controller.getPref().apply(FakeRequest(POST, controllers.routes.DatabaseController.getPref().url)
        .withJsonBody(Json.obj()))

      status(result) mustBe 400
    }

    "delete users, activities, and trips in database" in {
      val delTripResult: Future[Result] = controller.deleteTrip().apply(FakeRequest(POST, controllers.routes.DatabaseController.deleteTrip().url)
        .withJsonBody(Json.obj("tripID" -> "1234")))
      val delActivityResult: Future[Result] = controller.deleteActivity().apply(FakeRequest(POST, controllers.routes.DatabaseController.deleteActivity().url)
        .withJsonBody(Json.obj("activityID" -> "EyyfzA0bjLodQHgZhk3GpQ")))
      val delUser1Result: Future[Result] = controller.deleteUser().apply(FakeRequest(POST, controllers.routes.DatabaseController.deleteUser().url)
        .withJsonBody(Json.obj("userID" -> "Jimbob")))
      val delUser2Result: Future[Result] = controller.deleteUser().apply(FakeRequest(POST, controllers.routes.DatabaseController.deleteUser().url)
        .withJsonBody(Json.obj("userID" -> "Jimbobfriend")))

      status(delTripResult) mustBe OK
      contentType(delTripResult) mustBe Some("application/json")
      contentAsString(delTripResult) must include("nodesDeleted=1")

      status(delActivityResult) mustBe OK
      contentType(delActivityResult) mustBe Some("application/json")
      contentAsString(delActivityResult) must include("nodesDeleted=1")

      status(delUser1Result) mustBe OK
      contentType(delUser1Result) mustBe Some("application/json")
      contentAsString(delUser1Result) must include("nodesDeleted=1")

      status(delUser2Result) mustBe OK
      contentType(delUser2Result) mustBe Some("application/json")
    }
  }
}
