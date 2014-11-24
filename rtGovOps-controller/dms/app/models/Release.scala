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

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}

case class Release(artifact: Artifact,
                   version: Version,
                   binaries: Seq[Binary],
                   dependencies: Seq[Dependency],
                   scripts: Seq[Script])

object Release {

  val json: Writes[Release] =
    ((JsPath \ "version").write[Version] and
     (JsPath \ "binaries").write[Seq[Binary]] and
     (JsPath \ "scripts").write[Seq[Script]]
    ) { release => (release.version, release.binaries, release.scripts)}

  trait Repository {

    def of(artifact: Artifact): Seq[Release]

    def of(dependency: Dependency): Seq[Release] =
      of(dependency.artifact) filter (dependency check _.version)
  }

}
