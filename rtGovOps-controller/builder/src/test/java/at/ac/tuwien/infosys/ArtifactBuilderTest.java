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
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import at.ac.tuwien.infosys.model.Component;
import at.ac.tuwien.infosys.model.Plan;
import at.ac.tuwien.infosys.store.ImageStorage;
import at.ac.tuwien.infosys.store.model.DeviceUpdateRequest;
import at.ac.tuwien.infosys.util.Config;
import at.ac.tuwien.infosys.util.ImageUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class ArtifactBuilderTest {

	private final String deviceId = "ID1";
	private List<String> deviceIds;
	private final String component = "comp1";
	private final String version = "alpha";
	private final String imageId = "imageID1";
	private final String idPrefix = component + "_" + version;

	private MockMvc mockMvc;

	private final Path mockedPath = Paths.get("/tmp/mockedPath");

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	@InjectMocks
	private IArtifactBuilder builder;

	@Mock
	private ImageStorage imageStorage;

	@Mock
	private RestTemplate restTemplate;

	@Mock
	private ImageUtil imageUtil;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Value("${dms.url}")
	private String dmsURL;

	@Value("${manager.url}")
	private String managerURL;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		reset(imageStorage);
		reset(restTemplate);
		reset(imageUtil);
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
				.build();

		try {
			Files.createDirectory(mockedPath);
		} catch (IOException e) {
			e.printStackTrace();
		}

		deviceIds = new ArrayList<String>();
		deviceIds.add(deviceId);
	}

	@After
	public void tearDown() {
		try {
			Files.delete(mockedPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test_build_nullRequest() throws Exception {
		DeviceUpdateRequest updateRequest = null;
		mockMvc.perform(
				post("/artifact-builder/build").content(
						objectMapper.writeValueAsString(updateRequest))
						.contentType(MediaType.APPLICATION_JSON)).andExpect(
				status().isBadRequest());

		verifyZeroInteractions(restTemplate, imageUtil, imageStorage);
	}

	@Test
	public void test_build_dmsNotAvailable() throws Exception {
		DeviceUpdateRequest updateRequest = new DeviceUpdateRequest(deviceIds,
				component, version);

		when(
				restTemplate.getForEntity(dmsURL, Component[].class,
						updateRequest.getComponent(),
						updateRequest.getVersion())).thenReturn(
				new ResponseEntity<Component[]>(HttpStatus.BAD_REQUEST));

		mockMvc.perform(
				post("/artifact-builder/build").content(
						objectMapper.writeValueAsString(updateRequest))
						.contentType(MediaType.APPLICATION_JSON)).andExpect(
				status().isInternalServerError());

		verify(restTemplate).getForEntity(dmsURL, Component[].class,
				updateRequest.getComponent(), updateRequest.getVersion());
		verifyZeroInteractions(imageUtil, imageStorage);
	}

	@Test
	public void test_build_dmsAvailable_imageUtilThrowsException()
			throws Exception {
		DeviceUpdateRequest updateRequest = new DeviceUpdateRequest(deviceIds,
				component, version);
		Plan mockedPlan = Config.DEVICE_PLAN;
		Component[] mockedComponents = mockedPlan.getComponents().toArray(
				new Component[mockedPlan.getComponents().size()]);

		when(
				restTemplate.getForEntity(dmsURL, Component[].class,
						updateRequest.getComponent(),
						updateRequest.getVersion()))
				.thenReturn(
						new ResponseEntity<Component[]>(mockedComponents,
								HttpStatus.OK));

		when(imageStorage.getUpdate(idPrefix)).thenReturn(null);

		when(imageUtil.createImage(mockedPlan, idPrefix)).thenThrow(
				new IOException());

		mockMvc.perform(
				post("/artifact-builder/build").content(
						objectMapper.writeValueAsString(updateRequest))
						.contentType(MediaType.APPLICATION_JSON)).andExpect(
				status().isInternalServerError());

		verify(restTemplate).getForEntity(dmsURL, Component[].class,
				updateRequest.getComponent(), updateRequest.getVersion());
		verify(imageStorage).getUpdate(idPrefix);
		verify(imageUtil).createImage(mockedPlan, idPrefix);
		verifyNoMoreInteractions(imageStorage);
	}

	@Test
	public void test_build() throws Exception {
		DeviceUpdateRequest updateRequest = new DeviceUpdateRequest(deviceIds,
				component, version);
		Plan mockedPlan = Config.DEVICE_PLAN;
		Component[] mockedComponents = mockedPlan.getComponents().toArray(
				new Component[mockedPlan.getComponents().size()]);

		when(
				restTemplate.getForEntity(dmsURL, Component[].class,
						updateRequest.getComponent(),
						updateRequest.getVersion()))
				.thenReturn(
						new ResponseEntity<Component[]>(mockedComponents,
								HttpStatus.OK));

		Path mockedPath = Paths.get("/tmp/mockedPath");
		
		when(imageStorage.getUpdate(idPrefix)).thenReturn(null);
		
		when(imageUtil.createImage(mockedPlan, idPrefix)).thenReturn(mockedPath);
		when(imageUtil.getImageId()).thenReturn(imageId);

		when(restTemplate.postForEntity(managerURL, null, String.class))
				.thenReturn(new ResponseEntity<String>(HttpStatus.OK));

		mockMvc.perform(
				post("/artifact-builder/build").content(
						objectMapper.writeValueAsString(updateRequest))
						.contentType(MediaType.APPLICATION_JSON)).andExpect(
				status().isAccepted());

		verify(restTemplate).getForEntity(dmsURL, Component[].class,
				updateRequest.getComponent(), updateRequest.getVersion());
		verify(imageUtil).createImage(mockedPlan, idPrefix);
		verify(imageUtil).getImageId();
		verify(restTemplate).postForEntity(managerURL, null, String.class);
	}
}
