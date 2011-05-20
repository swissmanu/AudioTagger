package ch.hsr.audiotagger.io.artwork;

import java.util.ArrayList;

public abstract class AbstractArtworkProvider {
	
	public abstract String getName();
	
	public abstract ArrayList<String> searchArtwork(String artist) throws Exception;
	
	public abstract ArrayList<String> searchArtwork(String artist, String album) throws Exception;
	
}
