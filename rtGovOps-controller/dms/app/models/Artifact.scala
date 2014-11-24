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

import scala.collection.immutable.Seq

import play.api.libs.json._
import play.api.libs.functional.syntax._

sealed trait Artifact {

  def name: Artifact.Name
}

object Artifact {

  type Name = String

  implicit val json: Writes[Artifact] = Writes {
    case component: Component => Component.json.writes(component)
    case library: Library => Library.json.writes(library)
    case tool: Tool => Tool.json.writes(tool)
    case environment: Environment => Environment.json.writes(environment)
  }

  case class Component(name: Artifact.Name) extends Artifact

  object Component {

    implicit val json: Writes[Component] = Artifact.jsonFor("component")
  }

  case class Library(name: Artifact.Name) extends Artifact

  object Library {

    implicit val json: Writes[Library] = Artifact.jsonFor("library")
  }

  case class Tool(name: Artifact.Name) extends Artifact

  object Tool {

    implicit val json: Writes[Tool] = Artifact.jsonFor("tool")
  }

  case class Environment(name: Artifact.Name) extends Artifact

  object Environment {

    implicit val json: Writes[Environment] = Artifact.jsonFor("environment")
  }

  trait Repository {

    def all: Seq[Artifact]

    def findByName(name: Artifact.Name): Option[Artifact]
  }

  private def jsonFor[A <: Artifact](_type: String): Writes[A] =
    ((JsPath \ "type").write[String] and
     (JsPath \ "name").write[Artifact.Name]
    ) { artifact => (_type, artifact.name)}
}
