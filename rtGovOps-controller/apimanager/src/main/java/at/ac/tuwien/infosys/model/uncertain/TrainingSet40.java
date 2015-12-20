package at.ac.tuwien.infosys.model.uncertain;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.infosys.store.model.DeviceDTO.Decison.DECISION;

public class TrainingSet40 {

	public static List<Device> devices = new ArrayList<>();
	
	static{
		
		Device d1 = new Device("D1");
		d1.addMeta("location", "location1");
		d1.addMeta("owner", "stefan");
		d1.addMeta("type", "");
		d1.setDecision(DECISION.GOOD);

		Device d2 = new Device("D2");
		d2.addMeta("location", "location1");
		d2.addMeta("owner", "");
		d2.addMeta("type", "");
		d2.setDecision(DECISION.GOOD);

		Device d3 = new Device("D3");
		d3.addMeta("location", "");//x
		d3.addMeta("owner", "stefan");
		d3.addMeta("type", "two");
		d3.setDecision(DECISION.GOOD);

		Device d4 = new Device("D4");
		d4.addMeta("location", "location2");
		d4.addMeta("owner", "");
		d4.addMeta("type", "one");
		d4.setDecision(DECISION.BAD);

		Device d5 = new Device("D5");
		d5.addMeta("location", "");
		d5.addMeta("owner", "other");
		d5.addMeta("type", "");
		d5.setDecision(DECISION.BAD);

		Device d6 = new Device("D6");
		d6.addMeta("location", "");
		d6.addMeta("owner", "other");
		d6.addMeta("type", "two");
		d6.setDecision(DECISION.BAD);

		Device d7 = new Device("D7");
		d7.addMeta("location", "location2");
		d7.addMeta("owner", "");
		d7.addMeta("type", "");
		d7.setDecision(DECISION.BAD);
		
		Device d8 = new Device("D8");
		d8.addMeta("location", "");
		d8.addMeta("owner", "");
		d8.addMeta("type", "two");
		d8.setDecision(DECISION.GOOD);
		
		Device d9 = new Device("D9");
		d9.addMeta("location", "location1");
		d9.addMeta("owner", "stefan");
		d9.addMeta("type", "");
		d9.setDecision(DECISION.GOOD);
		
		Device d10 = new Device("D10");
		d10.addMeta("location", "");
		d10.addMeta("owner", "stefan");
		d10.addMeta("type", "two");
		d10.setDecision(DECISION.GOOD);

		//List<Device> devices = new ArrayList<Device>();
		devices.add(d1);
		devices.add(d2);
		devices.add(d3);
		devices.add(d4);
		devices.add(d5);
		devices.add(d6);
		devices.add(d7);
		devices.add(d8);
		devices.add(d9);
		devices.add(d10);
	}
}
