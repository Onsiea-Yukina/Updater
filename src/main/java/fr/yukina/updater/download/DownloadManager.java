package fr.yukina.updater.download;

import com.google.gson.Gson;
import fr.yukina.updater.utils.APIUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class DownloadManager
{
	private final int port;
	private final String                    program;
	private final String                    version;
	private final Map<String, DownloadFile> fileMap;

	public DownloadManager(int portIn, String programNameIn, String versionNameIn, File destinationIn)
	{
		if(!destinationIn.exists() && !destinationIn.mkdirs())
		{
			throw new RuntimeException("Failed to create destination directory : " + destinationIn.getAbsolutePath());
		}

		this.port = portIn;
		this.program = programNameIn;
		this.version = versionNameIn;
		this.fileMap = new HashMap<>();

		var                             report = APIUtils.clientRequest(this.port, "version/files",
		                                                                "program=" + this.program,
		                                                           "version=" + this.version);
		FileDescriptor[] files = new Gson().fromJson((String) report.returnObject(), FileDescriptor[].class);
		for (FileDescriptor file : files)
		{
			this.fileMap.put(file.name(), new DownloadFile(file.name(), file.size(), 512, destinationIn));
		}
	}

	public void downloadConcurrency(int threadCountIn)
	{
		var executorService = Executors.newFixedThreadPool(threadCountIn);
		for(var downloadFile : this.fileMap.values())
		{
			executorService.submit(() ->
			                       {
				                       downloadFile.downloadConcurrency(this.port, this.program, this.version,
				                                                        threadCountIn);
			                       });
		}

		executorService.shutdown();
		try {
			if (!executorService.awaitTermination(1, TimeUnit.HOURS)) {
				executorService.shutdownNow();
			}
		} catch (InterruptedException e) {
			executorService.shutdownNow();
		}
	}

	public void download() {
		System.out.println("START DOWNLOAD : " + this.program + " " + this.version);
		for(var downloadFile : this.fileMap.values())
		{
			downloadFile.download(this.port, this.program, this.version);
		}
	}

	public record FileDescriptor(String name, long size)
	{
	}
}