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

import scala.collection.immutable.{Seq, Vector}
import scala.collection.mutable

case class Plan(artifact: Artifact, candidates: Seq[(Release, Seq[Plan])]) {

  require(candidates.nonEmpty, "Candidates cannot be empty")
  require(candidates.forall(artifact == _._1.artifact), "All releases must be for same artifact")

  lazy val best: Option[Seq[Release]] = resolve(Vector.empty, this)

  def valid(release: Release): Boolean = {
    // if it is same artifact than it has to be in the list of possible candidates releases
    if (release.artifact == artifact) candidates exists (release == _._1)
    // or it is a valid release for all dependency plans in at least one candidate release
    else candidates exists (_._2 forall (_ valid release))
  }

  private def resolve(current: Seq[Release], root: Plan): Option[Seq[Release]] = {
    current.find(artifact == _.artifact) match {
      case Some(release) =>
        // if a release for the artifact has been already found, than that release has to be one of
        // the current candidate releases
        if (candidates exists (release == _._1)) Some(current)
        else throw new IllegalArgumentException(s"$release is not supported by $this")
      case None =>
        var result: Option[Seq[Release]] = None

        // if a release for the artifact has not been found, than we have to go through all
        // candidate releases, find one that is valid for whole plan and resolve all its
        // dependencies
        val i = candidates.sortBy(_._1.version).reverse.iterator
        while (result.isEmpty && i.hasNext) {
          val (release, plans) = i.next()

          if (root valid release) {
            result = Some(current)

            val j = plans.iterator
            while (result.isDefined && j.hasNext) {
              val plan = j.next()
              result = plan.resolve(result.get, root)
            }

            result = result map (_ :+ release)
          }
        }

        result
    }
  }
}

object Plan {

  trait Repository {

    def releases: Release.Repository

    def of(release: Release): Option[Plan] = {
      var result: Option[mutable.Builder[Plan, Vector[Plan]]] = Some(Vector.newBuilder)

      // go through all dependencies of the release and create plans for them
      val i = release.dependencies.iterator
      while (result.isDefined && i.hasNext) {
        result = of(i.next()) map (result.get += _)
      }

      // create a plan using this release and plans for its dependencies
      result map (plans => Plan(release.artifact, Vector(release -> plans.result())))
    }

    private def of(dependency: Dependency): Option[Plan] = {
      val versions = releases.of(dependency)
      val builder = Vector.newBuilder[(Release, Seq[Plan])]
      builder.sizeHint(versions.size)

      // go through all releases, create a plan for them and add all candidate releases to the list
      // of all possible candidate releases
      val i = versions.iterator
      while (i.hasNext) {
        of(i.next()) match {
          case Some(plan) => builder ++= plan.candidates
          case _ => /* nothing */
        }
      }

      val candidates = builder.result()
      if (candidates.isEmpty) None else Some(Plan(dependency.artifact, candidates))
    }
  }

}
