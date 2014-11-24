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
package at.ac.tuwien.infosys.repository;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import at.ac.tuwien.infosys.Application;
import at.ac.tuwien.infosys.model.Component;
import at.ac.tuwien.infosys.model.Dependency;
import at.ac.tuwien.infosys.model.Resource;
import at.ac.tuwien.infosys.model.Version;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public class LocalComponentRepositoryTest {

	private final String unknownComponent = "ruby";

	private final String knownComponent = "sample-app";
	private final Version unknownVersion = new Version("1", "0", "0");
	private final Version knownVersion = new Version("0", "0", "1");

	private Component expectedComponent;

	@Autowired
	private LocalComponentRepository componentRepository;

	@Rule
	public TemporaryFolder tempRepository = new TemporaryFolder();

	private File repo;

	@Before
	public void setUp() throws Exception {
		repo = tempRepository.newFolder();
		FileUtils.copyDirectory(new File("component-repository"), repo);

		// use mocked repository-location in repository class
		componentRepository.setRepository(repo.toPath());

		String appURL = "file:" + repo.getAbsolutePath() + "/sample-app/0.0.1";

		List<Resource> appBinaries = new ArrayList<Resource>();
		appBinaries.add(new Resource("SampleClient.class", new URL(appURL
				+ "/artifacts/SampleClient.class")));

		List<Resource> appScripts = new ArrayList<Resource>();
		appScripts.add(new Resource("install.sh", new URL(appURL
				+ "/scripts/install.sh")));
		appScripts.add(new Resource("restart.sh", new URL(appURL
				+ "/scripts/restart.sh")));
		appScripts.add(new Resource("run.sh", new URL(appURL
				+ "/scripts/run.sh")));
		appScripts.add(new Resource("stop.sh", new URL(appURL
				+ "/scripts/stop.sh")));
		appScripts.add(new Resource("uninstall.sh", new URL(appURL
				+ "/scripts/uninstall.sh")));

		expectedComponent = new Component("sample-app", new Version("0", "0",
				"1"), appBinaries, appScripts);
	}

	@Test(expected = NotFoundException.class)
	public void test_get_unkownComponent() throws NotFoundException {
		componentRepository.get(unknownComponent, new Version());
	}

	@Test(expected = NotFoundException.class)
	public void test_get_unkownVersion() throws NotFoundException {
		componentRepository.get(knownComponent, unknownVersion);
	}

	@Test
	public void test_get() throws NotFoundException {
		Component result = componentRepository
				.get(knownComponent, knownVersion);
		System.out.println(result);
		assertThat(result, equalTo(expectedComponent));
	}

	@Test
	public void test_getDependencies_noDependencies() throws NotFoundException {
		List<Dependency> dependencies = componentRepository
				.getDependencies(new Component("compact-jvm", new Version("1",
						"8", null), null, null));
		assertNotNull(dependencies);
		assertThat(dependencies.size(), equalTo(0));
	}

	@Test
	public void test_getDependencies() throws NotFoundException {
		List<Dependency> dependencies = componentRepository
				.getDependencies(new Component(knownComponent, knownVersion,
						null, null));
		assertNotNull(dependencies);
		assertThat(dependencies.size(), equalTo(1));
		
		Dependency expectedDependency = new Dependency("compact-jvm", new Version("1",
				"8", null));
		
		assertThat(dependencies.get(0), equalTo(expectedDependency));
	}
}
