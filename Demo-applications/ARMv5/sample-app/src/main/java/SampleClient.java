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
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.UUID;

public class SampleClient {

	public static final double RANGE_MIN = 10.0;
	public static final double RANGE_MAX = 30.0;

	public static int NUMBER_OF_PUSHES = 50;

	public static void main(String[] args) {

		if (args.length < 3 || args.length > 4) {
			System.out
					.println("Usage: java SampleClient <server-url> <client-name> <push-period in ms> [<number of pushes>]");
			System.exit(1);
		}

		try {

			// e.g. http://0.0.0.0:8080/DSGSampleServer/client/
			String url = args[0];
			// e.g. sampleClient
			String clientName = args[1];
			// e.g. 5000
			long pushPeriod = Long.valueOf(args[2]);

			System.out.println("SampleClient: " + clientName
					+ " is up and starting to push readings... ");

			if (args.length == 4)
				NUMBER_OF_PUSHES = Integer.valueOf(args[3]);

			while (NUMBER_OF_PUSHES > 0) {
				URL serverUrl = new URL(url + clientName);
				HttpURLConnection conn = (HttpURLConnection) serverUrl
						.openConnection();
				conn.setDoOutput(true);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/json");

				String id = UUID.randomUUID().toString();
				Random r = new Random();
				double randomTemperature = RANGE_MIN + (RANGE_MAX - RANGE_MIN)
						* r.nextDouble();

				String input = "{\"id\":\"" + id + "\",\"temperature\":"
						+ randomTemperature + "}";

				OutputStream os = conn.getOutputStream();
				os.write(input.getBytes());
				os.flush();

				if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
					throw new RuntimeException("Failed : HTTP error code : "
							+ conn.getResponseCode());
				}

				conn.disconnect();

				Thread.sleep(pushPeriod);

				NUMBER_OF_PUSHES--;
			}

			System.out.println("SampleClient: " + clientName
					+ " shutting down... ");

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

}
