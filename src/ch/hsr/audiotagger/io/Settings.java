package ch.hsr.audiotagger.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Observable;
import java.util.Properties;

/**
 * Stellt ein Singleton zur Verfügung, welches den Zugriff auf diverse
 * Einstellungen des AudioTaggers ermöglicht.
 * 
 * @author Manuel Alabor
 */
public class Settings extends Observable {

	private static Settings instance;
	private Properties properties = null;
	private boolean loaded = false;
	
	public final static String KEY_CDDB_HOST = "cddb.host";
	public final static String KEY_PROXY_USE = "proxy.use";
	public final static String KEY_PROXY_HOST = "proxy.host";
	public final static String KEY_PROXY_PORT = "proxy.port";
	public final static String KEY_COMMON_TRY_REMOVE_WRITEONLY = "common.removewriteonly";
	public final static String KEY_COMMON_AUTOSAVE_WHEN_HOTKEYS = "common.autosavewhenhotkeys";
	public final static String KEY_COMMON_ENABLE_MAC_DETECTION = "common.enablemacdetection";
	public final static String KEY_COMMON_CHECK_FOR_UPDATES = "common.checkupdates";
	
	private final static String SETTINGS_FILE = "settings.properties";
	
	// Singleton ---------------------------------------------------------------
	public static Settings getInstance() {
		if(instance == null) instance = new Settings();
		return instance;
	}
	
	// Konstruktor -------------------------------------------------------------
	private Settings() {
		/* Laden: */
		properties = new Properties();
		loadSettings();
	}
	
	
	// Filezugriff -------------------------------------------------------------
	/**
	 * Lädt die Einstellungen aus dem SETTINGS_FILE.
	 */
	private void loadSettings() {
		try {
			/* Laden: */
			if(new File(SETTINGS_FILE).exists()) {
				properties.load(new FileInputStream(SETTINGS_FILE));
				setChanged();
			}

			/* Standardwerte setzen, wenn nštig: */
			if(properties.get(KEY_PROXY_USE) == null) setBoolean(KEY_PROXY_USE, Boolean.FALSE);
			if(properties.get(KEY_COMMON_AUTOSAVE_WHEN_HOTKEYS) == null) setBoolean(KEY_COMMON_AUTOSAVE_WHEN_HOTKEYS, Boolean.FALSE);
			if(properties.get(KEY_COMMON_TRY_REMOVE_WRITEONLY) == null) setBoolean(KEY_COMMON_TRY_REMOVE_WRITEONLY, Boolean.FALSE);
			if(properties.get(KEY_COMMON_ENABLE_MAC_DETECTION) == null) setBoolean(KEY_COMMON_ENABLE_MAC_DETECTION, Boolean.FALSE);
			if(properties.get(KEY_COMMON_CHECK_FOR_UPDATES) == null) setBoolean(KEY_COMMON_CHECK_FOR_UPDATES, Boolean.FALSE);
			
			loaded = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Speichert die Einstellungen ins SETTINGS_FILE.
	 */
	public void storeSettings() {
		// Durch abfragen von loaded wird sichergestellt, dass ein bestehendes
		// config-File nicht mit leeren Werten Ÿberschrieben wird.
		if(loaded) {
			try {
				OutputStream os = new FileOutputStream(SETTINGS_FILE);
				properties.store(os, "AudioTagger Settings");
				os.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	// Zugriff -----------------------------------------------------------------
	public Object get(Object key) {
		return properties.get(key);
	}
	
	public String getString(String key) {
		return properties.getProperty(key);
	}
	
	public boolean getBoolean(String key) {
		Object value = properties.get(key);
		boolean bool = Boolean.parseBoolean(value.toString());
		return bool;
	}
	
	public int getInt(String key) {
		Object value = properties.get(key);
		int intValue = 0;
		if(value != null) intValue = Integer.parseInt(value.toString());
		
		return intValue;
	}
	
	public void set(Object key, Object value) {
		properties.put(key, value);
		setChanged();
		
		if(loaded) notifyObservers(key);  // Observers benachrichtigen
	}
	
	public void setString(String key, String value) {
		this.set(key, value);
	}
	
	public void setBoolean(String key, boolean value) {
		this.set(key, Boolean.toString(value));
	}
	
	public void setInt(String key, int value) {
		this.set(key, Integer.toString(value));
	}
	
}