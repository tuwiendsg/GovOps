package at.ac.tuwien.infosys.store.model;

import java.util.HashMap;
import java.util.Set;

public class DeviceDTO {

	public String id;
	public String name;
	public String ipAddress;
	public String metaInfo;
	private HashMap<String, String> meta = new HashMap<>();

	public DeviceDTO() {

	}

	public DeviceDTO(String id, String name, String metaInfo) {
		this.id = id;
		this.name = name;
		this.ipAddress = "";
		this.metaInfo = metaInfo;
	}

	public void addMetaData(String key, String value) {
		this.meta.put(key, value);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String toString() {
		return "Device:[id: " + this.id + ", IPAddr: " + this.ipAddress +", metaInfo: "+this.meta +"]";
	}

	public HashMap<String, String> getMeta() {
		return meta;
	}

	public void setMeta(HashMap<String, String> meta) {
		this.meta = meta;
	}

}
