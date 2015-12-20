package at.ac.tuwien.infosys.store.model;

import java.util.HashMap;

import at.ac.tuwien.infosys.store.model.DeviceDTO.Decison.DECISION;

public class DeviceDTO {

	public String id;
	public String name;
	public String ipAddress;
	public String metaInfo;
	private HashMap<String, String> meta = new HashMap<>();
	private DECISION decision;

	public DeviceDTO() {

	}
	public DeviceDTO(String id) {
		this.id = id;
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
	public void addMeta(String key, String value) {
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
	public DECISION getDecision() {
		return decision;
	}
	public void setDecision(DECISION decision) {
		this.decision = decision;
	}
	
	public static class Decison {

		private final DeviceDTO device;
		// Default threshold
		private double threshold = 0.5;

		public enum DECISION {
			GOOD, BAD
		};

		public Decison(DeviceDTO device, double threshold) {
			this.device = device;
			this.threshold = threshold;
		}

		public DECISION getDecision() {
			if (calculteCompleteness() >= this.threshold)
				return DECISION.GOOD;
			else
				return DECISION.BAD;
		}

		private double calculteCompleteness() {
			int total = device.getMeta().keySet().size();
			int avail = total;
			for (String key : device.getMeta().keySet()) {
				if ("".equals(device.getMeta().get(key)))
					avail--;
			}
			return new Double(avail) / new Double(total);
		}

		public void setThreshold(double threshold) {
			this.threshold = threshold;
		}

		public double getThreshold() {
			return threshold;
		}
	}

}
