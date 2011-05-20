package ch.hsr.audiotagger.io.artwork;

import java.util.ArrayList;

public class DummyArtworkProvider extends AbstractArtworkProvider {

	@Override
	public String getName() {
		return "Dummy";
	}

	@Override
	public ArrayList<String> searchArtwork(String artist) throws Exception {
		return searchArtwork(artist, null);
	}

	@Override
	public ArrayList<String> searchArtwork(String artist, String album)
			throws Exception {
		
		ArrayList<String> dummies = new ArrayList<String>();
		
		dummies.add("file:///Users/manuelalabor/Pictures/Alte Fotos & Bilder/Bild(47).jpg");
		dummies.add("file:///Users/manuelalabor/Pictures/Alte Fotos & Bilder/Bild(47).jpg");
		dummies.add("file:///Users/manuelalabor/Pictures/Alte Fotos & Bilder/Bild(47).jpg");
		dummies.add("file:///Users/manuelalabor/Pictures/Alte Fotos & Bilder/Bild(47).jpg");
		
		return dummies;
	}

}
