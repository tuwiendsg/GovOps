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
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.ZipException;

/**
 * @author michael
 * 
 */

public class ZipUtil {

	private Logger logger = Logger.getLogger(ZipUtil.class.getName());

	private URI zipFileURI;
	private Path zipFile;
	private Path sourceDir;

	public ZipUtil(String zipFileName, Path sourceDir, String workingRepo)
			throws IOException {
		this.sourceDir = sourceDir;
		this.zipFileURI = URI.create("jar:file:" + workingRepo + zipFileName);
		this.zipFile = Paths.get(workingRepo + zipFileName);
	}

	public Path createZip() throws ZipException, IOException {
		Map<String, String> environment = new HashMap<>();
		environment.put("create", "true");
		environment.put("useTempFile", "true");
		FileSystem zipFileSystem = FileSystems.newFileSystem(zipFileURI,
				environment);
		Iterable<Path> rootDirectories = zipFileSystem.getRootDirectories();
		Path root = rootDirectories.iterator().next();

		Files.walkFileTree(sourceDir, new CopyFileVisitor(root));

		zipFileSystem.close();

		return zipFile;
	}

	public void clean() {
		if (zipFile != null && zipFile.toFile().exists())
			try {
				Files.delete(zipFile);
			} catch (IOException e) {
//				e.printStackTrace();
			}
	}

	private static class CopyFileVisitor extends SimpleFileVisitor<Path> {

		private Logger logger = Logger.getLogger(CopyFileVisitor.class
				.getName());

		private final Path targetPath;
		private Path sourcePath = null;

		public CopyFileVisitor(Path targetPath) {
			this.targetPath = targetPath;
		}

		@Override
		public FileVisitResult preVisitDirectory(final Path dir,
				final BasicFileAttributes attrs) throws IOException {

			if (sourcePath == null) {
				sourcePath = dir;
			} else {
				Files.createDirectories(targetPath.resolve(sourcePath
						.relativize(dir).toString()));
			}

			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(final Path file,
				final BasicFileAttributes attrs) throws IOException {

			logger.info(file.toString());

			Files.copy(file,
					targetPath.resolve(sourcePath.relativize(file).toString()),
					StandardCopyOption.REPLACE_EXISTING);

			return FileVisitResult.CONTINUE;
		}
	}

}
