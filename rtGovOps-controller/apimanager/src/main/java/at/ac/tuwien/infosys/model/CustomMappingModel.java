package at.ac.tuwien.infosys.model;

import java.io.File;

public abstract class CustomMappingModel extends DefaultMappingModel {

	private File rawModel;
	
	public CustomMappingModel(String capabilityId, String deviceId,
			String method, String arguments) {
		super(capabilityId, deviceId, method, arguments);
	}

	@Override
	public abstract String getMapping();
	
	public File getRawModel(){
		return this.rawModel;
	}
	
}
