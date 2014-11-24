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

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import at.ac.tuwien.infosys.model.Component;
import at.ac.tuwien.infosys.model.Dependency;
import at.ac.tuwien.infosys.model.Resource;
import at.ac.tuwien.infosys.model.Version;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@org.springframework.stereotype.Component
public class LocalComponentRepository implements IComponentRepository {

	@Value("${component.repository.script.folder}")
	private String scriptFolder;

	@Value("${component.repository.artifact.folder}")
	private String artifactFolder;

	@Value("${component.repository.dependency.file}")
	private String dependencyFile;

	@Value("${component.repository}")
	private String repositoryPath;

	private Path repository;

	@Autowired
	private ObjectMapper objectMapper;

	public LocalComponentRepository() {
		super();
	}

	@PostConstruct
	public void init() {
		this.repository = Paths.get(repositoryPath);
	}

	@Override
	public Component get(String name, Version version) throws NotFoundException {
		Path versionedComponentPath = resolve(name, version);

		Path artifactsPath = versionedComponentPath.resolve(artifactFolder);
		Path scriptsPath = versionedComponentPath.resolve(scriptFolder);

		// get available artifacts
		List<Path> foundArtifacts = list(artifactsPath);

		// get available scripts
		List<Path> foundScripts = list(scriptsPath);

		return new Component(name, version, createResources(foundArtifacts),
				createResources(foundScripts));
	}

	@Override
	public List<Dependency> getDependencies(Component component)
			throws NotFoundException {

		Path versionedComponentPath = resolve(component.getName(),
				component.getVersion());

		Path dependencyFilePath = versionedComponentPath
				.resolve(dependencyFile);

		Dependency[] dependencies = new Dependency[0];
		try {
			dependencies = objectMapper.readValue(
					Files.newInputStream(dependencyFilePath),
					Dependency[].class);
		} catch (JsonParseException e) {
			throw new NotFoundException(
					"Could not parse found dependency information!", e);
		} catch (JsonMappingException e) {
			throw new NotFoundException(
					"Could not map found dependency information!", e);
		} catch (IOException e) {
			// throw new NotFoundException(
			// "Could not find dependency information!", e);
		}

		return Arrays.asList(dependencies);
	}

	protected Path resolve(String componentName, Version version)
			throws NotFoundException {
		Path componentPath = findFolder(repository, componentName);

		if (componentPath == null)
			throw new NotFoundException("Component with name: " + componentName
					+ " is not available!");

		// resolve version
		Path versionedComponentPath = findFolder(componentPath, version.print());

		if (versionedComponentPath == null)
			throw new NotFoundException("Given version: " + version
					+ " for component: " + componentName + " is not available!");

		return versionedComponentPath;
	}

	protected List<Resource> createResources(List<Path> resources) {
		List<Resource> result = new ArrayList<Resource>();

		for (Path path : resources) {
			Resource resource = new Resource();
			resource.setName(path.getFileName().toString());
			try {
				resource.setUri(path.toUri().toURL());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			result.add(resource);
		}
		return result;
	}

	protected List<Path> list(Path path) {
		List<Path> results = new ArrayList<Path>();

		DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
			@Override
			public boolean accept(Path entry) throws IOException {
				return (!Files.isHidden(entry) && Files.isRegularFile(entry));
			}
		};

		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(
				path, filter)) {
			for (Path p : directoryStream) {
				results.add(p);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return results;
	}

	protected Path findFolder(Path searchPath, String searchName) {
		Path foundPath = null;
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(
				searchPath, searchName)) {
			for (Path path : directoryStream) {
				foundPath = path;
				break;
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return foundPath;
	}

	public Path getRepository() {
		return repository;
	}

	public void setRepository(Path repository) {
		this.repository = repository;
	}

}
