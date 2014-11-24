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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;

@Component
@Scope(value = "singleton")
@Lazy
public class ImageStorage implements Closeable {

	private static final Logger LOGGER = Logger.getLogger(ImageStorage.class);
	private Sardine sardine;

	@Value("${file.repository.url}")
	private String repo_url;

	@Value("${file.repository.user}")
	private String repo_user;

	@Value("${file.repository.pw}")
	private String repo_pw;

	public ImageStorage() {
	}

	@PostConstruct
	public void init() {
		sardine = SardineFactory.begin(repo_user, repo_pw);
	}

	public InputStream getUpdate(Image image) throws IOException {
		String path = repo_url + image.getStoragePath();
		return sardine.get(path);
	}

	public Image getUpdate(String search) throws IOException {
		List<DavResource> list = sardine.list(repo_url);

		DavResource match = null;

		for (DavResource resource : list) {
			if (resource.getName().contains(search)) {
				System.out.println(resource.getName() + " "
						+ resource.getPath());
				match = resource;
				break;
			}
		}

		if (match != null) {
			String id = match.getName();
			return new Image(null, id, id + ".zip", id + "/" + id + ".zip");
		}

		return null;
	}

	public int cleanStorage() throws IOException {
		List<DavResource> list = sardine.list(repo_url);
		int deleted = 0;
		for (DavResource resource : list) {
			try {
				String url = repo_url + resource.getName() + "/";
				System.out.println(url);
				if(sardine.exists(url)) {
					sardine.delete(url);
					deleted++;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return deleted;
	}

	public Image storeUpdate(List<String> deviceIds, String imageId,
			InputStream inputStream) throws IOException {

		/*
		 * Assumption: Each image has a separate directory in the repo. Where
		 * each directory name corresponds to the id of the image.
		 */

		// check if device already has a directory
		String deviceDir = repo_url + imageId;
		if (!sardine.exists(deviceDir)) {
			LOGGER.info("Directory does not exist! Create " + deviceDir);
			sardine.createDirectory(deviceDir);
		}

		// create relative path to store update
		String fileName = imageId + ".zip";
		String path = imageId + "/" + fileName;

		// upload file
		sardine.put(repo_url + path, inputStream);

		return new Image(deviceIds, imageId, fileName, path);
	}

	public void removeUpdate(Image deviceUpdate) throws IOException {
		String path = repo_url + deviceUpdate.getStoragePath();
		sardine.delete(path);
	}

	@Override
	public void close() throws IOException {
		sardine.shutdown();
	}

	public String getRepo_url() {
		return repo_url;
	}

	public void setRepo_url(String repo_url) {
		this.repo_url = repo_url;
	}

	public String getRepo_user() {
		return repo_user;
	}

	public void setRepo_user(String repo_user) {
		this.repo_user = repo_user;
	}

	public String getRepo_pw() {
		return repo_pw;
	}

	public void setRepo_pw(String repo_pw) {
		this.repo_pw = repo_pw;
	}

}
