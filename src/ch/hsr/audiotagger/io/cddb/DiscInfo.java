package ch.hsr.audiotagger.io.cddb;

import org.jaudiotagger.audio.AudioFile;

/**
 * Stellt alle nötigen Informationen zu einer Audio-CD resp. genauer gesagt
 * Informationen zu einer Menge von Audio-Dateien zur verfügung, welche nötig
 * sind, um Informationen von einem CDDB-Server abzufragen.<br/>
 * <br/>
 * Per {@link #generateDiscInfo(AudioFile[])} wird eine Instanz von {@link DiscInfo}
 * passend zu den übergebenen {@link AudioFile}'s generiert und zurückgegeben.
 * 
 * @author Manuel Alabor
 * @see CDDB
 * @see #generateDiscInfo(AudioFile[])
 */
public class DiscInfo {
	
	private String discId;
	private int[] frames;
	private int totalLength;
	private int tracks;
	
	public DiscInfo(String discId, int[] frames, int totalLength, int tracks) {
		this.discId = discId;
		this.frames = frames;
		this.totalLength = totalLength;
		this.tracks = tracks;
	}
	
	public String getDiscId() {
		return discId;
	}
	
	public int[] getFrames() {
		return frames;
	}
	
	public int getTotalLength() {
		return totalLength;
	}
	
	public int getTracks() {
		return tracks;
	}
	
	@Override
	public String toString() {
		return "[DiscInfo: " + getDiscId() + ", " + getTotalLength() + ", " + getTracks() + "]";
	}
	
	
	// Factory -----------------------------------------------------------------
	/**
	 * Erstellt eine {@link DiscInfo}-Instanz aufgrund eines {@link AudioFile}-
	 * Arrays.
	 * 
	 * @param audioFiles
	 * @see DiscID-Berechnung: http://www.cs.princeton.edu/introcs/51data/CDDB.java.html
	 */
	public static DiscInfo generateDiscInfo(AudioFile[] audioFiles) {
		/* Vorbereiten: */
		// Allgemein:
		int totalLength = 0;
		int checkSum = 0;
		int framesPerSecond = 75;
		
		// Frames:
		int[] frames = new int[audioFiles.length+1];
		frames[0] = 150;  // StartGap; zu Beginn der CD gibt es "leere" Frames
		
		/* Frames, totalLength & checksum berechnen/vorbereiten: */
		for(int i = 0, l = audioFiles.length; i < l; i++) {
			int trackLength = audioFiles[i].getAudioHeader().getTrackLength();
			totalLength += trackLength;
			
			int frameLength = trackLength * framesPerSecond;
			frames[i+1] = frames[i] + frameLength;  // immer zum vorangegangen hinzuzählen = Offset
			
			checkSum += sumOfDigits(trackLength);
		}
		
		/* DiscID berechnen: */
		int XX = checkSum % 255;
		int YYYY = totalLength;
		int ZZ = audioFiles.length;
		
		// DiscID-Format: XXYYYYZZ; in einem 32bit-Int zusammenfassen:
		int discId = ((XX << 24) | (YYYY << 8) | ZZ);
		
		/* DiscInfo erstellen und zurückgeben: */
		DiscInfo discInfo = new DiscInfo(
				Integer.toHexString(discId),
				frames,
				totalLength,
				audioFiles.length
		);
		return discInfo;
	}
	
	/**
	 * Return sum of decimal digits in n
	 * 
	 * @param n
	 * @return
	 * @see #generateDiscInfo(AudioFile[])
	 */
	private static int sumOfDigits(int n) {
		int sum = 0;
		while (n > 0) {
			sum = sum + (n % 10);
			n = n / 10;
		}
		return sum;		
	}

}
