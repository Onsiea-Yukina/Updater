package fr.yukina.updater.installer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Installer
{
	public final static void install(File sourceIn, File destinationIn) throws IOException
	{
		if(!sourceIn.exists())
		{
			throw new RuntimeException("Source file does not exist : " + sourceIn.getAbsolutePath());
		}

		if(!destinationIn.exists() && !destinationIn.mkdirs())
		{
			throw new RuntimeException("Failed to create destination directory : " + destinationIn.getAbsolutePath());
		}

		if(sourceIn.isFile())
		{
			if(sourceIn.getName().endsWith(".zip"))
			{
				extract(sourceIn, destinationIn);
			}
			else
			{
				copy(sourceIn, destinationIn);
			}

			return;
		}

		var files = sourceIn.listFiles();
		if(files == null)
		{
			return;
		}

		for(var file : files)
		{
			install(file, new File(destinationIn, file.getName()));
		}
	}

	/**
	 * Extracts a ZIP file into a specified destination directory.
	 *
	 * @param sourceIn      The source ZIP file.
	 * @param destinationIn The destination directory where the ZIP contents will be extracted.
	 */
	private static void extract(File sourceIn, File destinationIn) throws IOException
	{
		if (!destinationIn.exists())
		{
			destinationIn.mkdirs(); // Make sure the destination directory exists
		}

		try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(sourceIn)))
		{
			ZipEntry entry;
			while ((entry = zipInputStream.getNextEntry()) != null)
			{
				File entryDestination = new File(destinationIn, entry.getName());
				if (entry.isDirectory())
				{
					entryDestination.mkdirs();
				}
				else
				{
					entryDestination.getParentFile().mkdirs(); // Ensure parent directories exist
					extractFile(zipInputStream, entryDestination);
				}

				zipInputStream.closeEntry();
			}
		}
	}

	/**
	 * Extracts a file from the zip input stream into a specified destination file.
	 *
	 * @param zipInputStreamIn The zip input stream.
	 * @param outputFileIn     The file to which the content will be written.
	 */
	private static void extractFile(ZipInputStream zipInputStreamIn, File outputFileIn) throws IOException
	{
		try (OutputStream outputStream = new FileOutputStream(outputFileIn))
		{
			byte[] buffer = new byte[4096];
			int length;
			while ((length = zipInputStreamIn.read(buffer)) > 0)
			{
				outputStream.write(buffer, 0, length);
			}
		}
	}

	/**
	 * Copies a file from a source location to a destination.
	 *
	 * @param sourceIn      The source file to be copied.
	 * @param destinationIn The destination file where the source file will be copied.
	 * @throws IOException If an I/O error occurs during copying.
	 */
	private static void copy(File sourceIn, File destinationIn) throws IOException {
		// Ensure that the parent directory of the destination file exists.
		if (!destinationIn.getParentFile().exists()) {
			destinationIn.getParentFile().mkdirs();
		}

		// Copy the file with the REPLACE_EXISTING option to overwrite a file if it exists at the destination.
		Files.copy(sourceIn.toPath(), destinationIn.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}
}