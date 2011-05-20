package ch.hsr.audiotagger.io.cddb;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.jaudiotagger.audio.AudioFile;

/**
 * Ermöglicht das Abfragen einer CDDB-basierten Datenbank im Internet/Netzwerk.<br/>
 * <br/>
 * Hierzu werden Audio-Dateien aus einem Album in ihrer KORREKTEN Reihenfolge
 * aufgrund ihrer Spielzeit analysiert.<br/>
 * Die daraus resultierenden Disc-Start-Frames werden zusammen mit einer Prüfsumme,
 * der totalen Spielzeit und der Anzahl Titel (= DiscID) zur identifikation des
 * Albums in der CDDB-Datenbank verwendet.<br/>
 * <br/>
 * Diese Informationen werden normalerweise direkt ab einer Audio-CD ausgelesen.
 * Da hier aber mit bereits <em>fertigen</em> Audio-Dateien gearbeitet wird,
 * übernimmt {@link DiscInfo#generateDiscInfo(AudioFile[])} die quasi
 * "umgekehrte" Berechnung der erwähnten Werte.<br/>
 * <br/>
 * Der Geschichte einfaches Ende: Nach dem Instanzieren der {@link CDDB}-Klasse
 * kann der Methode {@link #getTrackInformation(AudioFile[])} einfach ein
 * Array aus {@link AudioFile}'s übergeben werden.<br/>
 * {@link #getTrackInformation(AudioFile[])} wird anschliessend alle gefundenen
 * Treffer, passend zum {@link AudioFile}-Array, zurückliefern.<br/>
 * <br/>
 * <strong>ACHTUNG!</strong> Nochmal zur Wiederholung: Die {@link AudioFile}'s
 * müssen im Array dieselbe Reihenfolge haben, welche sie auf der originalen
 * Audio-CD hatten. Ansonsten führt die CDDB-Abfrage zu inkorrekten Resultaten.<br/>
 * <br/>
 * <strong>HINWEIS:</strong> Damit AudioTagger auch hinter Firewalls CDDB-Informationen
 * abrufen kann, implementiert diese Klasse NUR die Abfragevariante via HTTP-Protokol,
 * also Port 80.
 * 
 * @author Manuel Alabor
 * @see #getDiscInformation(AudioFile[])
 * @see DiscInfo
 * @see QueryResult
 */
public class CDDB {
	
	private String cddbHttpServerUrl;
	
	private final static String FREEDB_USER = "jondoe";
	private final static String FREEDB_APPLICATION_ID = "AudioTagger";
	private final static String FREEDB_APPLICATION_VERSION = "1";
	
	/** CDDB-Kommando: Wort des Tages */
	@SuppressWarnings("unused")
	private final static String COMMAND_MOTD = "motd";
	/** CDDB-Kommando: Kategorien auflisten */
	@SuppressWarnings("unused")
	private final static String COMMAND_LSCAT = "cddb lscat";
	/** CDDB-Kommando: Sites auflisten */
	@SuppressWarnings("unused")
	private final static String COMMAND_SITES = "sites";
	/** CDDB-Kommando: Eintrag lesen */
	private final static String COMMAND_READ = "cddb read";
	/** CDDB-Kommand: Einträge suchen */
	private final static String COMMAND_QUERY = "cddb query";
	
	private final static String LINE_BREAK = "\n";
	private final static String UTF8_ENCODING = "utf8";
	
	
	// Konstruktoren -----------------------------------------------------------
	/**
	 * Instanziert ein {@link CDDB}-Objekt für den übergebenen CDDB-Server
	 * <code>cddbHttpServerUrl</code>.
	 * 
	 * @param cddbHttpServerUrl
	 */
	public CDDB(String cddbHttpServerUrl) {
		this.cddbHttpServerUrl = cddbHttpServerUrl;
	}
	
	
	// One-Point-Of-Entry ------------------------------------------------------
	/**
	 * 
	 */
	public XMCD[] getDiscInformation(AudioFile[] audioFiles) throws Exception {
		
		/* Vorbereiten: */
		ArrayList<XMCD> xmcds = new ArrayList<XMCD>();
		
		if(audioFiles.length > 0) {
			/* DiscInfo genereieren: */
			DiscInfo discInfo = DiscInfo.generateDiscInfo(audioFiles);
			
			/* CDDB Query: */
			// Per Query nach zur DiscInfo passenden Einträgen in der CDDB
			// suchen.
			QueryResult[] queryResults = query(discInfo);
			
			/* CDDB Read: */
			// Ermittelte QueryResults verwenden, um per Read nähere Informationen
			// zu holen.
			for(QueryResult q: queryResults) {
				XMCD buf = read(q);
				if(buf != null) xmcds.add(buf);
			}
		}
		
		/* Rückgabe: */
		return xmcds.toArray(new XMCD[xmcds.size()]);
	}

	
	// CDDB-Implementierung ----------------------------------------------------
	/**
	 * Führt mit einer {@link DiscInfo}-Instanz das Query-Kommando auf der CDDB-
	 * Datenbank aus.<br/>
	 * Das Query-Kommando liefert <em>0-n</em> Resultate welche jeweils
	 * Informationen zur Disc-Kategorie, DiscID sowie Disc-Titel ([Artist] / [Album])
	 * enthalten. Diese Informationen können anschliessend für das Read-Kommando
	 * verwendet werden, um genauere Informationen für eine Disc/ein Album abzufragen.<br/>
	 * <br/>
	 * Genau diese Infromationen werden in einem {@link QueryResult}-Array zurückgegeben.<br/>
	 * Wurde nichts gefunden, wird ein leeres Array zurückgegeben.
	 * 
	 * @return QueryResult[]
	 * @see DiscInfo
	 * @see QueryResult
	 * @see #COMMAND_QUERY
	 * @see #callCDDBServer(String, String)
	 * @see #read(String, String)
	 */
	private QueryResult[] query(DiscInfo discInfo) throws Exception {
		
		/* Vorbereiten: */
		QueryResult[] queryResult = new QueryResult[0];
		
		/* Query-Kommando zusammenstellen: */
		int[] frames = discInfo.getFrames();
		StringBuilder command = new StringBuilder();
		command.append(COMMAND_QUERY + " ");
		command.append(discInfo.getDiscId() + " ");
		command.append(discInfo.getTracks() + " ");
		for(int i = 0, l = frames.length-1; i < l; i++) command.append(Integer.toString(frames[i]) + " ");
		command.append(Integer.toString(discInfo.getTotalLength()));

		/* CDDB-Server aufrufen: */
		String[] resultLines = callCDDBServer(cddbHttpServerUrl, command.toString());
		
		/* Resultat parsen: */
		if(resultLines.length > 0) {
			// Erste Zeile enthält Statuscode zu Beginn:
			if(resultLines[0].startsWith("200")) {
				// Genau ein Resultat gefunden:
				queryResult = new QueryResult[1];
				queryResult[0] = QueryResult.parseQueryResult(resultLines[1]);
				
			} else if(resultLines[0].startsWith("211")) {
				// Mehrere Resultate gefunden:
				queryResult = new QueryResult[resultLines.length-2];  // -2 = minus Statuszeile minus Termination Zeile
				for(int i = 1, l = resultLines.length-1; i < l; i++) {
					queryResult[i-1] = QueryResult.parseQueryResult(resultLines[i]);
				}
			
			} else {
				// TODO error oder nichts gefunden
			}
		}
		
		/* Rückgabe: */
		return queryResult;
	}
	
