package ch.hsr.audiotagger.io;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Properties;

public class FavoriteFolders extends Observable {

	private static FavoriteFolders instance = null;
	private ArrayList<Favorite> favorites = null;
	private boolean loaded = false;
	
	private final static String FAVORITES_FILE = "favorites.properties";
	private final static String KEY_COUNT = "favorites.count";
	private final static String KEY_PREFIX = "favorites.";
	private final static String KEY_NAME = ".name";
	private final static String KEY_PATH = ".path";
	
	public static FavoriteFolders getInstance() {
		if(instance == null) instance = new FavoriteFolders();
		return instance;
	}
	
	private FavoriteFolders() {
		favorites = new ArrayList<Favorite>();
		loadFavorites();
	}
	
	private void loadFavorites() {
		try {
			/* Laden: */
			if(new File(FAVORITES_FILE).exists()) {
				Properties properties = new Properties();
				properties.load(new FileInputStream(FAVORITES_FILE));
				
				
				String count = properties.getProperty(KEY_COUNT);
				if(count != null) {
					int l = Integer.parseInt(count);
					for(int i = 0; i < l; i++) {
						String name = properties.getProperty(KEY_PREFIX + i + KEY_NAME);
						String path = properties.getProperty(KEY_PREFIX + i + KEY_PATH);
						
						if(name != null && path != null) {
							addFavorite(name, path);
						}
					}
				}
				
				loaded = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public int getFavoriteCount() {
		return favorites.size();
	}
	
	public Favorite getFavorite(int index) {
		return favorites.get(index);
	}
	
	public void addFavorite(String name, String path) {
		favorites.add(new Favorite(name, path));
		setChanged();
		
		if(loaded) {
			notifyObservers(name);
		}
	}
	
	// Hilfsklassen ------------------------------------------------------------
	public class Favorite {
		
		private String name;
		private File file;
		
		public Favorite(String name, String path) {
			this.name = name;
			this.file = new File(path);
		}
		
		public String getName() {
			return name;
		}
		
		public File getFile() {
			return file;
		}
		
	}
	
}
