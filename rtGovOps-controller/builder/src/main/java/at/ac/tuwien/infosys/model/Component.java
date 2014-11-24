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
package at.ac.tuwien.infosys.model;

import java.util.List;

public class Component {

	private String name;
	private Version version;
	private List<Resource> binaries;
	private List<Resource> scripts;

	public Component() {
		super();
	}

	public Component(String name, Version version, List<Resource> artifacts,
			List<Resource> scripts) {
		super();
		this.name = name;
		this.version = version;
		this.binaries = artifacts;
		this.scripts = scripts;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Version getVersion() {
		return version;
	}

	public void setVersion(Version version) {
		this.version = version;
	}

	public List<Resource> getBinaries() {
		return binaries;
	}

	public void setBinaries(List<Resource> binaries) {
		this.binaries = binaries;
	}

	public List<Resource> getScripts() {
		return scripts;
	}

	public void setScripts(List<Resource> scripts) {
		this.scripts = scripts;
	}

	@Override
	public String toString() {
		return "Component [name=" + name + ", version=" + version
				+ ", binaries=" + binaries + ", scripts=" + scripts + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((binaries == null) ? 0 : binaries.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((scripts == null) ? 0 : scripts.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Component other = (Component) obj;
		if (binaries == null) {
			if (other.binaries != null)
				return false;
		} else if (!binaries.equals(other.binaries))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (scripts == null) {
			if (other.scripts != null)
				return false;
		} else if (!scripts.equals(other.scripts))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

}
