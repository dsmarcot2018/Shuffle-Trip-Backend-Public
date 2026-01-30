package models.helpers

import models.database.{Activity, Trip, User}
import models.input.{ActivityID, FilterInputValues, InputValues, NameAndAddress, Preferences, SaveTrip, TripID, UserFriend, UserID}
import play.api.libs.json.{JsValue, Json, OFormat}
import play.api.mvc.{AnyContent, Request}

class ControllerHelper {
  implicit val inputValues: OFormat[InputValues] = Json.format[InputValues]
  implicit val filterInputValues: OFormat[FilterInputValues] = Json.format[FilterInputValues]
  implicit val activityIDFormat: OFormat[ActivityID] = Json.format[ActivityID]
  implicit val nameAndAddressFormat: OFormat[NameAndAddress] = Json.format[NameAndAddress]
  implicit val activityFormat: OFormat[Activity] = Json.format[Activity]
  implicit val tripFormat: OFormat[Trip] = Json.format[Trip]
  implicit val saveTripFormat: OFormat[SaveTrip] = Json.format[SaveTrip]
  implicit val userFormat: OFormat[User] = Json.format[User]
  implicit val userFriendFormat: OFormat[UserFriend] = Json.format[UserFriend]
  implicit val usernameFormat: OFormat[UserID] = Json.format[UserID]
  implicit val preferencesFormat: OFormat[Preferences] = Json.format[Preferences]
  implicit val tripIDFormat: OFormat[TripID] = Json.format[TripID]

  private def getJsonContent(request: Request[AnyContent]): Option[JsValue] = {
    request.body.asJson
  }

  def makeInputValues(request: Request[AnyContent]): Option[InputValues] = {
    getJsonContent(request).flatMap(Json.fromJson[InputValues](_).asOpt)
  }

  def makeFilterInputValues(request: Request[AnyContent]): Option[FilterInputValues] = {
    getJsonContent(request).flatMap(Json.fromJson[FilterInputValues](_).asOpt)
  }

  def makeActivityID(request: Request[AnyContent]): Option[ActivityID] = {
    getJsonContent(request).flatMap(Json.fromJson[ActivityID](_).asOpt)
  }

  def makeNameAndAddress(request: Request[AnyContent]): Option[NameAndAddress] = {
    getJsonContent(request).flatMap(Json.fromJson[NameAndAddress](_).asOpt)
  }

  def makeActivity(request: Request[AnyContent]): Option[Activity] = {
    getJsonContent(request).flatMap(Json.fromJson[Activity](_).asOpt)
  }

  def makeSaveTrip(request: Request[AnyContent]): Option[SaveTrip] = {
    getJsonContent(request).flatMap(Json.fromJson[SaveTrip](_).asOpt)
  }

  def makeUser(request: Request[AnyContent]): Option[User] = {
    getJsonContent(request).flatMap(Json.fromJson[User](_).asOpt)
  }

  def makeUserFriend(request: Request[AnyContent]): Option[UserFriend] = {
    getJsonContent(request).flatMap(Json.fromJson[UserFriend](_).asOpt)
  }

  def makeUserID(request: Request[AnyContent]): Option[UserID] = {
    getJsonContent(request).flatMap(Json.fromJson[UserID](_).asOpt)
  }

  def makePreferences(request: Request[AnyContent]): Option[Preferences] = {
    getJsonContent(request).flatMap(Json.fromJson[Preferences](_).asOpt)
  }

  def makeTripID(request: Request[AnyContent]): Option[TripID] = {
    getJsonContent(request).flatMap(Json.fromJson[TripID](_).asOpt)
  }
}
