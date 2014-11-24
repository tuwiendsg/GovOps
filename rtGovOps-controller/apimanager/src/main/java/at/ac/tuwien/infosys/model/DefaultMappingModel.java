package at.ac.tuwien.infosys.model;

public class DefaultMappingModel {

	private final String prefix = "http://";
	private final String deviceMapper = "/cgi-bin/mapper";
	private String capabilityId = "";
	private String deviceId = "";
	private String method="";
	private String [] arguments =null;
	
	public DefaultMappingModel(String capabilityId, String deviceId, String method, String arguments){
		
		this.capabilityId = capabilityId;
		this.deviceId = deviceId;
		this.method = method;
		if (arguments!=null){
			this.arguments = arguments.split(",");
		}
	}
	
	public String getMapping(){
		
		String baseUrl = this.prefix+getDeviceIP(this.deviceId)+this.deviceMapper+"/"+this.capabilityId+"/"+this.method;
		String arguments="";
		if(this.arguments!=null){
			arguments = this.encodeArguments();
		}
		return baseUrl+arguments;
	}

	private String encodeArguments() {
		String prefix = "/arguments?";
		String separator = "&";
		String argBase = "arg";
		
		String encoded = "";
		for (int i = 0; i < arguments.length; i++) {
			encoded+=argBase+(i+1)+"="+this.arguments[i]+separator;
		}
		encoded = encoded.substring(0,encoded.length()-1);
		return prefix+encoded;
	}
	
	private String getDeviceIP(String deviceId){
		return deviceId.replace("_", ".");
	}
	
	@Override
	public String toString() {
		return this.getMapping();
	}
	
	
	
}
