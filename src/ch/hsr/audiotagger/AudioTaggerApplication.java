/*
 *  This file is part of AudioTagger.
 *  Find more information about the official release versions under
 *  <http://www.msites.net/audiotagger/>
 *
 *  AudioTagger is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  AudioTagger is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with AudioTagger.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.hsr.audiotagger;

import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ch.hsr.audiotagger.io.Settings;
import ch.hsr.audiotagger.ui.GUIImageManager;
import ch.hsr.audiotagger.ui.windows.AudioTaggerFrame;
import ch.hsr.audiotagger.ui.windows.platformspecific.MacAudioTaggerFrame;
import ch.hsr.audiotagger.ui.windows.platformspecific.UniversalAudioTaggerFrame;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.jgoodies.looks.plastic.theme.SkyBlue;

public class AudioTaggerApplication {
	
	private final static String VERSION = "0.5.1";
	
	public static void main(String[] args) throws Exception {
		/* Vorbereiten: */
		initSettings();
		boolean isMac = false;
		if(Settings.getInstance().getBoolean(Settings.KEY_COMMON_ENABLE_MAC_DETECTION)) {
			isMac = isMac();
		}
		
		/* Look&Feel: */
		if(!isMac) {
	        try {
	        	PlasticLookAndFeel.setPlasticTheme(new SkyBlue());
	            UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
	        } catch (UnsupportedLookAndFeelException e) {
	            e.printStackTrace();
	        }
	        
		} else {
			try {
			    UIManager.setLookAndFeel(
			        UIManager.getSystemLookAndFeelClassName());
			} catch (UnsupportedLookAndFeelException e) {
			  e.printStackTrace();
			}
		}
        GUIImageManager.loadImageManager("/ch/hsr/audiotagger/ui/res/");
        
        /* Platformspezifisches AudioTaggerFrame instanzieren und anzeigen: */
		AudioTaggerFrame frame = null;
		if(isMac) frame = new MacAudioTaggerFrame();
		else frame = new UniversalAudioTaggerFrame();
		
		frame.setVisible(true);
	}
	
	
	public static boolean isMac() {
		boolean isMac = false;
		
		String osName = System.getProperty("os.name");
		if(osName != null && osName.startsWith("Mac")) isMac = true;
		
		return isMac;
	}
	
	private static void initSettings() {
		Settings settings = Settings.getInstance();
		
		settings.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				Settings settings = (Settings)o;
				String key = arg.toString();
		        
				if(key.equals(Settings.KEY_PROXY_USE) || key.equals(Settings.KEY_PROXY_HOST) || key.equals(Settings.KEY_PROXY_PORT)) {
					Properties systemSettings = System.getProperties();		
					
					if(settings.getBoolean(Settings.KEY_PROXY_USE)) {
				        String host = settings.getString(Settings.KEY_PROXY_HOST);
				        String port = settings.getString(Settings.KEY_PROXY_PORT);
				        if(host.equals("")) {
				        	port = "";
				        } else {
				        	if(port.equals("")) port = "80";
				        }
				        
						systemSettings.put("http.proxyHost", host);
				        systemSettings.put("http.proxyPort", port);
					} else {
				        systemSettings.put("http.proxyHost", "");
				        systemSettings.put("http.proxyPort", "");
					}
					
			        System.setProperties(systemSettings);
				}
			}
		});
		settings.notifyObservers(Settings.KEY_PROXY_USE);  // Zum ersten mal gleich Ÿbernehmen
	}
	
	public static String getVersion() {
		return VERSION;
	}

}
