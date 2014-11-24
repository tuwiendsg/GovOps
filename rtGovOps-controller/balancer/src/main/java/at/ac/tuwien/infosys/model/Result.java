package at.ac.tuwien.infosys.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(value="result")
public class Result {

	private String id;
	private String capaName;
	private String capaResult;
	
	public Result(){
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCapaName() {
		return capaName;
	}

	public void setCapaName(String capaName) {
		this.capaName = capaName;
	}

	public String getCapaResult() {
		return capaResult;
	}

	public void setCapaResult(String capaResult) {
		this.capaResult = capaResult;
	}


	@Override
	public String toString() {
		return "[ id: "+this.id+", capaName: "+this.capaName+ ", capaResult: "+this.capaResult+"]";
	}
}
