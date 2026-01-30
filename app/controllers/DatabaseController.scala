package controllers

import com.typesafe.config.ConfigFactory
import models.handlers.{ApiHandler, DatabaseHandler}
import models.database.{Activity, Trip, User}
import models.helpers.ControllerHelper
import models.input.{ActivityID, Preferences, SaveTrip, TripID, UserFriend, UserID}
import play.api.Configuration
import play.api.libs.json.Format.GenericFormat
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import play.api.libs.json._

import javax.inject.{Inject, Singleton}

@Singleton
class DatabaseController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {
  implicit val activityFormat: OFormat[Activity] = Json.format[Activity]
  implicit val tripFormat: OFormat[Trip] = Json.format[Trip]
  implicit val userFormat: OFormat[User] = Json.format[User]
  implicit val myDataHandler: DatabaseHandler = new DatabaseHandler(new Configuration(ConfigFactory.load("private-variables.conf")))
  implicit val myApiHandler: ApiHandler = new ApiHandler(new Configuration(ConfigFactory.load("private-variables.conf")))
  implicit val controllerHelper: ControllerHelper = new ControllerHelper

  def createActivity(): Action[AnyContent] = Action { implicit request =>
    val activity: Option[Activity] = controllerHelper.makeActivity(request)

    activity match {
      case Some(newItem) =>
        val foundItem = myDataHandler.addActivity(newItem)
        Ok(Json.toJson(foundItem))
      case None =>
        BadRequest
    }
  }

  def getActivity: Action[AnyContent] = Action { implicit request =>
    val activityID: Option[ActivityID] = controllerHelper.makeActivityID(request)

    activityID match {
      case Some(newItem) =>
        val foundItem = myDataHandler.getActivity(newItem.activityID)
        Ok(Json.toJson(foundItem))
      case None =>
        BadRequest
    }
  }

  def deleteActivity(): Action[AnyContent] = Action { implicit request =>
    val activityID: Option[ActivityID] = controllerHelper.makeActivityID(request)

    activityID match {
      case Some(newItem) =>
        val foundItem = myDataHandler.deleteActivity(newItem.activityID)
        Ok(Json.toJson(foundItem))
      case None =>
        BadRequest
    }
  }

  def createTrip(): Action[AnyContent] = Action { implicit request =>
    val saveTrip: Option[SaveTrip] = controllerHelper.makeSaveTrip(request)

    saveTrip match {
      case Some(newItem) =>
        val myTrip = Trip(rating = newItem.rating, owner = newItem.owner, name = newItem.name, description = newItem.description, tripID = newItem.tripID, latitude = newItem.latitude, longitude = newItem.longitude, radius = newItem.radius)
        val categories: List[String] = myApiHandler.getCategories(newItem.activities)
        val activities: List[Activity] = for ((activityID, rating) <- newItem.activities zip newItem.ratings) yield Activity(activityID, rating)
        val response = myDataHandler.saveTrip(myTrip, activities, categories)
        Ok(Json.toJson(response))
      case None =>
        BadRequest
    }
  }

  def getTrip: Action[AnyContent] = Action { implicit request =>
    val trip: Option[TripID] = controllerHelper.makeTripID(request)

    trip match {
      case Some(newItem) =>
        val foundItem = myDataHandler.getTrip(newItem.tripID)
        Ok(Json.toJson(foundItem))
      case None =>
        BadRequest
    }
  }

  def deleteTrip(): Action[AnyContent] = Action { implicit request =>
    val trip: Option[TripID] = controllerHelper.makeTripID(request)

    trip match {
      case Some(newItem) =>
        val foundItem = myDataHandler.deleteTrip(newItem.tripID)
        Ok(Json.toJson(foundItem))
      case None =>
        BadRequest
    }
  }

  def createUser(): Action[AnyContent] = Action { implicit request =>
    val user: Option[User] = controllerHelper.makeUser(request)

    user match {
      case Some(newItem) =>
        val response = myDataHandler.addUser(newItem)
        Ok(Json.toJson(response))
      case None =>
        BadRequest
    }
  }

