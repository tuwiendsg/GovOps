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

import scala.math.Ordered._

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.mvc.PathBindable

case class Version private(major: Version.Part, minor: Version.Part, fix: Option[Version.Part]) {

  require(major >= 0, "Major version part must be non-negative")
  require(minor >= 0, "Minor version part must be non-negative")
  require(fix.getOrElse(0) >= 0, "Fix version part must be non-negative")

  override lazy val toString = fix match {
    case Some(part) => s"$major.$minor.$part"
    case None => s"$major.$minor"
  }
}

object Version {

  type Part = Int

  def apply(major: Version.Part, minor: Version.Part = 0): Version =
    Version(major, minor, fix = None)

  def apply(major: Version.Part, minor: Version.Part, fix: Version.Part): Version =
    Version(major, minor, Some(fix))

  implicit val order: Ordering[Version] = new Ordering[Version] {
    override def compare(x: Version, y: Version): Int = {
      val major = x.major - y.major
      if (major == 0) {
        val minor = x.minor - y.minor
        if (minor == 0) x.fix match {
          case Some(xFix) => y.fix.fold(1)(xFix - _)
          case None => y.fix.fold(0)(_ => -1)
        } else minor
      } else major
    }
  }

  implicit val json: Writes[Version] =
    ((JsPath \ "major").write[Version.Part] and
     (JsPath \ "minor").write[Version.Part] and
     (JsPath \ "fix").writeNullable[Version.Part]
    )(unlift(Version.unapply))

  def parse(version: String): Option[Version] = {
    val first = version.indexOf('.')
    val second = version.lastIndexOf('.')

    try {
      Some(
        if (first < 0) {
          Version(Integer.parseInt(version))
        } else {
          val major = Integer.parseInt(version.substring(0, first))
          if (first == second) {
            val minor = Integer.parseInt(version.substring(first + 1))
            Version(major, minor)
          } else {
            val minor = Integer.parseInt(version.substring(first + 1, second))
            val fix = Integer.parseInt(version.substring(second + 1))
            Version(major, minor, fix)
          }
        }
      )
    } catch {
      case _: NumberFormatException => None
    }
  }

  implicit def pathBinder(implicit strings: PathBindable[String]) = new PathBindable[Version] {

    override def bind(key: String, value: String) = for {
      value <- strings.bind(key, value).right
      version <- parse(value).toRight(s"Wrong version format $value (expected: x[.y[.z]])").right
    } yield version

    override def unbind(key: String, version: Version) = strings.unbind(key, version.toString)
  }

  sealed trait Check extends Function[Version, Boolean] {

    def unary_! : Version.Check = {
      val check = this
      new Check {

        override def apply(version: Version) = !check(version)

        override lazy val toString = s"!($check)"
      }
    }

    def &&(check2: Version.Check): Version.Check = {
      val check1 = this
      new Version.Check {

        override def apply(version: Version) = check1(version) && check2(version)

        override lazy val toString = s"($check1) && ($check2)"
      }
    }

    def ||(check2: Version.Check): Version.Check = {
      val check1 = this
      new Version.Check {

        override def apply(version: Version) = check1(version) || check2(version)

        override lazy val toString = s"($check1) || ($check2)"
      }
    }
  }

  object Check {

    implicit val json: Writes[Version.Check] = Writes(check => JsString(check.toString()))

    object ?? {

      def <(version: Version) = check(s"< $version")(_ < version)

      def <=(version: Version) = check(s"<= $version")(_ <= version)

      def >(version: Version) = check(s"> $version")(_ > version)

      def >=(version: Version) = check(s">= $version")(_ >= version)

      def ==(version: Version) = check(s"== $version")(_ == version)

      def <>(version: Version) = check(s"<> $version")(_ != version)

      private def check(operator: String)(predicate: Version => Boolean): Version.Check =
        new Version.Check {

          override def apply(version: Version) = predicate(version)

          override lazy val toString = s"?? $operator"
        }
    }

  }

}