	/**
	 * Liest die Informationen einer bestimmten Disc/eines bestimmten Albums aus
	 * der Datenbank. Diese enthalten alle verfügbaren Infos wie Track-Infos,
	 * Jahr, usw.
	 * 
	 * @param discCategory bspw. rock etc.
	 * @param discId
	 * @see #query(DiscInfo)
	 * @see QueryResult
	 */
	private XMCD read(QueryResult queryResult) throws Exception {
		/* Vorbereiten: */
		XMCD xmcd = null;
		
		/* Read-Kommando zusammenstellen: */
		StringBuilder command = new StringBuilder();
		command.append(COMMAND_READ + " ");
		command.append(queryResult.getCategory() + " ");
		command.append(queryResult.getDiscId());

		/* CDDB-Server aufrufen: */
		String[] resultLines = callCDDBServer(cddbHttpServerUrl, command.toString());
		
		/* Resultat parsen: */
		if(resultLines.length > 0) {
			// Erste Zeile enthält Statuscode zu Beginn:
			if(resultLines[0].startsWith("210")) {
				// XMCD-Eintrag/File gefunden.
				// Per StringBuilder XMCD extrahieren und anschliessend per
				// XMCD#parseXMCD(String) parsen.
				StringBuilder xmcdSource = new StringBuilder();
				for(int i = 1, l = resultLines.length-1; i < l; i++) {  // erste Zeile Statuscode, letzte Zeile Terminator "."
					xmcdSource.append(resultLines[i] + LINE_BREAK);
				}
				
				xmcd = XMCD.parseXMCD(xmcdSource.toString());
			} else {
				// TODO error oder nichts gefunden
			}
		}
		
		/* Rückgabe: */
		return xmcd;
	}
	
	
	// Server-Handling ---------------------------------------------------------
	/**
	 * Nimmt einen CDDB-HTTP-URL mit allen Parametern, empfängt das Resultat und
	 * gibt dieses in einem {@link String}-Array (ein Eintrag pro Zeile; leere
	 * Zeilen werden gefiltert und nicht zurückgegeben)
	 * zurück.
	 * 
	 * @param urlString
	 * @return
	 * @throws Exception
	 */
	private String[] callCDDBServer(String httpServerUrl, String command) throws Exception {
		/* Vorbereiten: */
		// URL zusammensetzen:
		String localhostName = InetAddress.getLocalHost().getHostName();
		String urlString = httpServerUrl
						 + "?cmd=" + URLEncoder.encode(command, UTF8_ENCODING)  // Kommando
						 + "&hello=" + FREEDB_USER + "+"				  // Handshake
						 	+ localhostName + "+"
						 	+ FREEDB_APPLICATION_ID + "+"
						 	+ FREEDB_APPLICATION_VERSION
					 	+ "&proto=6";									  // Server-Version
		
		// Buffers & URL-Instanz:
		ArrayList<String> resultLines = new ArrayList<String>();
		URL url = new URL(urlString);
		
		/* Server aufrufen: */
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String inLine = null;
        while ((inLine = in.readLine()) != null) {
        	String trimmed = inLine.trim();
        	if(!trimmed.isEmpty()) resultLines.add(trimmed);
        }
        in.close();
        
        /* Alle einzelnen Zeilen in einem Array zurückgeben: */
        String[] lines = resultLines.toArray(new String[resultLines.size()]);
        return lines;
	}
	
}
