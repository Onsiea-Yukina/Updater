package fr.yukina.updater;

import fr.yukina.updater.download.DownloadManager;
import fr.yukina.updater.installer.Installer;

import java.io.File;
import java.io.IOException;

public class Main
{
	public static void main(String[] args)
	{
		new DownloadManager(8080, "test", "1.0", new File("resources/generated/received/")).downloadConcurrency(4);
		try
		{
			Installer.install(new File("resources/generated/received/test_1.0.zip"), new File("resources/generated/install/"));
		}
		catch (IOException eIn)
		{
			throw new RuntimeException(eIn);
		}
	}
}
