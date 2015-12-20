package at.ac.tuwien.infosys.model.uncertain;

import java.util.HashMap;
import java.util.Map;

import at.ac.tuwien.infosys.store.model.DeviceDTO.Decison.DECISION;

public class Device {
	
	private DECISION decision;
	private String id;
	private HashMap<String, String> meta = new HashMap<>();

	public Device(String id) {
		this.id = id;
	}
	
	public void addMeta(String key, String value){
		meta.put(key, value);
	}
	public Map<String, String> getMeta(){
		return this.meta;
	}
	
	public String getId() {
		return id;
	}

	public DECISION getDecision() {
		return decision;
	}
	public void setDecision(DECISION decision) {
		this.decision = decision;
	}
	
	public String toString() {
		//return "Device:[id: " + this.id +" >> metaInfo: "+this.meta +"]";
		return "Device:["+ this.id +"]";
	}
	
}
