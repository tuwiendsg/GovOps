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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import at.ac.tuwien.infosys.model.Reading;

@RestController
@RequestMapping("/client")
public class ClientController {

	private final MessageSendingOperations<String> messagingTemplate;

	@Autowired
	public ClientController(
			final MessageSendingOperations<String> messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public String get(@PathVariable String id) {

		return "Received: " + id;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.POST)
	public String set(@PathVariable String id, @RequestBody Reading reading) {

		String result = "Received: " + id + " and " + reading;

		System.out.println(result);

		this.messagingTemplate.convertAndSend("/data",
				new Double(reading.getTemperature()).intValue());

		return result;
	}

}
