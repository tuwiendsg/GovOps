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
package at.ac.tuwien.infosys.store;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import at.ac.tuwien.infosys.Application;
import at.ac.tuwien.infosys.model.Device;
import at.ac.tuwien.infosys.model.DeviceUpdate;
import at.ac.tuwien.infosys.model.repository.DeviceRepository;
import at.ac.tuwien.infosys.model.repository.DeviceUpdateRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class DeviceStoreTest {

	@Autowired
	private IDeviceStore deviceStore;
	
	@Autowired
	private DeviceRepository deviceRepository;
	
	@Autowired
	private DeviceUpdateRepository deviceUpdateRepository;
	
	@Before
	public void setUp() {
		deviceRepository.deleteAll();
		deviceUpdateRepository.deleteAll();
	}
	
	@Test
	@Transactional
	public void test_addUpdate(){
		List<String> deviceIds = new ArrayList<String>();
		deviceIds.add("ID1");
		deviceIds.add("ID2");

		Image image = new Image(deviceIds, "imageId", "storageFileName",
				"storagePath");

		deviceStore.addUpdate(image);
		
		List<DeviceUpdate> updates = deviceRepository.getOne("ID1").getUpdates();
		assertNotNull(updates);
		assertThat(updates.size(), equalTo(1));
		assertThat(updates.get(0), equalTo(new DeviceUpdate(image)));
	}
	
	@Test
	@Transactional
	public void test_clean(){
		
		String id1 = "ID1";
		String id2 = "ID2";
		
		Device device1 = new Device(id1, "name1");
		Device device2 = new Device(id2, "name2");
		
		deviceRepository.save(device1);
		deviceRepository.save(device2);
		
		List<String> deviceIds = new ArrayList<String>();
		deviceIds.add(id1);
		deviceIds.add(id2);

		Image image = new Image(deviceIds, "imageId", "storageFileName",
				"storagePath");

		deviceStore.addUpdate(image);
		
		List<DeviceUpdate> updates = deviceRepository.getOne("ID1").getUpdates();
		assertNotNull(updates);
		assertThat(updates.size(), equalTo(1));
		assertThat(updates.get(0), equalTo(new DeviceUpdate(image)));
		
		deviceStore.clean();
		
		long size = deviceRepository.count();
		assertThat(size, equalTo(0l));
	}

}
