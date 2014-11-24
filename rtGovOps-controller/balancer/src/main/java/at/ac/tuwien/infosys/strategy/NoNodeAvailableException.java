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
package at.ac.tuwien.infosys.strategy;

public class NoNodeAvailableException extends Exception {

	private static final long serialVersionUID = 5829429328519995301L;

	public NoNodeAvailableException() {
		super();
	}

	public NoNodeAvailableException(String message) {
		super(message);
	}

	public NoNodeAvailableException(Throwable cause) {
		super(cause);
	}

	public NoNodeAvailableException(String message, Throwable cause) {
		super(message, cause);
	}

}
