package at.ac.tuwien.infosys.store.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is just a placeholder for multiple device DTOs. Needed since Jakson in Spring <4.0 has a prob with erasure types. 
 * @author stefan
 * 
 *
 */
public class DevicesDTO {
	
	
	private List<DeviceDTO> devices = new ArrayList<DeviceDTO>();
	
	public DevicesDTO(){
	}

	
	public void setDevices(List<DeviceDTO> devices) {
		this.devices = devices;
	}
	
	public List<DeviceDTO> getDevices() {
		return devices;
	}
	
	public void addDTO(DeviceDTO dto){
		this.devices.add(dto);
	}
	
}
