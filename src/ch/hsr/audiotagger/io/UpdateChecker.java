package ch.hsr.audiotagger.io;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import ch.hsr.audiotagger.AudioTaggerApplication;

public class UpdateChecker {

	private String checkerUrl;
	private String appId;
	
	public final static String CHECKER_URL = "http://shared.msites.net/updatechecker/index.php";
	public final static String APP_ID = "AudioTagger";
	
	public UpdateChecker(String checkerUrl, String appId) {
		this.checkerUrl = checkerUrl;
		this.appId = appId;
	}
	
	public VersionInfo checkVersion(String currentVersion) {
		String result = "";
		String url = "";
		String version = "";
		boolean newVersionAvailable = false;
		
		try {
			URL fullUrl = new URL(this.checkerUrl + "?appid=" + this.appId.toLowerCase() + "&users_version=" + currentVersion);
			
	        BufferedReader in = new BufferedReader(new InputStreamReader(fullUrl.openStream()));
	        String inLine = null;
	        while ((inLine = in.readLine()) != null) {
	        	String trimmed = inLine.trim();
	        	if(!trimmed.isEmpty()) {
	        		result = trimmed;
	        		break;  // es wird nur eine Zeile erwartet.
	        	}
	        }
	        in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/* Auswerten: */
		if(!result.isEmpty()) {
			String[] elements = result.split(";");
			if(elements[0].equals("outdated")) {
				url = elements[2];
				version = elements[1];
				newVersionAvailable = true;
			}
		}
		
		return new VersionInfo(url,version,newVersionAvailable);
	}
	
	// Hilfsklassen ------------------------------------------------------------
	public class VersionInfo {
		private String url;
		private String version;
		private boolean newVersionAvailable;
		
		public VersionInfo(String url, String version, boolean newVersionAvailable) {
			this.url = url;
			this.version = version;
			this.newVersionAvailable = newVersionAvailable;
		}
		
		public String getUrl() {
			return url;
		}
		
		public String getVersion() {
			return version;
		}
		
		public boolean isNewVersionAvailable() {
			return newVersionAvailable;
		}
	}
	
	
	public static void main(String[] args) {
		UpdateChecker checker = new UpdateChecker(CHECKER_URL, APP_ID);
		System.out.println(checker.checkVersion(AudioTaggerApplication.getVersion()).isNewVersionAvailable());
	}
}