  def getUser: Action[AnyContent] = Action { implicit request =>
    val username: Option[UserID] = controllerHelper.makeUserID(request)

    username match {
      case Some(newItem) =>
        val foundItem = myDataHandler.getUser(newItem.userID)
        Ok(Json.toJson(foundItem))
      case None =>
        BadRequest
    }
    //val new_interests = foundItem(2).asInstanceOf[java.util.List[String]].asScala.map(x => "\"" + x + "\"").toArray.mkString("[", ", ", "]")
    //val new_preferences = foundItem(3).asInstanceOf[java.util.List[String]].asScala.map(x => "\"" + x + "\"").toArray.mkString("[", ", ", "]")
    //val jsonString = s"{\"username\":\"${foundItem.head}\", \"email\":\"${foundItem(1)}\", \"interests\":${new_interests}, \"preferences\":${new_preferences}}"
    //val jsonValue: JsValue = Json.parse(jsonString)
  }

  def deleteUser(): Action[AnyContent] = Action { implicit request =>
    val userID: Option[UserID] = controllerHelper.makeUserID(request)

    userID match {
      case Some(newItem) =>
        val foundItem = myDataHandler.deleteUser(newItem.userID)
        Ok(Json.toJson(foundItem))
      case None =>
        BadRequest
    }
  }

  def makeFriend(): Action[AnyContent] = Action { implicit request =>
    val user: Option[UserFriend] = controllerHelper.makeUserFriend(request)

    user match {
      case Some(newItem) =>
        val response = myDataHandler.makeFriend(newItem)
        Ok(Json.toJson(response))
      case None =>
        BadRequest
    }
  }

  def getTrips: Action[AnyContent] = Action { implicit request =>
    val username: Option[UserID] = controllerHelper.makeUserID(request)

    username match {
      case Some(newItem) =>
        val response = myDataHandler.getTrips(newItem.userID)
        Ok(Json.toJson(response))
      case None =>
        BadRequest
    }
  }

  def getActivities: Action[AnyContent] = Action { implicit request =>
    val trip: Option[TripID] = controllerHelper.makeTripID(request)

    trip match {
      case Some(newItem) =>
        val foundItem = myDataHandler.getActivities(newItem.tripID)
        Ok(Json.toJson(foundItem))
      case None =>
        BadRequest
    }
  }

  def getFriends: Action[AnyContent] = Action { implicit request =>
    val username: Option[UserID] = controllerHelper.makeUserID(request)

    username match {
      case Some(newItem) =>
        val response = myDataHandler.getFriends(newItem.userID)
        Ok(Json.toJson(response))
      case None =>
        BadRequest
    }
  }

  def getFriendsTrips: Action[AnyContent] = Action { implicit request =>
    val userID: Option[UserID] = controllerHelper.makeUserID(request)

    var jsonString = "["

    userID match {
      case Some(newItem) =>
        val response = myDataHandler.getFriends(newItem.userID)
        for(friend <- response){
          jsonString = jsonString + "{\"userID\": \"" + friend + "\", \"trips\": ["
          val trips = myDataHandler.getTrips(friend)
          for(trip <- trips){
            jsonString = jsonString + "{\"name\": \"" + trip.name + "\", \"rating\": \"" + trip.rating + "\", \"owner\": \"" + trip.owner + "\", \"description\": \"" + trip.description + "\", \"tripID\": \"" + trip.tripID + "\", \"latitude\": " + trip.latitude + ", \"longitude\": " + trip.longitude + ", \"radius\": " + trip.radius + ", \"activities\": ["
            val activities = myDataHandler.getActivities(trip.tripID)
            for(activity <- activities){
              jsonString = jsonString + myApiHandler.getBusiness(activity.activityID) + ", "
            }
            if(activities.nonEmpty) {
              jsonString = jsonString.dropRight(2)
            }
            jsonString = jsonString + "]}, "
          }
          jsonString = jsonString.dropRight(2)
          jsonString = jsonString + "]}, "
        }
        jsonString = jsonString.dropRight(2)
        jsonString = jsonString + "]"
        Ok(Json.parse(jsonString))
      case None =>
        BadRequest
    }
  }

  def updatePreferences(): Action[AnyContent] = Action { implicit request =>
    val preferences: Option[Preferences] = controllerHelper.makePreferences(request)

    preferences match {
      case Some(newItem) =>
        val response = myDataHandler.savePreferences(newItem.userID, newItem.preferences)
        Ok(Json.toJson(response))
      case None =>
        BadRequest
    }
  }

  def getPref: Action[AnyContent] = Action {implicit request =>
    val username: Option[UserID] = controllerHelper.makeUserID(request)

    username match {
      case Some(newItem) =>
        val response = myDataHandler.getNeighborPreferences(newItem.userID)
        Ok(Json.toJson(response))
      case None =>
        BadRequest
    }
  }
}