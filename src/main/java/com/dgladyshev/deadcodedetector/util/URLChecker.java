package com.dgladyshev.deadcodedetector.util;

import com.dgladyshev.deadcodedetector.exceptions.MalformedRepositoryURL;

import java.net.HttpURLConnection;
import java.net.URL;

public class URLChecker {

	public static void isAccessible(String url) throws MalformedRepositoryURL {
		try {
			HttpURLConnection.setFollowRedirects(false);
			HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
			con.setRequestMethod("HEAD");
			if (con.getResponseCode() == HttpURLConnection.HTTP_OK) return;
		}
		catch (Exception e) {
			//nothing needs to be done
		}
		throw new MalformedRepositoryURL();
	}

}
