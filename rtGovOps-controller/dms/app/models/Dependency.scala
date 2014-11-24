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

sealed trait Dependency {

  def artifact: Artifact

  def check: Version.Check
}

object Dependency {

  implicit val json: Writes[Dependency] = Writes {
    case runtime: Runtime => Runtime.json.writes(runtime)
    case execution: Execution => Execution.json.writes(execution)
  }

  case class Runtime(artifact: Artifact, check: Version.Check) extends Dependency

  object Runtime {

    implicit val json: Writes[Runtime] = Dependency.jsonFor("runtime")
  }

  case class Execution(artifact: Artifact, check: Version.Check) extends Dependency

  object Execution {

    implicit val json: Writes[Execution] = Dependency.jsonFor("execution")
  }

  trait Repository {

    def plans: Plan.Repository

    def of(release: Release): Option[Seq[Release]] = plans.of(release).flatMap(_.best)
  }

  private def jsonFor[A <: Dependency](_type: String): Writes[A] =
    ((JsPath \ "type").write[String] and
     (JsPath \ "artifact").write[Artifact] and
     (JsPath \ "version").write[Version.Check]
    ) { dependency => (_type, dependency.artifact, dependency.check)}
}
