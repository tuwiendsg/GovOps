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
package at.ac.tuwien.infosys;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import at.ac.tuwien.infosys.model.Component;
import at.ac.tuwien.infosys.model.Dependency;
import at.ac.tuwien.infosys.model.Version;
import at.ac.tuwien.infosys.repository.IComponentRepository;
import at.ac.tuwien.infosys.repository.NotFoundException;

@RestController
@RequestMapping("/component-repository")
public class ComponentRepository {

	@Autowired
	private IComponentRepository componentRepository;

	@RequestMapping(value = "/component/{componentName}/{version}", method = RequestMethod.GET)
	public ResponseEntity<Component> getComponent(
			@PathVariable String componentName, @PathVariable String version) {

		System.out.println(componentName + " " + version);
		
		Component component = null;

		try {
			component = componentRepository.get(componentName, new Version(
					version));
		} catch (NotFoundException e) {
			e.printStackTrace();
			return new ResponseEntity<Component>(HttpStatus.BAD_REQUEST);
		}

		if (component != null)
			return new ResponseEntity<Component>(component, HttpStatus.OK);

		return new ResponseEntity<Component>(HttpStatus.BAD_REQUEST);
	}

	@RequestMapping(value = "/dependencies/{componentName}/{version}", method = RequestMethod.GET)
	public ResponseEntity<List<Dependency>> getDependency(@PathVariable String componentName,
			@PathVariable String version) {

		List<Dependency> dependencies = null;
		try {
			dependencies = componentRepository.getDependencies(new Component(
					componentName, new Version(version), null, null));
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (dependencies != null)
			return new ResponseEntity<List<Dependency>>(dependencies,
					HttpStatus.OK);

		return new ResponseEntity<List<Dependency>>(HttpStatus.BAD_REQUEST);
	}
}
