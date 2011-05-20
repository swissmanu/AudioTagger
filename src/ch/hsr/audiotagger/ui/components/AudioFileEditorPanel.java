package ch.hsr.audiotagger.ui.components;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;
import javax.swing.GroupLayout.Alignment;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagFieldKey;
import org.jaudiotagger.tag.datatype.Artwork;

import ch.hsr.audiotagger.ui.windows.WaitDialog;

/**
 * Erstellt aus einem {@link JPanel} einen Editor mit allen nötigen Feldern, um
 * die {@link Tag}'s eines oder mehreren {@link AudioFile}(s) bearbeiten zu
 * können.<br/>
 * 
 * @author Manuel Alabor
 */
public class AudioFileEditorPanel extends JPanel {

	private ArrayList<AudioFile> audioFiles;
	private final JComboBox comTitle = new JComboBox(new EditorComboBoxModel());
	private final JComboBox comArtist = new JComboBox(new EditorComboBoxModel());
	private final JComboBox comAlbum = new JComboBox(new EditorComboBoxModel());
	private final JTextField txtTracknumber = new JTextField();
	private final JTextField txtTotalTracks = new JTextField();
	private final JTextField txtDiscnumber = new JTextField();
	private final JTextField txtTotalDiscs = new JTextField();
	private final JTextField txtYear = new JTextField();
	private final JComboBox comGenre = new JComboBox(new EditorComboBoxModel(GENRES));
	private final ArtworkComponent imgArtwork = new ArtworkComponent();
	
	private boolean editorChanged = false;
	private boolean watchComponentsForChange = false;
	
	private static final long serialVersionUID = -8595042984062789079L;
	private static final String[] GENRES = { "ClassicRock", "Country", "Dance",
			"Disco", "Funk", "Grunge", "Hip-Hop", "Jazz", "Metal", "New Age",
			"Oldies", "Other", "Pop", "R&B", "Rap", "Reggae", "Rock", "Techno",
			"Industrial", "Alternative", "Ska", "Death Metal", "Pranks",
			"Soundtrack", "Euro-Techno", "Ambient", "Trip-Hop", "Vocal",
			"Jazz+Funk", "Fusion", "Trance", "Classical", "Instrumental",
			"Acid", "House", "Game", "Sound Clip", "Gospel", "Noise",
			"AlternRock", "Bass", "Soul", "Punk", "Space", "Meditative",
			"Instrumental Pop", "Instrumental Rock", "Ethnic", "Gothic",
			"Darkwave", "Techno-Industrial", "Electronic", "Pop-Folk",
			"Eurodance", "Dream", "Southern Rock", "Comedy", "Cult", "Gangsta",
			"Top 40", "Christian Rap", "Pop/Funk", "Jungle", "Native American",
			"Cabaret", "New Wave", "Psychadelic", "Rave", "Showtunes",
			"Trailer", "Lo-Fi", "Tribal", "Acid Punk", "Acid Jazz", "Polka",
			"Retro", "Musical", "Rock & Roll", "Hard Rock", "Folk",
			"Folk-Rock", "National Folk", "Swing", "Fast Fusion", "Bebob",
			"Latin", "Revival", "Celtic", "Bluegrass", "Avantgarde",
			"Gothic Rock", "Progressive Rock", "Psychedelic Rock",
			"Symphonic Rock", "Slow Rock", "Big Band", "Chorus",
			"Easy Listening", "Acoustic", "Humour", "Speech", "Chanson",
			"Opera", "Chamber Music", "Sonata", "Symphony", "Booty Bass",
			"Primus", "Porn Groove", "Satire", "Slow Jam", "Club", "Tango",
			"Samba", "Folklore", "Ballad", "Power Ballad", "Rhythmic Soul",
			"Freestyle", "Duet", "Punk Rock", "Drum Solo", "Acapella",
			"Euro-House", "Dance Hall", "Goa", "Drum & Bass", "Club-House",
			"Hardcore", "Terror", "Indie", "BritPop", "Negerunk", "Polsk Punk",
			"Beatv", "Christian Gangsta Rap", "Heavy Metal", "Black Metal",
			"Crossover", "Contemporary Christian", "Christian Rock",
			"Merengue", "Salsa", "Trash Metal", "Anime", "JPop", "Synthpop" };
	
