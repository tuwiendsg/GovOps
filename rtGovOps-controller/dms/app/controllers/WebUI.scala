/*
 * Copyright 2014 Sanjin Sehic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import scala.collection.immutable.Vector
import scala.concurrent.Future
import scala.concurrent.duration._

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.libs.ws._
import play.api.mvc._
import play.api.Play.current
import play.mvc.Http

import models.{Deployment, Device, Repository, Version}

object WebUI extends Controller with Repository.Demo {

  private val configuration = current.configuration

  private object builder {

    val timeout =
      configuration.getMilliseconds("builder.timeout").getOrElse(10.seconds.toMillis).toInt

    val build = {
      val host = configuration.getString("builder.host").getOrElse("localhost")
      val port = configuration.getInt("builder.port").getOrElse(8080)
      val path = configuration
        .getString("builder.path.build")
        .getOrElse("SDGBuilder/artifact-builder/build")

      s"http://$host:$port/$path"
    }
  }

  private object manager {

    val timeout =
      configuration.getMilliseconds("manager.timeout").getOrElse(10.seconds.toMillis).toInt

    def profile(mac: Device.Mac) = {
      val host = configuration.getString("manager.host").getOrElse("localhost")
      val port = configuration.getInt("manager.port").getOrElse(8080)
      val path = configuration
        .getString("manager.path.build")
        .getOrElse("SDGManager/device-manager/profile")

      s"http://$host:$port/$path/$mac"
    }
  }

  def index = Action {
    implicit request =>
      Ok(views.html.index(devices.all, artifacts.all))
  }

  def profile(mac: String) = Action.async {
    val redirect = Redirect(routes.WebUI.index())
    WS.url(manager.profile(mac))
      .withRequestTimeout(manager.timeout)
      .get() map {
      case response if response.status == Http.Status.OK =>
        Json.fromJson[Device](Json.parse(response.body)) match {
          case JsSuccess(device, _) => Ok(views.html.profile(Right(device)))
          case JsError(_) => Ok(views.html.profile(Left("Parsing JSON was unsuccessful")))
        }
      case response =>
        Ok(views.html.profile(Left(s"Getting device profile returned status ${response.status}")))
    } recover {
      case error => Ok(views.html.profile(Left(error.getMessage)))
    }
  }

  def versions(name: String) = Action {
    val artifact = artifacts.findByName(name)
    Ok(views.html.releases(artifact, artifact.map(releases.of).getOrElse(Vector.empty)))
  }

  def dependencies(name: String, version: Version) = Action {
    val artifact = artifacts.findByName(name)
    val release = artifact.map(releases.of).flatMap(_.find(version == _.version))
    Ok(views.html.dependencies(release))
  }

  def deploy = Action.async {
    implicit request =>
      val redirect = Redirect(routes.WebUI.index())
      Deployment.form.bindFromRequest.fold(
        form => Future.successful(
          redirect.flashing("error" -> "Device, artifact, and version must be selected")
        ),
        deployment =>
          WS.url(builder.build)
            .withRequestTimeout(builder.timeout)
            .post(Json.toJson(deployment)) map {
            case response if response.status == Http.Status.OK =>
              redirect.flashing("success" -> "Deployment successful")
            case response =>
              redirect.flashing("error" -> s"Deployment unsuccessful (status: ${response.status})")
          } recover {
            case error =>
              redirect.flashing("error" -> s"Deployment unsuccessful (error: ${error.getMessage})")
          }
      )
  }
}
