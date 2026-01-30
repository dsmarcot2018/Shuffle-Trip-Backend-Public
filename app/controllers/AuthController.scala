package controllers

import com.typesafe.config.ConfigFactory
import models.handlers.ApiHandler
import models.input.{FilterInputValues, InputValues}
import play.api.Configuration
import play.api.libs.json.{Json, OFormat}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}

import javax.inject.{Inject, Singleton}

@Singleton
class AuthController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

}
