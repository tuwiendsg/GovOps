package at.ac.tuwien.infosys.model;

public class Capability {

	private String id;
	private String mappingModel;
	
	public Capability (String id, String mappingModel){
		
		this.id = id;
		this.mappingModel = mappingModel;
	}
	
	public String getId() {
		return id;
	}
	public String getMappingModel() {
		return mappingModel;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Capability other = (Capability) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	
}
