package fr.yukina.updater.download;

import fr.yukina.updater.utils.APIUtils;

public record DownloadSegment(String name, int number, long start, long end)
{
	public DownloadedSegment download(int portIn)
	{
		System.out.println("DOWNLOAD SEGMENT : " + this.name + "[" + this.start + ":" + this.end + "]");
		var report = APIUtils.clientRequest(portIn, "version/files/", "program=test", "version=1.0",
		                                    "file=" + this.name,
		                           "start=" + this.start, "end=" + this.end);

		if (!report.isSuccess() || report.returnObject() == null)
		{
			System.err.println("Failed to download segment " + this.number + ": " + report.returnMessage());
			return new DownloadedSegment(this, (byte[]) report.returnObject(), false);
		}

		return new DownloadedSegment(this, (byte[]) report.returnObject(), report.isSuccess());
	}
}