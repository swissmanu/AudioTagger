/*
 * Created on 31.05.2004
 */
package ch.hsr.audiotagger.ui;

import java.awt.Canvas;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Der GUIImageManager verwaltet alle GUI-spezifischen Bilder.<br>
 * Er stellt sicher dass kein Bild zweimal geladen wird, indem er intern einen
 * Cache verwaltet.<br/>
 * <br/>
 * Der {@link GUIImageManager} implementiert (zum gršssten Teil) das Singleton-Design-
 * Pattern. Per <code>getInstance</code> wird die aktuelle Instanz angefordert.<br/>
 * Bevor dies jedoch getan werden kann, muss der {@link GUIImageManager} per
 * <code>loadImageManager(String)</code> initialisiert werden. Mit dem Parameter
 * "homePath" wird dem {@link GUIImageManager} das Verzeichnis angegeben, in welchem
 * sich alle Bilder der Applikation befinden.<br/>
 * Somit kann beim codieren spŠter lediglich der Dateiname des gewŸnschten Bildes
 * angegeben werden, und es ist nicht nštig, den gesamten, absoluten Pfad anzugeben.<br/>
 * <br/>
 * <br/>
 * Version 5.0: Arbeitet jetzt mit CompatibleImages.<br/>
 * Version 5.2: Auf BufferedImages umgestellt; getURL() kann Fehler printen<br>
 * <br/>
 * Beispiele:<br/><code>
 *  GUIImageManager.loadImageManager("/gui/core/images/");<br/>
 *  Image relativeImage = GUIImageManager.getInstance().getImage("logo.png");<br/>
 *  Image absolutImage = GUIImageManager.getInstance().getImage("/manu/gui/components/icon.png"); 
 * </code>
 * 
 * @author Manuel Alabor
 * @version 5.2
 */
public class GUIImageManager {

    private static GUIImageManager imageManager = null;
    
    private String homePath;    
	private Hashtable<String, BufferedImage> imageCache;
	
	
	// Singleton-Implementierung -----------------------------------------------
    /**
     * Initialisiert den {@link GUIImageManager}.
     * 
     * @param homePath
     */
	public static void loadImageManager(String homePath) {
        imageManager = new GUIImageManager(homePath);
    }
    
	/**
	 * Liefert die interne {@link GUIImageManager}-Instanz.<br/>
	 * Zuerst {@link #loadImageManager(String)} aufrufen! Gibt ansonsten <code>null</code>
	 * zurück.
	 * 
	 * @return {@link GUIImageManager}-Instanz
	 */
	public static GUIImageManager getInstance() {
	    return imageManager;
	}
	
	
	// Konstruktoren -----------------------------------------------------------
	/**
	 * Standartkonstruktor
	 * 
	 * @param homePath Pfad des Ordners, in welchem sich alle Bilder der Applikation befinden
	 */
	private GUIImageManager(String homePath) {
		/* homePath prüfen: */
	    // Sicherstellen, dass sich am Ende von homePath ein "/" befindet:
		if(homePath.charAt(homePath.length()-1) != '/') {
		    homePath = homePath + '/';
		}
	    
		/* Setzen: */
	    this.imageCache = new Hashtable<String, BufferedImage>();
		this.homePath = homePath;
	}
	
	
	// Methoden zum Laden von Bildern ------------------------------------------
	/**
	 * LŠdt ein Bild und liefert dieses zurŸck.<br>
	 * Wurde ein Bild bereits geladen (es befindet sich dann in der Hashtable
	 * images), wird dieses nicht erneut geladen, sondern das entsprechende Bild
	 * wird aus der Hashtable gelesen und zurŸckgegeben.
	 * 
	 * @param imageName
	 * @return Image
	 */
	public BufferedImage getImage(String imageName) {
	    /* Absoluter Pfad?: */
	    // Ist zu Beginn des Bildnamens ein "/" angegeben, so handelt es sich um
	    // einen absoluten Pfad.
	    // Fehlt dieses Zeichen, so wird relativePath dem Bildnamen hinzugefügt.
	    String path = imageName;
	    if(path.charAt(0) != '/') {
	        path = homePath + imageName;  // relativer Pfad
	    }
	    
	    /* Bild laden: */
	    if(imageCache.containsKey(path)) {
			return imageCache.get(path);
		} else {
            BufferedImage image = loadImage(getURL(path));
            
			imageCache.put(path, image);  // Bild speichern
			return image;				 // Bild zurŸckgeben
		}
	}
    
