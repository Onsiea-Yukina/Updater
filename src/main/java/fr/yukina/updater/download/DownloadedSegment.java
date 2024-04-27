package fr.yukina.updater.download;

public record DownloadedSegment(DownloadSegment segment, byte[] data, boolean success)
{
}