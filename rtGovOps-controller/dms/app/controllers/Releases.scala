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

import scala.collection.immutable.Seq

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}
import play.api.mvc._

import models.{Dependency, Release, Repository}

object Releases extends Controller with Repository.Demo {

  implicit val json: Writes[Release] =
    (JsPath.write[Release](Release.json) and
     (JsPath \ "dependencies").write[Seq[Dependency]]
    ) { release => (release, release.dependencies)}

  def list(artifact: String) = Action {
    toJson(artifacts.findByName(artifact) map releases.of)
  }
}
