package ch.hsr.audiotagger.ui.models;

import java.io.File;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;

/**
 * Dieses {@link TableModel} ermöglicht das Anzeigen einer oder mehreren getaggten
 * Audiodateien.
 * 
 * @author Manuel Alabor
 */
public class AudioFileTableModel extends AbstractTableModel {

	private ArrayList<AudioFile> audioFiles = new ArrayList<AudioFile>();
	
	public final static int COL_FILENAME = 0;
	public final static int COL_TRACK = 1;
	public final static int COL_TITLE = 3;
	public final static int COL_ALBUM = 4;
	public final static int COL_ARTIST = 2;
	private static final long serialVersionUID = -8340647078080785920L;
	
	public AudioFileTableModel(File[] files) {
		setFiles(files);
	}
	
	public AudioFileTableModel() {
		this(new File[]{});
	}
	
	
	// Interne Verwaltung ------------------------------------------------------
	/**
	 * Übernimmt ein {@link File}-Array und lädt es in dieses {@link TableModel}.
	 * 
	 * @param files Array mit anzuzeigenden Audiodateien
	 */
	public void setFiles(File[] files) {
		/* Vorhandene AudioFiles löschen: */
		audioFiles.clear();
		
		/* AudioFiles laden: */
		try {
			AudioFile audioFile = null;
			
			for(File file: files) {
				audioFile = AudioFileIO.read(file);
				audioFiles.add(audioFile);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		/* Angeschlossene JTable aktualisieren: */
		fireTableDataChanged();
	}
	
	/**
	 * Leert den internen Dateien-Speicher des {@link AudioFileTableModel}'s.
	 */
	public void clearFiles() {
		audioFiles.clear();
		fireTableDataChanged();  // angeschlossene JTables aktualisieren
	}
	
	/**
	 * 
	 * 
	 * @param rowIndex
	 * @return
	 */
	public AudioFile getAudioFile(int rowIndex) {
		AudioFile audioFile = audioFiles.get(rowIndex);
		return audioFile;
	}
	
	// TableModel-Implementierung ----------------------------------------------
	@Override
	/**
	 * Gibt die Anzahl Spalten der Tabelle zurück.
	 * 
	 * @return Anzahl Spalten
	 */
	public int getColumnCount() {
		return 5;
	}

	@Override
	/**
	 * Gibt die Anzahl anzuzeigende Dateien zurück. = Zeilenzahl
	 * 
	 * @return Zeilen/Dateien 
	 */
	public int getRowCount() {
		return audioFiles.size();
	}

	@Override
	/**
	 * Gibt den Wert für die Zelle auf der Zeile rowIndex und der Spalte columnIndex
	 * zurück.
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @return Object 
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		AudioFile audioFile = audioFiles.get(rowIndex);
		Object value = null;
		Tag tag = audioFile.getTag();
		
		switch(columnIndex) {
		case COL_FILENAME :
			value = audioFile.getFile().getName();
			break;
		case COL_TRACK :
			value = tag.getFirstTrack();
			break;
		case COL_ARTIST :
			value = tag.getFirstArtist();
			break;
		case COL_TITLE :
			value = tag.getFirstTitle();
			break;
		case COL_ALBUM :
			value = tag.getFirstAlbum();
			break;
		}
		
		return value;
	}
	
	@Override
	/**
	 * Gibt den Spaltennamen für die Spalte mit dem Index column zurück.
	 * 
	 * @param column
	 * @return Spaltennamen
	 */
	public String getColumnName(int column) {
		String name = "";
		
		switch(column) {
		case COL_FILENAME :
			name ="Filename";
			break;
		case COL_TRACK :
			name = "#";
			break;
		case COL_ARTIST :
			name = "Artist";
			break;
		case COL_TITLE :
			name = "Title";
			break;
		case COL_ALBUM :
			name = "Album";
			break;
		}
		
		return name;
	}
	
	public void forceRefresh() {
		fireTableDataChanged();
	}

}
