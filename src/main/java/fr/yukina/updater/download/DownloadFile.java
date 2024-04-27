package fr.yukina.updater.download;

import lombok.Setter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public final class DownloadFile
{
	private final String                name;
	private final long                  size;
	private final long                  step;
	private @Setter File                  destination;
	private final   List<DownloadSegment> segments;
	private final Map<Integer, DownloadedSegment> downloadedSegmentMap;

	public DownloadFile(String nameIn, long sizeIn, long stepIn, File destinationIn)
	{
		this.name = nameIn;
		this.size = sizeIn;
		this.step = stepIn;
		this.destination = destinationIn;
		this.segments = new ArrayList<>();
		int segment = 0;
		for(var range = 0; range < this.size; range += this.step)
		{
			this.segments.add(new DownloadSegment(this.name, segment, range, Math.min(range + this.step - 1,
			                                                                          this.size - 1)));
			segment ++ ;
		}
		this.downloadedSegmentMap = new ConcurrentHashMap<>();
	}

	public void downloadConcurrency(int portIn, String programNameIn, String versionNameIn, int threadCountIn)
	{
		System.out.println("DOWNLOAD FILE : " + this.name + "[" + this.size + "@" + this.step + "]");
		var                  executorService = Executors.newFixedThreadPool(threadCountIn);
		for(var segment : this.segments)
		{
			executorService.submit(() -> {
				try
				{
					var downloaded =  segment.download(portIn);
					if (downloaded != null)
					{
						this.downloadedSegmentMap.put(segment.number(), downloaded);
					}
				}
				catch (Exception e)
				{
					System.out.println("Failed to download segment " + segment.number() + ": " + e.getMessage());
				}
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

		this.writeFile(this.destination.getAbsolutePath(), this.name, this.size, this.downloadedSegmentMap);
	}

	public void download(int portIn, String programNameIn, String versionNameIn)
	{
		System.out.println("DOWNLOAD FILE : " + this.name + "[" + this.size + "@" + this.step + "]");

		for(var segment : this.segments)
		{
			var downloadedSegment = segment.download(portIn);

			if(downloadedSegment == null)
			{
				continue;
			}

			this.downloadedSegmentMap.put(segment.number(), downloadedSegment);
		}

		this.writeFile(this.destination.getAbsolutePath(), this.name, this.size, this.downloadedSegmentMap);
	}

	private void writeFile(String directoryIn, String fileNameIn, long sizeIn, Map<Integer, DownloadedSegment> segmentMapIn)
	{
		var directory = new File(directoryIn);
		if (!directory.exists())
		{
			if(!directory.mkdirs())
			{
				throw new RuntimeException("Unable to create directory " + directory.getAbsolutePath());
			}
		}
		var file = new File(directory, fileNameIn);

		try (FileOutputStream outputStream = new FileOutputStream(file);
		     BufferedOutputStream bufferedStream = new BufferedOutputStream(outputStream))
		{
			final AtomicLong i = new AtomicLong();

			segmentMapIn.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEachOrdered(entry -> {
				if(i.get() > sizeIn)
				{
					return;
				}

				try {
					bufferedStream.write(entry.getValue().data());
				} catch (IOException e) {
					throw new RuntimeException("Failed to write segment to file " + fileNameIn + ": " + e.getMessage(), e);
				}

				i.set(i.get() + entry.getValue().data().length);
			});
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}