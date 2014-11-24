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

public class Version {

	private String major;
	private String minor;
	private String fix;

	public Version(String major, String minor, String fix) {
		super();
		this.major = major;
		this.minor = minor;
		this.fix = fix;
	}

	public Version() {
		super();
	}

	public Version(String version) {
		super();

		if (version != null && !version.isEmpty()) {

			String[] versionParts;
			if (version.contains("."))
				versionParts = version.split("\\.");
			else
				versionParts = version.split("_");

			switch (versionParts.length) {
			case 0:
				break;

			case 1:
				this.major = versionParts[0];
				break;

			case 2:
				this.major = versionParts[0];
				this.minor = versionParts[1];
				break;

			case 3:
				this.major = versionParts[0];
				this.minor = versionParts[1];
				this.fix = versionParts[2];
				break;

			default:
				break;
			}
		}
	}

	public String getMajor() {
		return major;
	}

	public void setMajor(String major) {
		this.major = major;
	}

	public String getMinor() {
		return minor;
	}

	public void setMinor(String minor) {
		this.minor = minor;
	}

	public String getFix() {
		return fix;
	}

	public void setFix(String fix) {
		this.fix = fix;
	}

	public String print() {
		StringBuilder builder = new StringBuilder();

		builder.append(major + ".");
		builder.append(minor);
		if (fix != null)
			builder.append("." + fix);

		return builder.toString();
	}

	@Override
	public String toString() {
		return "Version [major=" + major + ", minor=" + minor + ", fix=" + fix
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fix == null) ? 0 : fix.hashCode());
		result = prime * result + ((major == null) ? 0 : major.hashCode());
		result = prime * result + ((minor == null) ? 0 : minor.hashCode());
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
		Version other = (Version) obj;
		if (fix == null) {
			if (other.fix != null)
				return false;
		} else if (!fix.equals(other.fix))
			return false;
		if (major == null) {
			if (other.major != null)
				return false;
		} else if (!major.equals(other.major))
			return false;
		if (minor == null) {
			if (other.minor != null)
				return false;
		} else if (!minor.equals(other.minor))
			return false;
		return true;
	}

}
