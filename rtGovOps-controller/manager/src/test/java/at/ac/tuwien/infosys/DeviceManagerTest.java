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
package at.ac.tuwien.infosys;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.input.ReaderInputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import at.ac.tuwien.infosys.model.Profile;
import at.ac.tuwien.infosys.store.IDeviceStore;
import at.ac.tuwien.infosys.store.Image;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class DeviceManagerTest {

	private final String deviceName = "device1";
	private final String deviceId = "ID1";
	private final String deviceId2 = "ID2";
	private final String profile = "Profile for ID1";
	private final String imageId = "ImageID1";
	private final String storageName = imageId + ".zip";
	private final String storagePath = deviceId + "/" + storageName;

	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	@InjectMocks
	private DeviceManager deviceManager;

	@Mock
	private IDeviceStore mockedDeviceStore;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		reset(mockedDeviceStore);
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
				.build();
	}

	@Test
	public void test_addProfile() throws Exception {
		mockMvc.perform(
				post("/device-manager/profile/" + deviceId).content(profile)
						.contentType(MediaType.TEXT_PLAIN))
				.andExpect(status().isOk())
				.andExpect(content().string("Profile added for: " + deviceId));

		verify(mockedDeviceStore).addProfile(deviceId, profile);
	}

	@Test
	public void test_getProfile() throws Exception {
		Profile mockedProfile = new Profile(deviceName, deviceId, profile);

		when(mockedDeviceStore.hasDevice(deviceId)).thenReturn(true);
		when(mockedDeviceStore.getProfile(deviceId)).thenReturn(mockedProfile);

		mockMvc.perform(get("/device-manager/profile/" + deviceId))
				.andExpect(status().isOk())
				.andExpect(
						content().string(
								objectMapper.writeValueAsString(mockedProfile)));

		verify(mockedDeviceStore).hasDevice(deviceId);
		verify(mockedDeviceStore).getProfile(deviceId);
	}

	@Test
	public void test_getProfile_unknown() throws Exception {
		when(mockedDeviceStore.hasDevice(deviceId)).thenReturn(false);

		mockMvc.perform(get("/device-manager/profile/" + deviceId)).andExpect(
				status().isBadRequest());

		verify(mockedDeviceStore).hasDevice(deviceId);
	}

	@Test
	public void test_getUpdate_unknown() throws Exception {
		when(mockedDeviceStore.hasDevice(deviceId)).thenReturn(false);

		mockMvc.perform(get("/device-manager/update/" + deviceId))
				.andExpect(status().isOk())
				.andExpect(content().string("Nothing to update!"));

		verify(mockedDeviceStore).hasDevice(deviceId);
		verify(mockedDeviceStore).addDevice(deviceId);
	}

	@Test
	public void test_getUpdate_noUpdate() throws Exception {
		when(mockedDeviceStore.hasDevice(deviceId)).thenReturn(true);
		when(mockedDeviceStore.getUpdate(deviceId)).thenReturn(null);

		mockMvc.perform(get("/device-manager/update/" + deviceId))
				.andExpect(status().isOk())
				.andExpect(content().string("Nothing to update!"));

		verify(mockedDeviceStore).hasDevice(deviceId);
		verify(mockedDeviceStore).getUpdate(deviceId);
	}

	@Test
	public void test_getUpdate_updateAvailable() throws Exception {
		when(mockedDeviceStore.hasDevice(deviceId)).thenReturn(true);

		String mockedInput = "Update...";
		StringReader reader = new StringReader(mockedInput);
		InputStream mockedStream = new ReaderInputStream(reader);
		when(mockedDeviceStore.getUpdate(deviceId)).thenReturn(mockedStream);

		mockMvc.perform(get("/device-manager/update/" + deviceId))
				.andExpect(status().isOk())
				.andExpect(content().bytes(mockedInput.getBytes()));

		verify(mockedDeviceStore).hasDevice(deviceId);
		verify(mockedDeviceStore).getUpdate(deviceId);
	}

	@Test
	public void test_updateSuccessful_unknown() throws Exception {
		when(mockedDeviceStore.hasDevice(deviceId)).thenReturn(false);

		mockMvc.perform(
				get("/device-manager/update-successful/" + deviceId + "/"
						+ imageId)).andExpect(status().isBadRequest());

		verify(mockedDeviceStore).hasDevice(deviceId);
	}

	@Test
	public void test_updateSuccessful() throws Exception {
		when(mockedDeviceStore.hasDevice(deviceId)).thenReturn(true);

		mockMvc.perform(
				get("/device-manager/update-successful/" + deviceId + "/"
						+ imageId))
				.andExpect(status().isOk())
				.andExpect(
						content()
								.string("Update removed from Manager's queue!"));

		verify(mockedDeviceStore).hasDevice(deviceId);
		verify(mockedDeviceStore).removeUpdate(deviceId, imageId);
	}

	@Test
	public void test_update() throws Exception {
		when(mockedDeviceStore.hasDevice(deviceId)).thenReturn(true);

		List<String> deviceIds = new ArrayList<String>();
		deviceIds.add(deviceId);
		deviceIds.add(deviceId2);

		Image mockedImage = new Image(deviceIds, imageId, storageName,
				storagePath);

		mockMvc.perform(
				post("/device-manager/update/").contentType(
						MediaType.APPLICATION_JSON).content(
						objectMapper.writeValueAsString(mockedImage)))
				.andExpect(status().isOk())
				.andExpect(content().string("Successfully stored image!"));

		verify(mockedDeviceStore).addUpdate(mockedImage);
	}
}
