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

import scala.collection.immutable.Vector

import models.Version.Check.??

trait Repository {

  def devices: Device.Repository

  def artifacts: Artifact.Repository

  def releases: Release.Repository

  def plans: Plan.Repository

  def dependencies: Dependency.Repository
}

object Repository {

  trait Demo extends Repository {

    override val devices: Device.Repository = new Device.Repository {

      override val all = Vector(
        Device(name = "G2021", mac = "00:60:0C:81:71:F3", profile = "")
      )
    }

    override val artifacts: Artifact.Repository = new Artifact.Repository {

      override val all = Vector(SedonaVM.artifact, G2021Modbus.artifact, CompactJVM.artifact, SampleApp.artifact)

      override def findByName(name: String) = all.find(_.name == name)
    }

    override val releases: Release.Repository = new Release.Repository {

      private val all = Vector(SedonaVM.version1_2_28, G2021Modbus.version1, CompactJVM.version1_8_0, SampleApp.version0_0_1)

      override def of(artifact: Artifact) = all.filter(_.artifact == artifact)
    }

    override implicit val plans: Plan.Repository = new Plan.Repository {
      override val releases = Demo.this.releases
    }

    override implicit val dependencies: Dependency.Repository = new Dependency.Repository {
      override val plans = Demo.this.plans
    }

    // Artifacts

    private object SedonaVM {

      private val dir = "file:/tmp/component-repository/sedona-vm-1.2.28/"

      val artifact = Artifact.Component(name = "sedona-vm")

      val version1_2_28 = Release(
        artifact = artifact,
        version = Version(major = 1, minor = 2, fix = 28),
        binaries = Vector(
          Binary(name = "svm", uri = new URI(dir + "/artifacts/svm"))
        ),
        dependencies = Vector.empty,
        scripts = Vector(
          Script(name = "install.sh", uri = new URI(dir + "scripts/install.sh")),
          Script(name = "run.sh", uri = new URI(dir + "scripts/run.sh")),
          Script(name = "stop.sh", uri = new URI(dir + "scripts/stop.sh")),
          Script(name = "restart.sh", uri = new URI(dir + "scripts/restart.sh")),
          Script(name = "uninstall.sh", uri = new URI(dir + "scripts/uninstall.sh"))
        )
      )
    }

    private object G2021Modbus {

      private val dir = "file:/tmp/component-repository/g2021-modbus-1.0.0/"

      val artifact = Artifact.Component(name = "g2021-modbus")

      val version1 = Release(
        artifact = artifact,
        version = Version(major = 1),
        binaries = Vector(
          Binary(name = "G2021Modbus.sab", uri = new URI(dir + "artifacts/G2021Modbus.sab")),
          Binary(name = "G2021Modbus.sax", uri = new URI(dir + "artifacts/G2021Modbus.sax")),
          Binary(name = "Kits.scode", uri = new URI(dir + "artifacts/Kits.scode")),
          Binary(name = "Kits.xml", uri = new URI(dir + "artifacts/Kits.xml"))
        ),
        dependencies = Vector(
          Dependency.Execution(SedonaVM.artifact, check = ?? >= Version(1, 2, 28))
        ),
        scripts = Vector(
          Script(name = "install.sh", uri = new URI(dir + "scripts/install.sh")),
          Script(name = "run.sh", uri = new URI(dir + "scripts/run.sh")),
          Script(name = "stop.sh", uri = new URI(dir + "scripts/stop.sh")),
          Script(name = "restart.sh", uri = new URI(dir + "scripts/restart.sh")),
          Script(name = "uninstall.sh", uri = new URI(dir + "scripts/uninstall.sh"))
        )
      )
    }

	private object CompactJVM {

      private val dir = "file:/tmp/component-repository/compact-jvm-1.8/"

      val artifact = Artifact.Component(name = "compact-jvm")

      val version1_8_0 = Release(
        artifact = artifact,
        version = Version(major = 1, minor = 8, fix = 0),
        binaries = Vector(
          Binary(name = "compact-jvm.zip", uri = new URI(dir + "artifacts/compact-jvm.zip"))
        ),
        dependencies = Vector.empty,
        scripts = Vector(
          Script(name = "install.sh", uri = new URI(dir + "scripts/install.sh")),
          Script(name = "run.sh", uri = new URI(dir + "scripts/run.sh")),
          Script(name = "stop.sh", uri = new URI(dir + "scripts/stop.sh")),
          Script(name = "restart.sh", uri = new URI(dir + "scripts/restart.sh")),
          Script(name = "uninstall.sh", uri = new URI(dir + "scripts/uninstall.sh"))
        )
      )
    }

	private object SampleApp {

      private val dir = "file:/tmp/component-repository/sample-app-0.0.1/"

      val artifact = Artifact.Component(name = "sample-app")

      val version0_0_1 = Release(
        artifact = artifact,
        version = Version(major = 0, minor = 0, fix = 1),
        binaries = Vector(
          Binary(name = "SampleClient.class", uri = new URI(dir + "artifacts/SampleClient.class"))
        ),
        dependencies = Vector(
          Dependency.Execution(CompactJVM.artifact, check = ?? >= Version(1, 8, 0))
        ),
        scripts = Vector(
          Script(name = "install.sh", uri = new URI(dir + "scripts/install.sh")),
          Script(name = "run.sh", uri = new URI(dir + "scripts/run.sh")),
          Script(name = "stop.sh", uri = new URI(dir + "scripts/stop.sh")),
          Script(name = "restart.sh", uri = new URI(dir + "scripts/restart.sh")),
          Script(name = "uninstall.sh", uri = new URI(dir + "scripts/uninstall.sh"))
        )
      )
    }

  }

}
