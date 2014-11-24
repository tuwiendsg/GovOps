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

package models

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}

case class Deployment(device: Device.Mac, artifact: Artifact.Name, version: String)

object Deployment {

  implicit val json: Writes[Deployment] =
    ((JsPath \ "deviceId").write[Device.Mac] and
     (JsPath \ "component").write[Artifact.Name] and
     (JsPath \ "version").write[String]
    )(unlift(unapply))

  val form = Form(
      mapping(
        "device" -> nonEmptyText,
        "artifact" -> nonEmptyText,
        "version" -> nonEmptyText
      )(Deployment.apply)(Deployment.unapply)
    )
}
