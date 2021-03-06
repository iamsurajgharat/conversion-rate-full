package com.surajgharat.conversionrates.controllers
import com.github.nscala_time.time.Imports._
import com.surajgharat.conversionrates.models._
import com.surajgharat.conversionrates.repositories._
import com.surajgharat.conversionrates.services._
import com.surajgharat.conversionrates.helpers._
import org.joda.time
import play.api._
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json._
import play.api.mvc._
import zio.Task
import zio.UIO
import zio.ZIO

import javax.inject._
import scala.concurrent.Future
import java.io.StringWriter
import java.io.PrintWriter


@Singleton
class ConversionRateController @Inject() (
        val rateService: ConversionRateService,
        val controllerComponents: ControllerComponents
    ) extends BaseController {
        
    import BaseResponse._
    import Repository._
    import ZIOHelper._
    implicit val ec = controllerComponents.executionContext
    private val logger = Logger(getClass)

    def greet = Action { request =>
        Ok("All is well")
    }
    
    def getRates() = zioActionWithBody { request => 
        def validateError(errors: scala.collection.Seq[(JsPath, scala.collection.Seq[JsonValidationError])]) : UIO[Result] = {
            val validationError = ZIO.attempt(Json.obj("errors" -> JsError.toJson(errors))).
                orElse(ZIO.succeed(Json.obj("errors" -> "Error in parsing input")))
            validationError.map(BadRequest(_))
        }

        def validateSuccess(request:List[ConversionRateRequest]) : UIO[Result] = {
            rateService.getRates(request).fold(handleInternalError, handleSuccess)
        }

        request.body.validate[List[ConversionRateRequest]].fold(validateError, validateSuccess)
    }

    def getAllRates() = zioAction { _ =>
        rateService.getAllRates().fold(handleInternalError, handleSuccess)
    }

    def saveRates() = zioActionWithBody { request => 
        def validateError(errors: scala.collection.Seq[(JsPath, scala.collection.Seq[JsonValidationError])]) : UIO[Result] = {
            val validationError = ZIO.attempt(Json.obj("errors" -> JsError.toJson(errors))).
                orElse(ZIO.succeed(Json.obj("errors" -> "Error in parsing input")))
            validationError.map(BadRequest(_))
        }

        def validateSuccess(rates:List[ConversionRate]) : UIO[Result] = {
            rateService.saveRates(rates).fold(handleInternalError, handleSuccess)
        }

        request.body.validate[List[ConversionRate]].fold(validateError, validateSuccess)
    }

    def zioAction(actionFun : Request[AnyContent] => UIO[Result]):Action[AnyContent] = {
        Action { request =>
            ((interpret[Result] _) compose actionFun)(request)
        }
    }

    def zioActionWithBody(actionFun : Request[JsValue] => UIO[Result]):Action[JsValue] = {
        Action(parse.json) { request =>
            ((interpret[Result] _) compose actionFun)(request)
        }
    }
    
    //private def interpret(effect: UIO[Result]):Result = zio.Runtime.default.unsafeRunTask(effect)
    private def handleInternalError(e:Throwable):Result = e match {
        case _ : ValidationException => 
            BadRequest(e.getMessage())
        case _ => 
            e.printStackTrace()
            InternalServerError(e.getMessage())
    }

    private def getStackTrace(ex:Throwable):String = {
        val sw = new StringWriter()
        val pw = new PrintWriter(sw)
        ex.printStackTrace(pw)
        sw.toString()
    }

    //import ai.x.play.json.Jsonx
    private def handleSuccess[T <: BaseResponse](data:List[T]):Result = Ok(toJson(data))

    private def toJson[T <: BaseResponse](data : List[T]):JsValue = JsArray(data.map(toJson(_)))

    private def toJson[T <: BaseResponse](data : T) : JsValue = data match {
        case req : ConversionRateRequest => Json.toJson(req)
        case res : ConversionRateResponse => Json.toJson(res)
        case rate : ConversionRate => Json.toJson(rate)
    }
}