	/**
	 * Benutzt die Methode getImage(...) um ein Bild zu laden/holen.<br>
	 * Das Bild wird jedoch nicht als normales Image-Objekt zurückgegeben, sondern
	 * wird als ImageIcon ausgeliefert.<br>
	 * <br>
	 * ACHTUNG! imageName ist relativ zum Standort von GUIImageManager!
	 * 
	 * @param imageName
	 * @return ImageIcon
	 */
	public ImageIcon getImageIcon(String imageName) {
		Image image = getImage(imageName);
		ImageIcon icon;
		
		try {
		    icon = new ImageIcon(image);
		} catch(Exception e) {
		    icon = null;
		    e.printStackTrace();
		}
		
		return icon;
	}
	
	/**
	 * Liefert ein Bild.<br/>
	 * Im Gegensatz zu den anderen getImage*-Methoden speichert diese Version
	 * hier das Bild nicht in den Cache.
	 * 
	 * @param imageName
	 * @return
	 */
	public BufferedImage getUnchachedImage(String imageName) {
	    String path = imageName;
	    if(path.charAt(0) != '/') {
	        path = homePath + imageName;  // relativer Pfad
	    }
	    
	    if(imageCache.containsKey(path)) {
	        return imageCache.get(path);
	    } else {
	        BufferedImage image = loadImage(getURL(path));
	        return image;
	    }
	}
	
	public BufferedImage getImageFromUrl(URL url) {
		if(imageCache.containsKey(url.toString())) {
			return imageCache.get(url.toString());
		} else {
			BufferedImage image = loadImage(url);
			return image;
		}
	}
	
	/**
	 * Diese Überladung von getImage ermöglicht während dem Laden des Bildes einen
	 * MediaTracker zu verwenden.<br>
	 * Hiermit wird sichergestellt dass das Bild nicht erst vor dem ersten anzeigen
	 * nachgeladen wird, sondern sofort im Speicher zur Verfügung steht.<br>
	 * <br>
	 * ACHTUNG! imageName ist relativ zum Standort von GUIImageManager!
	 * 
	 * @param imageName
	 * @param boolean Tracker verwenden?
	 * @return Image
	 */
	public Image getImage(String imageName, boolean useTracker) {
	    /* Prüfung: */
	    // Wenn das gewünschte Bild bereits geladen ist, den Tracker NIEMALS
	    // benutzen (auch wenn dies aufgrund des übergebenen Parameters ge-
	    // wünscht sein sollte).
	    if(imageCache.containsKey(imageName)) {
			useTracker = false;
		}
	    
	    /* Bild holen: */
	    Image image = getImage(imageName);
	    
		/* Tracker: */
		if(useTracker) {
			// Per MediaTracker laden:
			MediaTracker tracker = new MediaTracker(new Canvas());
			tracker.addImage(image, 1);
			try {
				tracker.waitForAll();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		/* Bild zurückgeben: */
		return image;
	}
    
    // Hilfsmethoden -----------------------------------------------------------
	private URL getURL(String imageName) {
        URL url = this.getClass().getResource(imageName);
        if(url == null) System.err.println("GUIImageManager: \"Imagefile [" + imageName + "] not found\"");
        
	    return url;
	}
    
    private BufferedImage loadImage(URL url) {
        BufferedImage tmp = null;
        try {
            tmp = ImageIO.read(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedImage compatibleImage = GUIHelper.toCompatibleImage(tmp);
        
        return compatibleImage;
    }
		
}
