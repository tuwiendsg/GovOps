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

import java.net.URI

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}

case class Script(name: Script.Name, uri: URI)

object Script {

  type Name = String

  implicit val json: Writes[Script] =
    ((JsPath \ "name").write[Script.Name] and
     (JsPath \ "uri").write[URI]
    )(unlift(unapply))
}
