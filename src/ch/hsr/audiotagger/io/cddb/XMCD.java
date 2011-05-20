package ch.hsr.audiotagger.io.cddb;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Das Read-Kommando einer CDDB-Datenbank liefert, falls der Eintrag gefunden
 * wurde, die Informationen einer CD/eines Albums im XMCD-Format. Diese Klasse
 * bildet die Informationen eines solchen Files für Java ab.<br/><br/>
 * Folgend ein Beispiel einer solchen Datei:<br/>
 * <code>
 * # xmcd<br/>
 * #<br/>
 * # Track frame offsets:<br/>
 * #	150<br/>
 * #	14717<br/>
 * #	31910<br/>
 * #<br/>
 * # Disc length: 3368 seconds<br/>
 * #<br/>
 * # Revision: 14<br/>
 * # Processed by: cddbd v1.5.2PL0 Copyright (c) Steve Scherf et al.<br/>
 * # Submitted via: EasyCDDAExtractor 10.0.6.1<br/>
 * #<br/>
 * DISCID=bb0d250e,bd0d260e<br/>
 * DTITLE=Beck / Odelay<br/>
 * DYEAR=1996<br/>
 * DGENRE=Rock<br/>
 * TTITLE0=Devil's Haircut<br/>
 * TTITLE1=Hotwax<br/>
 * TTITLE2=Lord Only Knows<br/>
 * EXTD= YEAR: 1996<br/>
 * EXTT0=<br/>
 * EXTT1=<br/>
 * EXTT2=<br/>
 * PLAYORDER=</code><br/>
 * <br/>
 * Daraus geht hervor, dass das File-Format aus simpeln Key/Value-Paaren besteht.
 * Jede Zeile mit einem "#" enthält einen Kommentar und kann somit ignoriert werden.<br/>
 * <br/>
 * TODO bessere JavaDoc
 * 
 * @author Manuel Alabor
 * @see CDDB
 */
public class XMCD {
	
	private String discId;
	private String title;
	private String genre;
	private String year;
	private String playorder;
	private String[] trackTitles;
	
	private final static String FIELD_DISCID = "DISCID";
	private final static String FIELD_TITLE = "DTITLE";
	private final static String FIELD_GENRE = "DGENRE";
	private final static String FIELD_YEAR = "DYEAR";
	private final static String FIELD_PLAYORDER = "PLAYORDER";
	private final static String FIELD_TRACKTITLE_PREFIX = "TTITLE";
	
	
	
	public XMCD(String discId, String title, String genre, String year, String playorder, String[] trackTitles) {
		this.discId = discId;
		this.title = title;
		this.genre = genre;
		this.year = year;
		this.playorder = playorder;
		this.trackTitles = trackTitles;
	}
	
	// Zugriff -----------------------------------------------------------------
	public String getDiscId() {
		return discId;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getGenre() {
		return genre;
	}
	
	public String getYear() {
		return year;
	}
	
	public String getPlayorder() {
		return playorder;
	}
	
	public String[] getTrackTitles() {
		return trackTitles;
	}
	
	// Factory -----------------------------------------------------------------
	/**
	 * Parst ein XMCD-File und erstellt daraus eine {@link XMCD}-Instanz und
	 * gibt diese zurück.
	 * 
	 * @param xmcdSource
	 * @see XMCD
	 * @see CDDB
	 */
	public static XMCD parseXMCD(String xmcdSource) {
		/* Vorbereiten: */
		XMCD xmcd = null;
		Properties props = new Properties();
		
		/* XMCD einlesen: */
		// Das XMCD-Format kann glücklicherweise mit dem Java-eigenen Properties-
		// Konstrukt parsen, da die beiden Formate gleich aufgebaut sind.
		try {
			props.load(new StringReader(xmcdSource));
			
			// Felder:
			String discId = props.getProperty(FIELD_DISCID);
			String genre = props.getProperty(FIELD_GENRE);
			String title = props.getProperty(FIELD_TITLE);
			String year = props.getProperty(FIELD_YEAR);
			String playorder = props.getProperty(FIELD_PLAYORDER);
			
			// Tracktitles:
			ArrayList<String> tracks = new ArrayList<String>();
			for(int i = 0, l = 1000; i < l; i++) {
				String trackName = props.getProperty(FIELD_TRACKTITLE_PREFIX + i);
				if(trackName != null) tracks.add(trackName);
				else break;
			}
			
			// XMCD-Instanz erstellen:
			xmcd = new XMCD(discId, title, genre, year, playorder, tracks.toArray(new String[tracks.size()]));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/* Rückgabe: */
		return xmcd;
	}
	
}
