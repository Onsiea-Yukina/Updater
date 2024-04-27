package fr.yukina.updater.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

public class APIUtils
{
	public static RequestReport clientRequest(int portIn, String path, String... queries)
	{
		long startTime = System.currentTimeMillis();
		String requestUrl = buildRequestUrl("http://localhost:" + portIn, path, queries);
		long queryConstructionTime = System.currentTimeMillis() - startTime;

		HttpURLConnection connection = null;
		try
		{
			URL url = new URL(requestUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);
			connection.setRequestMethod("GET");

			long requestStartTime = System.currentTimeMillis();
			int responseCode = connection.getResponseCode();
			long requestTime = System.currentTimeMillis() - requestStartTime;

			boolean isSuccess = responseCode >= 200 && responseCode < 300;
			String returnType = connection.getContentType();
			Object returnObject = null;

			if (isSuccess)
			{
				try (InputStream inputStream = connection.getInputStream())
				{
					if (returnType.contains("application/json"))
					{
						BufferedReader reader   = new BufferedReader(new InputStreamReader(inputStream));
						String         line;
						StringBuilder  response = new StringBuilder();
						while ((line = reader.readLine()) != null)
						{
							response.append(line);
						}
						returnObject = response.toString();
					}
					else
					{
						// Handle binary data
						ByteArrayOutputStream buffer = new ByteArrayOutputStream();
						int                   nRead;
						byte[] data = new byte[1024];
						while ((nRead = inputStream.read(data, 0, data.length)) != -1)
						{
							buffer.write(data, 0, nRead);
						}
						buffer.flush();
						returnObject = buffer.toByteArray();
					}
				}
			}

			return new RequestReport(requestUrl, isSuccess, responseCode, connection.getResponseMessage(), returnType,
			                         returnObject, queryConstructionTime, requestTime);
		}
		catch (ProtocolException eIn)
		{
			throw new RuntimeException(eIn);
		}
		catch (IOException eIn)
		{
			throw new RuntimeException(eIn);
		}
		finally
		{
			if (connection != null)
			{
				connection.disconnect();
			}
		}
	}

	private static String buildRequestUrl(String baseUrl, String path, String[] queries) {
		StringBuilder requestUrlBuilder = new StringBuilder(baseUrl);
		if (path != null && !path.isEmpty()) {
			requestUrlBuilder.append("/").append(path);
		}
		if (queries.length > 0) {
			requestUrlBuilder.append("?");
			for (int i = 0; i < queries.length; i++) {
				if (i > 0) {
					requestUrlBuilder.append("&");
				}
				requestUrlBuilder.append(queries[i]);
			}
		}
		return requestUrlBuilder.toString();
	}

	public record RequestReport(String requestURL, boolean isSuccess, int returnCode, String returnMessage,
	                            String returnType,
	                            Object returnObject, long queryConstructionTime, long requestTime)
	{
	}
}
