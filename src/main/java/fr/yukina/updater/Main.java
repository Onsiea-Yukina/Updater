package fr.yukina.updater;

import fr.yukina.updater.download.DownloadManager;

import java.io.File;

public class Main
{
	public static void main(String[] args)
	{
		new DownloadManager(8080, "test", "1.0", new File("resources/generated/received/")).downloadConcurrency(4);
	}
}
