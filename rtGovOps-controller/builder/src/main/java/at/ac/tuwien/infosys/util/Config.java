/*
 * Copyright (c) 2014 Technische Universitaet Wien (TUW), Distributed SystemsGroup E184.
 * 
 * This work was partially supported by the Pacific Controls under the Pacific Controls 
 * Cloud Computing Lab (pc3l.infosys.tuwien.ac.at)
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
 * 
 * Written by Michael Voegler
 */
package at.ac.tuwien.infosys.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.infosys.model.Component;
import at.ac.tuwien.infosys.model.Plan;
import at.ac.tuwien.infosys.model.Resource;
import at.ac.tuwien.infosys.model.Version;

public class Config {

	public static final URL IMAGE_RUN_SCRIPT;

	public static final Plan DEVICE_PLAN;

	public static final String DEVICE_PLAN_JSON = "["
			+ "{\"name\":\"sedona-vm\",\"version\":{\"major\":1,\"minor\":2,\"fix\":28},"
			+ "\"binaries\":[{\"name\":\"svm\",\"uri\":\"file:/tmp/component-repository/sedona-vm/1.2.28//artifacts/svm\"}],"
			+ "\"scripts\":[{\"name\":\"install.sh\",\"uri\":\"file:/tmp/component-repository/sedona-vm/1.2.28/scripts/install.sh\"},"
			+ "{\"name\":\"run.sh\",\"uri\":\"file:/tmp/component-repository/sedona-vm/1.2.28/scripts/run.sh\"},"
			+ "{\"name\":\"stop.sh\",\"uri\":\"file:/tmp/component-repository/sedona-vm/1.2.28/scripts/stop.sh\"},"
			+ "{\"name\":\"restart.sh\",\"uri\":\"file:/tmp/component-repository/sedona-vm/1.2.28/scripts/restart.sh\"},"
			+ "{\"name\":\"uninstall.sh\",\"uri\":\"file:/tmp/component-repository/sedona-vm/1.2.28/scripts/uninstall.sh\"}]},"
			+ "{\"name\":\"g2021-modbus\",\"version\":{\"major\":1,\"minor\":0},"
			+ "\"binaries\":["
			+ "{\"name\":\"G2021Modbus.sab\",\"uri\":\"file:/tmp/component-repository/g2021-modbus/1.0.0/artifacts/G2021Modbus.sab\"},"
			+ "{\"name\":\"G2021Modbus.sax\",\"uri\":\"file:/tmp/component-repository/g2021-modbus/1.0.0/artifacts/G2021Modbus.sax\"},"
			+ "{\"name\":\"Kits.scode\",\"uri\":\"file:/tmp/component-repository/g2021-modbus/1.0.0/artifacts/Kits.scode\"},"
			+ "{\"name\":\"Kits.xml\",\"uri\":\"file:/tmp/component-repository/g2021-modbus/1.0.0/artifacts/Kits.xml\"}],"
			+ "\"scripts\":[{\"name\":\"install.sh\",\"uri\":\"file:/tmp/component-repository/g2021-modbus/1.0.0/scripts/install.sh\"},"
			+ "{\"name\":\"run.sh\",\"uri\":\"file:/tmp/component-repository/g2021-modbus/1.0.0/scripts/run.sh\"},"
			+ "{\"name\":\"stop.sh\",\"uri\":\"file:/tmp/component-repository/g2021-modbus/1.0.0/scripts/stop.sh\"},"
			+ "{\"name\":\"restart.sh\",\"uri\":\"file:/tmp/component-repository/g2021-modbus/1.0.0/scripts/restart.sh\"},"
			+ "{\"name\":\"uninstall.sh\",\"uri\":\"file:/tmp/component-repository/g2021-modbus/1.0.0/scripts/uninstall.sh\"}]}"
			+ "]";

	static {

		try {

			String baseURL = "file:/tmp/component-repository";

			IMAGE_RUN_SCRIPT = new URL(baseURL + "/run.sh");

			String svmURL = baseURL + "/sedona-vm/1.2.28";

			List<Resource> svmBinaries = new ArrayList<Resource>();
			svmBinaries.add(new Resource("svm", new URL(svmURL
					+ "/artifacts/svm")));

			List<Resource> svmScripts = new ArrayList<Resource>();
			svmScripts.add(new Resource("install.sh", new URL(svmURL
					+ "/scripts/install.sh")));
			svmScripts.add(new Resource("run.sh", new URL(svmURL
					+ "/scripts/run.sh")));
			svmScripts.add(new Resource("stop.sh", new URL(svmURL
					+ "/scripts/stop.sh")));
			svmScripts.add(new Resource("restart.sh", new URL(svmURL
					+ "/scripts/restart.sh")));
			svmScripts.add(new Resource("uninstall.sh", new URL(svmURL
					+ "/scripts/uninstall.sh")));

			Component svmComonent = new Component("sedona-vm",
					new Version("1", "2", "28"), svmBinaries, svmScripts);

			String g2021AppURL = baseURL + "/g2021-modbus/1.0.0";

			List<Resource> g2021AppBinaries = new ArrayList<Resource>();
			g2021AppBinaries.add(new Resource("G2021Modbus.sab", new URL(
					g2021AppURL + "/artifacts/G2021Modbus.sab")));
			g2021AppBinaries.add(new Resource("G2021Modbus.sax", new URL(
					g2021AppURL + "/artifacts/G2021Modbus.sax")));
			g2021AppBinaries.add(new Resource("Kits.scode", new URL(g2021AppURL
					+ "/artifacts/Kits.scode")));
			g2021AppBinaries.add(new Resource("Kits.xml", new URL(g2021AppURL
					+ "/artifacts/Kits.xml")));

			List<Resource> g2021AppScripts = new ArrayList<Resource>();
			g2021AppScripts.add(new Resource("install.sh", new URL(g2021AppURL
					+ "/scripts/install.sh")));
			g2021AppScripts.add(new Resource("run.sh", new URL(g2021AppURL
					+ "/scripts/run.sh")));
			g2021AppScripts.add(new Resource("stop.sh", new URL(g2021AppURL
					+ "/scripts/stop.sh")));
			g2021AppScripts.add(new Resource("restart.sh", new URL(g2021AppURL
					+ "/scripts/restart.sh")));
			g2021AppScripts.add(new Resource("uninstall.sh", new URL(
					g2021AppURL + "/scripts/uninstall.sh")));

			Component g2021AppComponent = new Component("g2021-modbus",
					new Version("1", "0", "0"), g2021AppBinaries,
					g2021AppScripts);

			DEVICE_PLAN = new Plan(svmComonent, g2021AppComponent);

		} catch (MalformedURLException e) {
			throw new RuntimeException("Could not init config", e);
		}
	}

}