	public AudioFileEditorPanel() {
		buildGui();
	}
	
	// GUI-Erstellung ----------------------------------------------------------
	private void buildGui() {
		EditorChangeListener editorChangeListener = new EditorChangeListener();
		
		/* Components: */
		comTitle.setEditable(true);
		comTitle.setPrototypeDisplayValue("");
		comArtist.setEditable(true);
		comArtist.setPrototypeDisplayValue("");
		comAlbum.setEditable(true);
		comAlbum.setPrototypeDisplayValue("");
		comGenre.setEditable(true);
//		txtTotalTracks.setEnabled(false);
		txtDiscnumber.setEnabled(false);
		txtTotalDiscs.setEnabled(false);
		
		addChangeListenerToComboBox(comTitle, editorChangeListener);
		addChangeListenerToComboBox(comArtist, editorChangeListener);
		addChangeListenerToComboBox(comAlbum, editorChangeListener);
		addChangeListenerToComboBox(comGenre, editorChangeListener);
		txtTracknumber.getDocument().addDocumentListener(editorChangeListener);
		
		/* Layout: */
		// Vorbereiten:
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		
		// Labels:
		JLabel lblTitle = new JLabel("Title");
		JLabel lblArtist = new JLabel("Artist");
		JLabel lblAlbum = new JLabel("Album");
		JLabel lblTracknumber = new JLabel("Track");
		JLabel lblTotalTracks = new JLabel("of");
		JLabel lblDiscnumber = new JLabel("Disc");
		JLabel lblTotalDiscs = new JLabel("of");
		JLabel lblYear = new JLabel("Year");
		JLabel lblGenre = new JLabel("Genre");
		JLabel lblCover = new JLabel("Artwork");
		
		// Horizontales Layout:
		GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
		hGroup.addGroup(layout.createParallelGroup().
				addComponent(lblTitle).
				addComponent(lblArtist).
				addComponent(lblAlbum).
				addComponent(lblTracknumber).
				addComponent(lblDiscnumber).
				addComponent(lblYear).
				addComponent(lblCover));
		hGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
		hGroup.addGroup(layout.createParallelGroup().
				addComponent(comTitle, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE).addComponent(comArtist).addComponent(comAlbum)
				.addGroup(layout.createSequentialGroup().
						addComponent(txtTracknumber, 30, 30, 30).
						addGap(5).
						addComponent(lblTotalTracks).addGap(5).addComponent(txtTotalTracks, 30,30,30)
						)
				.addGroup(layout.createSequentialGroup().
						addComponent(txtDiscnumber, 30,30,30).
						addGap(5).
						addComponent(lblTotalDiscs).addGap(5).addComponent(txtTotalDiscs, 30,30,30)
						)
				.addGroup(layout.createSequentialGroup().
						addComponent(txtYear, 50,50,50).
						addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).
						addComponent(lblGenre).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(comGenre))
				.addComponent(imgArtwork, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		);
		
		layout.setHorizontalGroup(hGroup);
		
		
		// Vertikales Layout:
		GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
		vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).
				addComponent(lblTitle).addComponent(comTitle));
		vGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
		vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).
				addComponent(lblArtist).addComponent(comArtist));
		vGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
		vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).
				addComponent(lblAlbum).addComponent(comAlbum));
		vGroup.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);
		vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).
				addComponent(lblTracknumber).addComponent(txtTracknumber).
				addComponent(lblTotalTracks).addComponent(txtTotalTracks));
		vGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
		vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).
				addComponent(lblDiscnumber).addComponent(txtDiscnumber).
				addComponent(lblTotalDiscs).addComponent(txtTotalDiscs));
		vGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
		vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).
				addComponent(lblYear).addComponent(txtYear).
				addComponent(lblGenre).addComponent(comGenre));
		vGroup.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED);
		vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).
				addComponent(lblCover).addComponent(imgArtwork, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));
		layout.setVerticalGroup(vGroup);
	}
	
	// Tags laden --------------------------------------------------------------
	public void editTags(ArrayList<AudioFile> audioFiles) {
		/* Änderungen vorhanden? */
		// Sicherstellen, dass keine Änderungen verloren gehen.
		if(editorChanged) {
			int result = JOptionPane.showConfirmDialog(
					SwingUtilities.windowForComponent(this),
					"You have modified the currently selected audiofile(s)!\n" +
					"Would you like to save these changes?",
					"Save changes",
					JOptionPane.YES_NO_OPTION);
			
			switch(result) {
			case JOptionPane.YES_OPTION :
				saveTags();
				break;
			}
		}
		
		/* Neue Files in den Editor laden: */
		this.audioFiles = audioFiles;
		updateEditorControls();
	}
	
	public void editTag(AudioFile audioFile) {
		ArrayList<AudioFile> audioFiles = new ArrayList<AudioFile>();
		audioFiles.add(audioFile);
		
		editTags(audioFiles);
	}
	
	/**
	 * Nimmt die aktuell in <code>tags</code> vorhandenen Metatags, und befüllt
	 * die Editorfelder dementsprechend, dass diese bearbeitet werden könne.<br/>
	 * Hierbei ist das bearbeiten eines einzelnen {@link Tag}s als auch das
	 * bearbeiten von mehreren möglich.
	 * 
	 * @see #editTag(Tag)
	 * @see #editTags(ArrayList)
	 */
	private void updateEditorControls() {
		watchComponentsForChange = false;
		
		/* Felder zur¸cksetzen: */
		editorChanged = false;
		((EditorComboBoxModel)comTitle.getModel()).reset();
		((EditorComboBoxModel)comArtist.getModel()).reset();
		((EditorComboBoxModel)comAlbum.getModel()).reset();
		((EditorComboBoxModel)comGenre.getModel()).reset();
		txtTracknumber.setText("");
		txtTotalTracks.setText("");
		txtDiscnumber.setText("");
		txtTotalDiscs.setText("");
		txtYear.setText("");
		imgArtwork.setArtwork(null);
		imgArtwork.setChanged(false);
		
		if(audioFiles == null || audioFiles.size() == 0) {
			setEditorEnabled(false);
		} else {
			setEditorEnabled(true);
			
			if(audioFiles.size() == 1) {
				// SingleFile-Editing
				AudioFile audioFile = audioFiles.get(0);
				Tag tag = audioFile.getTag();
				
				/* Felder aktualisieren: */
				comTitle.addItem(tag.getFirstTitle());
				comTitle.setSelectedIndex(comTitle.getItemCount()-1);
				comArtist.addItem(tag.getFirstArtist());
				comArtist.setSelectedIndex(comArtist.getItemCount()-1);
				comAlbum.addItem(tag.getFirstAlbum());
				comAlbum.setSelectedIndex(comAlbum.getItemCount()-1);
				
				
				String[] numberBuffer = splitNumberData(tag.getFirstTrack());
				txtTracknumber.setText(numberBuffer[0]);
				txtTracknumber.setEnabled(true);
				txtTotalTracks.setText(numberBuffer[1]);
				numberBuffer = splitNumberData(tag.getFirst(TagFieldKey.DISC_NO));
				txtDiscnumber.setText(numberBuffer[0]);
				txtTotalDiscs.setText(numberBuffer[1]);
				
				txtYear.setText(tag.getFirstYear());
				
				EditorComboBoxModel model = (EditorComboBoxModel)comGenre.getModel();
				String genre = tag.getFirstGenre();
				model.addIfNotExists(genre);
				comGenre.setSelectedItem(genre);
				
				try {
					Artwork artwork = tag.getFirstArtwork();
					BufferedImage cover = null;
					if(artwork != null) cover = ImageIO.read(new ByteArrayInputStream(artwork.getBinaryData()));
					imgArtwork.setArtwork(cover);
					imgArtwork.setChanged(false);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			} else {
				/* MutliFile-Editing: */
				// ComboBoxes befüllen:
				// Wenn mehrere Audiodateien gewählt sind, ist es meistens auch der
				// Fall, dass beispielsweise die verschiedenen Files einen verschiedenen
				// Album-Tag haben. Damit der User nicht "von null" ein Album eingeben
				// muss, hat er die Möglichkeit, zwischen den bereits vorhandenen Varianten
				// eine auszusuchen.
				EditorComboBoxModel model = null;
				
				String totalTracks = "-1";
				String discnumber = "-1";
				String totalDiscs = "-1";
				for (AudioFile audioFile : audioFiles) {
					Tag tag = audioFile.getTag();
					
					// Title:
					model = (EditorComboBoxModel)comTitle.getModel();
					model.addIfNotExists(tag.getFirstTitle());
					
					// Artists:
					model = (EditorComboBoxModel)comArtist.getModel();
					model.addIfNotExists(tag.getFirstArtist());
					
					// Albums:
					model = (EditorComboBoxModel)comAlbum.getModel();
					model.addIfNotExists(tag.getFirstAlbum());
					
					// Genre.
					model = (EditorComboBoxModel)comGenre.getModel();
					model.addIfNotExists(tag.getFirstGenre());
					
					// Total Tracks:
					String[] splitBuffer = splitNumberData(tag.getFirstTrack());
					if(totalTracks.equals("-1") || totalTracks.equals(splitBuffer[1])) {
						totalTracks = splitBuffer[1];
					} else {
						totalTracks = "";
					}

					// Discs:
					splitBuffer = splitNumberData(tag.getFirst(TagFieldKey.DISC_NO));
					if(discnumber.equals("-1") || discnumber.equals(splitBuffer[0])) {
						discnumber = splitBuffer[0];
					} else {
						discnumber = "";
					}
					
					// Total Discs:
					if(totalDiscs.equals("-1") || totalDiscs.equals(splitBuffer[1])) {
						totalDiscs = splitBuffer[1];
					} else {
						totalDiscs = "";
					}
					
				}

				txtTracknumber.setText("");
				txtTracknumber.setEnabled(false);
				txtTotalTracks.setText(totalTracks);
				txtDiscnumber.setText(discnumber);
				txtTotalDiscs.setText(totalDiscs);
			}			
		}
		
		watchComponentsForChange = true;
	}
	
	private void setEditorEnabled(boolean enabled) {
		comTitle.setEnabled(enabled);
		comArtist.setEnabled(enabled);
		comAlbum.setEnabled(enabled);
		txtTracknumber.setEnabled(enabled);
		txtTotalTracks.setEnabled(enabled);
//		txtDiscnumber.setEnabled(enabled);
//		txtTotalDiscs.setEnabled(enabled);
		txtYear.setEnabled(enabled);
		comGenre.setEnabled(enabled);
		imgArtwork.setEnabled(enabled);
	}
	
	/**
	 * Versucht einen {@link String} nach dem Zeichen "/" zu teilen und gibt
	 * die zwei Elemente als String-Array zurück.<br/>
	 * Konnte nicht in zwei Teile geteilt werden, werden die entsprechenden
	 * Array-Elemente als leere Strings zurückgegeben.<br/>
	 * <br/>
	 * Wird benötigt, um Track- und Discnumber aufzutrennen.
	 * 
	 * @param data
	 * @return
	 */
	private String[] splitNumberData(String data) {
		String[] numbers = new String[2];
		numbers[0] = "";
		numbers[1] = "";
		
		if(data != null) {
			String[] splitted = data.split("/");
			if(splitted.length == 1) {
				numbers[0] = data;
			} else if(splitted.length == 2) {
				numbers[0] = splitted[0];
				numbers[1] = splitted[1];
			}
		}
		
		return numbers;
	}
	
	// Tags schreiben ----------------------------------------------------------
	/**
	 * Speichert die Informationen aus dem {@link AudioFileEditorPanel} in die
	 * entsprechenden {@link AudioFile}'s zurück.
	 */
	public void saveTags() {
		saveValueFromEditedComboBox();
		
		Runnable saver = new Runnable() {
			public void run() {
				if(audioFiles != null && audioFiles.size() > 0) {
					
					ArrayList<AudioFile> wrongFiles = new ArrayList<AudioFile>();
					
					for(AudioFile audioFile: audioFiles) {
						try {
							Tag tag = audioFile.getTag();

							// Common-Tags setzen:
							handleEditorValue(tag, TagFieldKey.TITLE, comTitle.getSelectedItem());
							handleEditorValue(tag, TagFieldKey.ARTIST, comArtist.getSelectedItem());
							handleEditorValue(tag, TagFieldKey.ALBUM, comAlbum.getSelectedItem());
							handleEditorValue(tag, TagFieldKey.YEAR, txtYear.getText());
							handleEditorValue(tag, TagFieldKey.GENRE, comGenre.getSelectedItem());

							// Wenn mehrere AudioFiles geschrieben werden sollen,
							// darf nur die Totale Anzahl Tracks geschrieben werden.
							// Bei einem einzelnen AudioFile beides.
							if(audioFiles.size() == 1) {
								// Tracknumber:
								String tracknumber = txtTracknumber.getText();
								String totalTracks = txtTotalTracks.getText();
								if(!totalTracks.equals("")) tracknumber = tracknumber + "/" + totalTracks;
								tag.setTrack(tracknumber);
								
								// Discs:
//								String discnumber = txtDiscnumber.getText();
//								String totalDiscs = txtTotalDiscs.getText();
//								if(!totalDiscs.equals("")) discnumber += "/" + totalDiscs;
//								tag.set(tag.createTagField(TagFieldKey.DISC_NO, discnumber));
								
							} else {
								// Tracknumber:
								String totalTracks = txtTotalTracks.getText();
								if(!totalTracks.equals("")) {
									String[] currentTracknumber = splitNumberData(tag.getFirstTrack());
									String tracknumber = currentTracknumber[0] + "/" + totalTracks;
									tag.setTrack(tracknumber);
								}
								
								// Discs:
//								String discnumber = txtDiscnumber.getText();
//								String totalDiscs = txtTotalDiscs.getText();
//								String[] currentDiscnumber = splitNumberData(tag.getFirst(TagFieldKey.DISC_NO));
//								
//								if(discnumber.equals("")) discnumber = currentDiscnumber[0];
//								if(!totalDiscs.equals("")) discnumber += "/" + totalDiscs;
//								tag.set(tag.createTagField(TagFieldKey.DISC_NO, discnumber));
							}
							

							// Artwork setzen (falls geändert):
							if(imgArtwork.hasChanged()) {
								tag.deleteTagField(TagFieldKey.COVER_ART);

								BufferedImage image = imgArtwork.getArtwork();
								if(image != null) {
									Artwork artwork = new Artwork();
									ByteArrayOutputStream out = new ByteArrayOutputStream();
									ImageIO.write(image, "jpeg", out);
									artwork.setBinaryData(out.toByteArray());
									artwork.setMimeType("image/jpeg");
									tag.createAndSetArtworkField(artwork);
								}
							}

							/* Speichern: */
							audioFile.commit();
							editorChanged = false;
						} catch(Exception e) {
							wrongFiles.add(audioFile);
						}
					}

					/* Fehler? */
					if(wrongFiles.size() > 0) {
						StringBuilder wrongFilesString = new StringBuilder();
						for(AudioFile wrongFile: wrongFiles) wrongFilesString.append(" - " + wrongFile.getFile().getName() + "\n");
						
						JOptionPane.showMessageDialog(
								SwingUtilities.getWindowAncestor(AudioFileEditorPanel.this),
								"AudioTagger was not able to save the tags for the " +
								"following audio files:\n" + wrongFilesString.toString(),
								"Tags not saved",
								JOptionPane.ERROR_MESSAGE);
					}
					
				}

			}
		};
		
		/* WaitDialog: */
		// Alles in einen WaitDialog verpacken & anzeigen/durchführen.
		WaitDialog waitDialog = new WaitDialog(
				SwingUtilities.getWindowAncestor(this),
				"Save tags",
				"Save tags. Please wait...",
				saver);
		waitDialog.setVisible(true);
	}
	
	/**
	 * Bestimmt, ob ein Tag gesetzt, gelöscht oder gar nicht angeührt werden
	 * soll.
	 * 
	 * @param tag
	 * @param tagFieldKey
	 * @param newValue
	 * @return
	 * @see #saveTags()
	 */
	private boolean handleEditorValue(Tag tag, TagFieldKey tagFieldKey, Object newValue) {
		boolean successful = true;
		
		if(newValue != EditorComboBoxModel.ENTRY_KEEP) {
			if(newValue == EditorComboBoxModel.ENTRY_REMOVE || newValue.toString().equals("")) {
				// Tag löschen:
				tag.deleteTagField(tagFieldKey);
			} else {
				try {
					// Tag erstellen/updaten:
					tag.set(tag.createTagField(tagFieldKey, newValue.toString()));
				} catch (Exception e) {
					successful = false;
					e.printStackTrace();
				}
			}
		}
		
		return successful;
	}
	
	
	// Zugriff -----------------------------------------------------------------
	/**
	 * Ermittelt, ob Werte im Editor seit dem letzten Laden von Audiofiles
	 * verändert wurden.
	 * 
	 * @return true/false
	 */
	public boolean hasEditorChanged() {
		return editorChanged;
	}
	
	/**
	 * Setzt für die momentan geladenen {@link AudioFile}'s das Artwork.
	 * 
	 * @param artwork
	 */
	public void setArtwork(BufferedImage artwork) {
		imgArtwork.setArtwork(artwork);
		editorChanged = true;
	}
	
	public void setFocusOnFirstComponent() {
		comTitle.requestFocus();
	}
	
	
	// TagEditorComboBoxModel --------------------------------------------------
	private class EditorComboBoxModel extends DefaultComboBoxModel {

		private final static String ENTRY_KEEP = "< keep >";
		private final static String ENTRY_REMOVE = "< remove >";
		private final static long serialVersionUID = -1995368345282311697L;
		private String[] initialElements = null;
		
		public EditorComboBoxModel() {
			reset();
		}
		
		public EditorComboBoxModel(String[] initialElements) {
			this.initialElements = initialElements;
			reset();
		}
		
		public void reset() {
			removeAllElements();
			addElement(ENTRY_KEEP);
			addElement(ENTRY_REMOVE);
			
			if(initialElements != null) {
				for(String entry: initialElements) {
					addElement(entry);
				}
			}
		}
		
		/**
		 * Prüft, ob ein Item bereits in diesem Model vorhanden ist. Wenn nicht,
		 * wird dieses dem Model hinzugefügt.
		 * 
		 * @param item
		 * @return true, wenn Item bereits vorhanden, false, wenn nicht vorhanden & hinzugefügt
		 */
		public boolean addIfNotExists(String item) {
			boolean exists = false;
			if(item.trim().equals("")) return true;
			
			for (int i = 0, l = getSize(); i<l; i++) {
				if(getElementAt(i).equals(item)) {
					exists = true;
					break;
				}
			}
			
			if(!exists) addElement(item);
			
			return exists;
		}
		
	}
	
	// ChangeListener ----------------------------------------------------------
	/**
	 * Diese Hilfsklasse implementiert diverse Listener-Interfaces. Eine Instanz
	 * davon wird den Editor-Components des {@link AudioFileEditorPanel} zugewiesen.
	 * Der Listener kann somit feststellen, ob Werte des Editors geändert wurden
	 * und setzt dementsprechend {@link AudioFileEditorPanel#editorChanged} auf
	 * true, damit die vorgenommene Änderung festgestellt werden kann.<br/>
	 * ("Would you like to save your changes?"-Meldung).<br/>
	 * <br/>
	 * Ist die {@link AudioFileEditorPanel#watchComponentsForChange}-Variabel auf
	 * <code>false</code> gesetzt, werden keine Changes überwacht. Dies ist z.B.
	 * dann nützlich, wenn vom Code aus Werte in den Editor-Components angepasst
	 * werden.
	 * 
	 * @see AudioFileEditorPanel#editorChanged
	 * @see AudioFileEditorPanel#watchComponentsForChange
	 * @see AudioFileEditorPanel#updateEditorControls()
	 */
	private class EditorChangeListener implements DocumentListener, ItemListener {
		
		private void changedEditor() {
			if(watchComponentsForChange) {
				editorChanged = true;
				System.out.println("cahgned");
			}
		}
		
		@Override
		public void changedUpdate(DocumentEvent e) { changedEditor(); }
		@Override
		public void insertUpdate(DocumentEvent e) { changedEditor(); }
		@Override
		public void removeUpdate(DocumentEvent e) { changedEditor(); }
		@Override
		public void itemStateChanged(ItemEvent e) { if(e.getStateChange() == ItemEvent.SELECTED) changedEditor(); }
	}
	
	/**
	 * F¸gt einer {@link JComboBox} an allen nˆtigen Stellen einen {@link EditorChangeListener}
	 * hinzu.
	 * 
	 * @param comboBox
	 * @param editorChangeListener
	 */
	private void addChangeListenerToComboBox(JComboBox comboBox, final EditorChangeListener editorChangeListener) {
		BasicComboBoxEditor editor = (BasicComboBoxEditor)comboBox.getEditor();
		JTextField textField = (JTextField)editor.getEditorComponent();
		textField.getDocument().addDocumentListener(editorChangeListener);
		comboBox.addItemListener(editorChangeListener);
	}

	/**
	 * Folgendes Szenario. Der User gibt einen Wert in eine {@link JComboBox} ein,
	 * und bevor er auf eine andere {@link Component} klickt, klickt er in der
	 * Toolbar auf "Speichern".<br/>
	 * Der eingegebene Wert ist in diesem Moment noch nicht als <em>selectedItem</em>
	 * in der {@link JComboBox} festgelegt worden.<br/>
	 * Aus diesem Grund prüft diese Methode, ob momentan eine {@link JComboBox}
	 * den Fokus hat, und speichert ggf. den eingegebenen Wert dieser als
	 * <em>selectedItem</em><br/>
	 * <br/>
	 * Würde dies nicht gemacht, ginge der eingegebene Wert aus dem Szenario oben
	 * verloren.
	 * 
	 * @see #editTags(ArrayList)
	 */
	private void saveValueFromEditedComboBox() {
		Component currentFocusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		Component combo = SwingUtilities.getAncestorOfClass(JComboBox.class, currentFocusOwner);
		
		if(combo != null && combo instanceof JComboBox) {
			JComboBox comboBox = (JComboBox)combo;
			comboBox.setSelectedItem(comboBox.getEditor().getItem());
		}
	}
	
}
