package fr.yukina.updater.data.version;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
public class Program
{
	private final String name;
	private final String title;
	private final String shortDescription;
	private final String description;
	private final String githubLink;
	private final String link;
	private final String image;
	private final String defaultFilePath;

	private String currentFilePath;
}