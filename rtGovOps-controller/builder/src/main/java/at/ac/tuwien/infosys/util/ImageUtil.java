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
package at.ac.tuwien.infosys.util;

import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;

import at.ac.tuwien.infosys.model.Component;
import at.ac.tuwien.infosys.model.Plan;
import at.ac.tuwien.infosys.model.Resource;

/**
 * @author michael
 *
 */
@org.springframework.stereotype.Component(value = "singleton")
public class ImageUtil {

	private static final String IMAGE_RUN_FILE_NAME = "run.sh";
	private static final String ID_FILE = "id";
	private static final String RUNLIST_FILE = "runlist";

	public static Logger logger = Logger.getLogger(ImageUtil.class.getName());

	private ZipUtil zipUtil;

	private Path imageTempDir;
	private String imageId;

	@Value("${working.repository}")
	private String workingRepo;

	public ImageUtil() {
	}

	@PostConstruct
	public void init() throws IOException {
		Path repo = Paths.get(workingRepo);
		Files.createDirectories(repo);
		this.imageTempDir = Files.createTempDirectory(
				Paths.get(repo.toString()), "image-");
		logger.info("Using temporary-folder: " + imageTempDir);
	}

	public synchronized Path createImage(Plan deviceComponentBundle,
			String idPrefix) throws IOException {

		if (idPrefix != null && !idPrefix.isEmpty())
			idPrefix += "_";
		else
			idPrefix = "unknown_";

		this.imageId = idPrefix + UUID.randomUUID().toString();

		logger.info("Building image with id: " + imageId);

		this.zipUtil = new ZipUtil(imageId + ".zip", imageTempDir, workingRepo);

		List<String> componentNames = new ArrayList<String>();

		// create image structure and copy files (artifacts and scripts)
		for (Component component : deviceComponentBundle.getComponents()) {
			Path compParentPath = Files.createDirectories(imageTempDir
					.resolve(component.getName()));
			componentNames.add(component.getName());

			Path artifactParentPath = Files.createDirectories(compParentPath
					.resolve("artifacts"));

			Path scriptParentPath = Files.createDirectories(compParentPath
					.resolve("scripts"));

			for (Resource artifact : component.getBinaries()) {
				Path artifactFile = Files.createFile(artifactParentPath
						.resolve(artifact.getName()));
				saveFile(artifact.getUri(), artifactFile);
			}

			for (Resource script : component.getScripts()) {
				Path scriptFile = Files.createFile(scriptParentPath
						.resolve(script.getName()));
				saveFile(script.getUri(), scriptFile);
			}
		}

		// create runlist file and write component-names
		StringBuilder builder = new StringBuilder();
		// boolean firstItem = true;
		for (String componentName : componentNames) {
			// if (!firstItem)
			// builder.append("\n");
			// else
			// firstItem = false;
			// builder.append(componentName);
			builder.append(componentName + "\n");
		}
		Path runlistFile = Files.createFile(imageTempDir.resolve(RUNLIST_FILE));
		Files.write(runlistFile, builder.toString().getBytes());

		// create file and write image-id
		Path idFile = Files.createFile(imageTempDir.resolve(ID_FILE));
		Files.write(idFile, imageId.getBytes());

		// add generic image-run script
		Path globalRunFile = Files.createFile(imageTempDir
				.resolve(IMAGE_RUN_FILE_NAME));
		saveFile(Config.IMAGE_RUN_SCRIPT, globalRunFile);

		logger.info("Finished building and initiate archiving!");

		// create final image by compressing the contents into a zip
		Path zipFile = zipUtil.createZip();

		logger.info("Finished archiving image: " + zipFile.toString());

		return zipFile;
	}

	public void clean() {
		if (imageTempDir != null && imageTempDir.toFile().exists()) {
			try {
				Files.walkFileTree(imageTempDir, new RemoveFileVisitor());
			} catch (IOException e) {
				// e.printStackTrace();
			}
		}
		if (zipUtil != null)
			zipUtil.clean();
	}

	private void saveFile(URL url, Path file) throws IOException {
		ReadableByteChannel rbc = Channels.newChannel(url.openStream());
		FileChannel channel = FileChannel
				.open(file, EnumSet.of(StandardOpenOption.CREATE,
						StandardOpenOption.TRUNCATE_EXISTING,
						StandardOpenOption.WRITE));
		channel.transferFrom(rbc, 0, Long.MAX_VALUE);
		channel.close();
	}

	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

	public static class RemoveFileVisitor extends SimpleFileVisitor<Path> {
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
				throws IOException {
			Files.delete(file);
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc)
				throws IOException {
			Files.delete(dir);
			return FileVisitResult.CONTINUE;
		}
	}

	public static void main(String[] args) {
		try {
			ImageUtil util = new ImageUtil();
			util.workingRepo = "/tmp/builder/";
			util.init();
			Path zip = util.createImage(Config.DEVICE_PLAN, "");
			System.out.println("Got: " + zip);
			util.clean();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
