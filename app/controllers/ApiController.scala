package controllers

import com.typesafe.config.ConfigFactory
import models.handlers.ApiHandler
import models.input.{ActivityID, FilterInputValues, InputValues, NameAndAddress}
import models.helpers.ControllerHelper
import play.api.Configuration
import play.api.libs.json.Format.GenericFormat
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import play.api.libs.json._
import javax.inject.{Inject, Singleton}

@Singleton
class ApiController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {
  implicit val myHandler: ApiHandler = new ApiHandler(new Configuration(ConfigFactory.load("private-variables.conf")))
  implicit val controllerHelper: ControllerHelper = new ControllerHelper

  def requestApiData(): Action[AnyContent] = Action { implicit request =>
    val inputVals: Option[InputValues] = controllerHelper.makeInputValues(request)

    inputVals match {
      case Some(newItem) =>
        val businesses = myHandler.termRequestBusinesses(newItem)
        Ok(Json.toJson(businesses))
      case None =>
        BadRequest
    }
  }

  def filteredRequestApiData(): Action[AnyContent] = Action { implicit request =>
    val inputVals: Option[FilterInputValues] = controllerHelper.makeFilterInputValues(request)

    inputVals match {
      case Some(newItem) =>
        val businesses = myHandler.filterTerms(newItem)
        Ok(Json.toJson(businesses))
      case None =>
        BadRequest
    }
  }

  def requestBusinessByID(): Action[AnyContent] = Action { implicit request =>
    val activityID: Option[ActivityID] = controllerHelper.makeActivityID(request)

    activityID match {
      case Some(newItem) =>
        val foundItem = myHandler.getBusiness(newItem.activityID)
        Ok(Json.toJson(foundItem))
      case None =>
        BadRequest
    }
  }

  def requestBusinessByAddress(): Action[AnyContent] = Action { implicit request =>
    val nameAndAddress: Option[NameAndAddress] = controllerHelper.makeNameAndAddress(request)

    nameAndAddress match {
      case Some(newItem) =>
        val foundItem1 = myHandler.getBusinessByAddress(newItem)
        if((foundItem1 \ "businesses").as[Seq[JsObject]].nonEmpty) {
          val foundItem2 = myHandler.getBusiness(((foundItem1 \ "businesses").as[Seq[JsObject]].head \ "id").as[String])
          Ok(Json.toJson(foundItem2))
        }
        else{
          val errorResponseBody: String = "{\"error\": 404, \"description\": \"No results found\"}"
          val errorJson = Json.parse(errorResponseBody)
          Ok(Json.toJson(errorJson))
        }
      case None =>
        BadRequest
    }
  }
}
