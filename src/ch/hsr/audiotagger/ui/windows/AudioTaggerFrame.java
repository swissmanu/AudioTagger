package ch.hsr.audiotagger.ui.windows;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Observable;
import java.util.Observer;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableRowSorter;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.tag.Tag;

import ch.hsr.audiotagger.AudioTaggerApplication;
import ch.hsr.audiotagger.io.Settings;
import ch.hsr.audiotagger.io.UpdateChecker;
import ch.hsr.audiotagger.io.UpdateChecker.VersionInfo;
import ch.hsr.audiotagger.ui.components.AudioFileEditorPanel;
import ch.hsr.audiotagger.ui.components.BetterJTable;
import ch.hsr.audiotagger.ui.models.AudioFileTableModel;
import ch.hsr.audiotagger.ui.windows.platformspecific.MacAudioTaggerFrame;
import ch.hsr.audiotagger.ui.windows.platformspecific.UniversalAudioTaggerFrame;

/**
 * Klasse mit den Minimal-Komponenten und {@link Action}'s für den AudioTagger.<br/>
 * Enthält den die {@link AudioFileEditorPanel} sowie eine {@link JTable} zur
 * Anzeige von {@link AudioFile}'s.<br/>
 * Zudem sind alle {@link Action}'s in einer {@link Hashtable} verfügbar. Entsprechende
 * Zugriffskeys sind als Konstanten für alle ableitenden Klassen verfügbar.<br/>
 * <br/>
 * <b>ACHTUNG:</b> Die komplette "Montage" des GUI's muss von der ableitenden Klasse
 * übernommen werden. {@link AudioTaggerFrame} bietet lediglich vorbereitete
 * Standardkomponenten und {@link Action}'s an.
 * 
 * @author Manuel Alabor
 * @see UniversalAudioTaggerFrame
 * @see MacAudioTaggerFrame
 */
public abstract class AudioTaggerFrame extends JFrame {

	private File audioFilesFolder = null;
	private int positionInFolderHistory = 0;
	
	protected final static AudioFileEditorPanel pnlAudioFileEditor = new AudioFileEditorPanel();
	protected final static BetterJTable tblAudioFiles = new BetterJTable();
	
	protected final static Hashtable<String, Action> actions = new Hashtable<String, Action>();
	private final static ArrayList<String> folderHistory = new ArrayList<String>(10);
	private final ObservableDelegate observableDelegate = new ObservableDelegate();
	private boolean currentlyLoadingFiles = false;
	
	protected final static String ACTION_QUIT = "quit";
	protected final static String ACTION_REFRESH = "refresh";
	protected final static String ACTION_PREFERENCES = "preferences";
	protected final static String ACTION_CHECK_FOR_UPDATES = "checkforupdates";
	protected final static String ACTION_TAGS_SAVE = "tagsSave";
	protected final static String ACTION_ARTWORK_IMPORT = "artworkImport";
	protected final static String ACTION_ARTWORK_SELECT_LOCAL = "artworkSelectLocal";
	protected final static String ACTION_ARTWORK_REMVOE = "artworkRemove";
	protected final static String ACTION_TAGS_IMPORT = "tagsImport";
	protected final static String ACTION_TAGS_TO_FILENAME = "tagsToFilename";
	protected final static String ACTION_TAGS_EDIT_TRACKNUMBERS = "tagsEditTracknumbers";
	protected final static String ACTION_GOTO_NEXT_FILE = "gotoNextFile";
	protected final static String ACTION_GOTO_PREV_FILE = "gotoPrevFile";
	protected final static String ACTION_GOTO_NEXT_FOLDER = "gotoNextFolder";
	protected final static String ACTION_GOTO_PREV_FOLDER = "gotoPrevFolder";
	protected final static String ACTION_HELP_ABOUT = "helpAbout";
	
	public final static String OBSERVABLE_FILES_CHANGED = "audioFilesLoaded";
	
	private static final long serialVersionUID = -2482322595547765402L;

	public AudioTaggerFrame() {
		super("AudioTagger");
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		initActions();
		customizeActions();
		initComponents();
	}
	
	// Initialisierung ---------------------------------------------------------
	private void initActions() {		
		actions.put(ACTION_QUIT, new QuitAction());
		actions.put(ACTION_TAGS_SAVE, new SaveTagsAction());
		actions.put(ACTION_PREFERENCES, new PreferencesAction());
		actions.put(ACTION_CHECK_FOR_UPDATES, new CheckForUpdatesAction());
		actions.put(ACTION_REFRESH, new RefreshFileTableAction());
		actions.put(ACTION_ARTWORK_IMPORT, new SearchArtworkAction());
		actions.put(ACTION_ARTWORK_SELECT_LOCAL, new SelectLocalArtworkAction());
		actions.put(ACTION_ARTWORK_REMVOE, new RemoveCurrentArtworkAction());
		actions.put(ACTION_TAGS_IMPORT, new ImportTagsAction());
		actions.put(ACTION_TAGS_TO_FILENAME, new TagsToFilenameAction());
		actions.put(ACTION_TAGS_EDIT_TRACKNUMBERS, new TracknumbersAction());
		actions.put(ACTION_GOTO_NEXT_FILE, new GotoInAudioFileTable("Next audio file", GotoInAudioFileTable.DIRECTION_NEXT));
		actions.put(ACTION_GOTO_PREV_FILE, new GotoInAudioFileTable("Previous audio file", GotoInAudioFileTable.DIRECTION_PREV));
		actions.put(ACTION_HELP_ABOUT, new AboutAction());
		actions.put(ACTION_GOTO_NEXT_FOLDER, new GotoInFolderHistory("Next folder", GotoInFolderHistory.DIRECTION_NEXT));
		actions.put(ACTION_GOTO_PREV_FOLDER, new GotoInFolderHistory("Previous folder", GotoInFolderHistory.DIRECTION_PREV));
		
		updateFolderHistoryActions();
	}
	
	private void initComponents() {
		/* Editor: */
		pnlAudioFileEditor.editTags(null);  // Editor leer initialisieren
		
		/* Dateitabelle: */
		tblAudioFiles.setModel(new AudioFileTableModel());
		tblAudioFiles.getSelectionModel().addListSelectionListener(new FileTableSelectionListener());

		TableRowSorter<AudioFileTableModel> sorter = new TableRowSorter<AudioFileTableModel>();
		sorter.setModel((AudioFileTableModel)tblAudioFiles.getModel());
		tblAudioFiles.setRowSorter(sorter);
	}
	
	/**
	 * Diese Methode verarbeitet alle {@link Action}'s in der {@link Hashtable}
	 * <code>actions</code> dementsprechend, dass die zu den {@link Action}'s
	 * zugewiesenen Hotkeys der {@link JComponent} <code>onComponent</code>
	 * zur {@link InputMap} resp. {@link ActionMap} hinzugefügt werden.<br/>
	 * <br/>
	 * Das Überschreiben dieser Methode bietet sich an, wenn in der Unterklasse
	 * noch eigene KeyBindings erstellt werden müssen.<br/>
	 * <br/>
	 * <b>ACHTUNG:</b> Damit die KeyBindings funktionieren, muss diese Methode in der
	 * ableitenden Klasse mit der zu erstellenden ContentPane als Parameter
	 * aufgerufen werden!
	 * 
	 * @param onComponent
	 * @return
	 */
	protected JComponent initKeyBindings(JComponent onComponent) {
		
		InputMap inputMap = onComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = onComponent.getActionMap();

		Enumeration<String> actionKeys = actions.keys();
		while(actionKeys.hasMoreElements()) {
			String key = actionKeys.nextElement();
			Action action = actions.get(key);
			Object keyStroke = action.getValue(Action.ACCELERATOR_KEY);
			
			if(keyStroke != null && keyStroke instanceof KeyStroke) {
				inputMap.put((KeyStroke)keyStroke, key);
				actionMap.put(key, action);
			}
		}
		
		return onComponent;
	}
	
	// Audiofiles --------------------------------------------------------------
	/**
	 * Lädt alle Audiofiles aus dem Ordner <code>folder</code> in den Speicher
	 * und zeigt sie in der Dateitabelle an.<br/>
	 * Die Wartezeit währendem Laden wird mit einem {@link WaitDialog}
	 * überbrückt.
	 * 
	 * @param folder
	 * @param addToHistory bestimmt, ob der Pfad von <code>folder</code> der Ordnerhistory hinzugefügt werden soll
	 */
	protected void loadFilesIntoTable(final File folder, final boolean addToHistory) {		
		if(!currentlyLoadingFiles) {
			Runnable task = new Runnable() {
				@Override
				public void run() {
					currentlyLoadingFiles = true;
					setCursor(new Cursor(Cursor.WAIT_CURSOR));
					enableControls(false);
					
					
					AudioFileTableModel model = (AudioFileTableModel)tblAudioFiles.getModel();
					File[] audioFiles = new File[0];
					
					if(folder != null) {
						audioFiles = folder.listFiles(new FileFilter() {
							@Override
							public boolean accept(File pathname) {
								boolean ok = false;
								String name = pathname.getName().toLowerCase();
								if(name.endsWith(".mp3") || name.endsWith(".m4a")) ok = true;
								return ok;
							}
						});
						
						if(audioFiles != null) {
							/* Schreibschutz entfernen: */
							// Falls nötig (und in den Einstellungen aktiviert), wird
							// versucht, den Schreibschutz aller Dateien zu entfernen.
							if(Settings.getInstance().getBoolean(Settings.KEY_COMMON_TRY_REMOVE_WRITEONLY)) {
								for(File audioFile: audioFiles) {
									if(!audioFile.canRead()) audioFile.setWritable(true);
								}
							}
						} else {
							audioFiles = new File[0];
						}
					}
					
					/* GUI aktualisieren: */
					boolean enabled = (audioFiles.length > 0);
					enableControls(true);
					enableActions(enabled);
					model.setFiles(audioFiles);
					currentlyLoadingFiles = false;
					
					setSelectedFolder(folder);
					if(addToHistory) addPathToFolderHistory(folder.getPath());
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
			};
	
			Thread t = new Thread(task);
			t.start();
		}
	}
	
	protected void loadFilesIntoTable(File folder) {
		loadFilesIntoTable(folder, true);
	}
	
	/**
	 * Liefert eine {@link ArrayList} mit allen {@link AudioFile}'s, welche in
	 * <code>tblAudioFiles</code> momentan ausgewählt sind.
	 * 
	 * @return {@link ArrayList}
	 */
	protected ArrayList<AudioFile> getSelectedAudioFiles() {
		ListSelectionModel selectionModel = tblAudioFiles.getSelectionModel();
		
		int startIndex = selectionModel.getMinSelectionIndex();
		int endIndex = selectionModel.getMaxSelectionIndex();
		
		ArrayList<Integer> selectedRows = new ArrayList<Integer>();
		
		if(!selectionModel.isSelectionEmpty()) {
			if(startIndex == endIndex) {
				/* Nur ein Element gewählt: */
				selectedRows.add(startIndex);
			} else {
				/* Mehrere Elemente gewählt: */
				// Sind mehrere Elemente gewählt, muss herausgefunden
				// werden, welche genau gewählt sind (es muss keine
				// durchgehende Auswahl von start bis end sein!)
				for(int i = startIndex; i <= endIndex; i++) {
					if(selectionModel.isSelectedIndex(i)) {
						selectedRows.add(i);
					}
				}
			}
		}

		AudioFileTableModel model = (AudioFileTableModel)tblAudioFiles.getModel();
		ArrayList<AudioFile> audioFiles = new ArrayList<AudioFile>(selectedRows.size());
		
		if(!selectedRows.isEmpty()) {					
			for(int i = 0, l = selectedRows.size(); i < l; i++) {
				int selectedRow = selectedRows.get(i);
				selectedRow = tblAudioFiles.getRowSorter().convertRowIndexToModel(selectedRow);
				audioFiles.add(model.getAudioFile(selectedRow));
			}
		}
		
		return audioFiles;
	}
	
	protected void enableControls(boolean enabled) {
		tblAudioFiles.setEnabled(enabled);
	}
	
	
	// Folder-History ----------------------------------------------------------
	protected void addPathToFolderHistory(String path) {
		folderHistory.add(path);
		positionInFolderHistory = folderHistory.size()-1;
		updateFolderHistoryActions();
	}
	
	protected void updateFolderHistoryActions() {
		boolean enableForward = false;
		boolean enableBack = false;
		
		if(folderHistory.size() > 1) {
			if(positionInFolderHistory > 0)
				enableBack = true;
			if(positionInFolderHistory < folderHistory.size()-1)
				enableForward = true;
		}
		
		actions.get(ACTION_GOTO_NEXT_FOLDER).setEnabled(enableForward);
		actions.get(ACTION_GOTO_PREV_FOLDER).setEnabled(enableBack);
	}
	
	// Zugriff -----------------------------------------------------------------
	protected void setSelectedFolder(File folder) {
		this.audioFilesFolder = folder;
		observableDelegate.setChanged(true);
		observableDelegate.notifyObservers(OBSERVABLE_FILES_CHANGED);
	}
	
	protected File getSelectedFolder() {
		return this.audioFilesFolder;
	}
	
	// Hilfsklassen ------------------------------------------------------------
	/**
	 * Dieser {@link ListSelectionListener} stellt sicher, dass die Audiofiles,
	 * welche in der Dateitabelle ausgewählt werden, dem {@link AudioFileEditorPanel}
	 * korrekt zur Bearbeitung übergeben werden.
	 * 
	 * @see JTable
	 * @see AudioFileEditorPanel
	 */
	protected class FileTableSelectionListener implements ListSelectionListener {
		
		@Override
		public void valueChanged(ListSelectionEvent e) {
			/* Ausgewählte Audiofiles holen: */
			ArrayList<AudioFile> audioFiles = getSelectedAudioFiles();
			
			/* Tab wechseln: */
			// Falls Files ausgewählt sind, momentan aber nicht der EditorPanel
			// angezeigt wird, zu diesem wechseln.
//			if(!audioFiles.isEmpty() && tbpTabs.getSelectedIndex() == TAB_BROWSE) {
//				tbpTabs.setSelectedIndex(TAB_EDIT);
//				tblAudioFiles.requestFocus();
//			}
			
			/* Tags dem TagEditorPanel übergeben: */
			pnlAudioFileEditor.editTags(audioFiles);
			
			/* GUI aktualisieren: */
			enableActions(true);
		}
	}
	
	private class ObservableDelegate extends Observable {
		public ObservableDelegate() { }
		public void setChanged(boolean changed) {
			if(changed) setChanged();
		}
	}
	
	
	// Observable --------------------------------------------------------------
	public void addObserver(Observer observer) {
		observableDelegate.addObserver(observer);
	}
	
	// Actions -----------------------------------------------------------------
	/**
	 * Ermöglicht das Anpassen der {@link Action}'s durch die ableitende Klasse.<br/>
	 * Dinge wie Hotkeys, Icons usw. könne hier zugewiesen werden.<br/>
	 * {@link #customizeActions()} wird von {@link AudioTaggerFrame} automatisch
	 * aufgerufen.
	 */
	protected abstract void customizeActions();
	
	protected void enableActions(boolean enableTagActions) {
		actions.get(ACTION_TAGS_SAVE).setEnabled(enableTagActions);
		actions.get(ACTION_ARTWORK_IMPORT).setEnabled(enableTagActions);
		actions.get(ACTION_ARTWORK_SELECT_LOCAL).setEnabled(enableTagActions);
		actions.get(ACTION_ARTWORK_REMVOE).setEnabled(enableTagActions);
		actions.get(ACTION_TAGS_IMPORT).setEnabled(enableTagActions);
		actions.get(ACTION_TAGS_EDIT_TRACKNUMBERS).setEnabled(enableTagActions);
		actions.get(ACTION_TAGS_TO_FILENAME).setEnabled(enableTagActions);
		actions.get(ACTION_GOTO_NEXT_FILE).setEnabled(enableTagActions);
		actions.get(ACTION_GOTO_PREV_FILE).setEnabled(enableTagActions);
	}
	
	private class GotoInAudioFileTable extends AbstractAction {

		private int direction;
		
		public static final int DIRECTION_NEXT = 1;
		public static final int DIRECTION_PREV = -1;
		private static final long serialVersionUID = -5172661753069932249L;
		
		public GotoInAudioFileTable(String text, int direction) {
			super(text);
			this.direction = direction;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			int currentRow = tblAudioFiles.getSelectedRow();
			int newRow = currentRow + direction;
			
			if(newRow >= 0 && newRow < tblAudioFiles.getRowCount()) {
				// Wenn aktiviert, automatisch speichern:
				if(pnlAudioFileEditor.hasEditorChanged()
				   && Settings.getInstance().getBoolean(Settings.KEY_COMMON_AUTOSAVE_WHEN_HOTKEYS)) {
					pnlAudioFileEditor.saveTags();
				}
				
				tblAudioFiles.getSelectionModel().setSelectionInterval(newRow, newRow);
			}
		}
	}
	
	private class SaveTagsAction extends AbstractAction {
		
		private static final long serialVersionUID = 3757688865328075383L;

		public SaveTagsAction() {
			super("Save tags");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			doSaveTags();
		}
		
		private void doSaveTags() {
			/* Daten im Editor speichern: */
			pnlAudioFileEditor.saveTags();
			
			/* Ausgewählte Zeilen in der Dateitabelle aktualisieren: */
			TableModelEvent e = new TableModelEvent(tblAudioFiles.getModel(),
					tblAudioFiles.getSelectionModel().getMinSelectionIndex(),
					tblAudioFiles.getSelectionModel().getMaxSelectionIndex());
			tblAudioFiles.tableChanged(e);
		}
	}
	
	private class RefreshFileTableAction extends AbstractAction { 
		
		private static final long serialVersionUID = 3757688865328075383L;

		public RefreshFileTableAction() {
			super("Refresh");
			putValue(SHORT_DESCRIPTION, "Refresh audio files from selected folder");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			doRefreshFileTable();
		}
		
		private void doRefreshFileTable() {
			pnlAudioFileEditor.editTags(null);  // Editor zurücksetzen
			if(audioFilesFolder != null) loadFilesIntoTable(audioFilesFolder, false);
		}
	}
	
	private class SearchArtworkAction extends AbstractAction {
		
		private static final long serialVersionUID = -8619209942627418993L;

		public SearchArtworkAction() {
			super("Import artwork from internet");
			putValue(SHORT_DESCRIPTION, "Import artwork from the internet for selected audio files");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			doSearchArtwork();
		}
		
		private void doSearchArtwork() {			
			/* Ausgewählte Audiofiles ermitteln: */
			// Sind mehrere AudioFiles ausgewählt, kann es sein, dass diese verschiedene
			// Artists und Album-Tags besitzen. Dem Artwork-Dialog wird jeweils ein
			// Array mit allen vorhandenen Tags übergeben, damit dieser diese
			// entsprechend verarbeiten kann.
			ArrayList<AudioFile> selectedAudioFiles = getSelectedAudioFiles();
			ArrayList<String> artists = new ArrayList<String>();
			ArrayList<String> albums = new ArrayList<String>();
			
			for (AudioFile audioFile: selectedAudioFiles) {
				Tag tags = audioFile.getTag();
				if(!artists.contains(tags.getFirstArtist())) artists.add(tags.getFirstArtist());
				if(!albums.contains(tags.getFirstAlbum())) albums.add(tags.getFirstAlbum());
			}
			
			/* Dialog anzeigen und wenn nötig anschliessend Artwork übernehmen: */
			ArtworkSearchDialog dialog = new ArtworkSearchDialog(
					AudioTaggerFrame.this,
					artists.toArray(new String[artists.size()]),
					albums.toArray(new String[albums.size()]));
			
			BufferedImage selectedArtwork = dialog.getSelectedArtwork();
			
			if(selectedArtwork != null) {
				pnlAudioFileEditor.setArtwork(selectedArtwork);
			}
		}
	}
	
	private class SelectLocalArtworkAction extends AbstractAction {
		
		private static final long serialVersionUID = -8953212902383412729L;

		public SelectLocalArtworkAction() {
			super("Select artwork");
			putValue(SHORT_DESCRIPTION, "Select an image file as artwork for selected audio files");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			doSelectLocalArtwork();
		}
		
		private void doSelectLocalArtwork() {			
			JFileChooser chooser = new JFileChooser();
			chooser.setMultiSelectionEnabled(false);
			chooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG images (*.png)", "png"));
			chooser.addChoosableFileFilter(new FileNameExtensionFilter("JPEG images (*.jpg)", "jpg"));
			int result = chooser.showOpenDialog(AudioTaggerFrame.this);
			
			if(result == JFileChooser.APPROVE_OPTION) {
				File selectedFile = chooser.getSelectedFile();
				boolean error = false;
				
				try {
					BufferedImage artwork = ImageIO.read(selectedFile);
					if(artwork != null) pnlAudioFileEditor.setArtwork(artwork);
					else error = true;
				} catch(Exception e) {
					error = true;
				}
				
				if(error) {
					JOptionPane.showMessageDialog(
							AudioTaggerFrame.this,
							"Could not load artwork from\n" + selectedFile.getPath() +
							"\n\nPlease select a proper image file.",
							"Artwork not loaded",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
	
	private class RemoveCurrentArtworkAction extends AbstractAction {
		
		private static final long serialVersionUID = -8953212902383412729L;

		public RemoveCurrentArtworkAction() {
			super("Remove artwork");
			putValue(SHORT_DESCRIPTION, "Remove the artwork from selected audio files");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			doDeleteCurrentArtwork();
		}
		
		private void doDeleteCurrentArtwork() {
			pnlAudioFileEditor.setArtwork(null);
		}
	}

	private class ImportTagsAction extends AbstractAction {

		private static final long serialVersionUID = -3464998810923805873L;

		public ImportTagsAction() {
			super("Import tags from internet");
			putValue(SHORT_DESCRIPTION, "Import tags from an internet database for selected audio files");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			doImportTags();
		}
		
		private void doImportTags() {
			ArrayList<AudioFile> audioFiles = getSelectedAudioFiles();
			boolean importTags = true;
			
			/* Alle geladenen AudioFiles ausgewählt? */
			// Hat der User nicht alle momentan geladenen Audiofiles ausgewählt,
			// um Tags zu importieren, wird in einem Confirm-Dialog nachgefragt,
			// ob er dies vorneweg tun will, damit die besten Resultate bei der
			// CDDB-Abfrage garantiert werden können.
			if(audioFiles.size() != tblAudioFiles.getModel().getRowCount()) {
				int result = JOptionPane.showConfirmDialog(
					AudioTaggerFrame.this,
					"You are trying to import tags only for few of the current loaded audio files.\n\n" +
					"If you have loaded the audio files of a complete album, it is recommended to run the\n" +
					"import procedure over all audio files.\n\n" +
					"Would you like to run the tag import over all currently loaded audio files?",
					"Import tags",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
				
				switch(result) {
				case JOptionPane.YES_OPTION :
					tblAudioFiles.getSelectionModel().setSelectionInterval(0, tblAudioFiles.getModel().getRowCount()-1);
					audioFiles = getSelectedAudioFiles();
					break;
				case JOptionPane.NO_OPTION :
					// Nothing to do
					break;
				case JOptionPane.CANCEL_OPTION :  // fall through
				case JOptionPane.CLOSED_OPTION :
					importTags = false;
					return;
				}
			}
			
			/* TagImportDialog: */
			// TagImportDialog erstellen & anzeigen.
			if(importTags) {
				TagImportDialog importDialog = new TagImportDialog(AudioTaggerFrame.this, audioFiles);
				if(importDialog.hasImportedTags())
					actions.get(ACTION_REFRESH).actionPerformed(new ActionEvent(this,0,""));
			}
		}
	}
	
	private class TagsToFilenameAction extends AbstractAction {

		private static final long serialVersionUID = -3464998810923805873L;

		public TagsToFilenameAction() {
			super("Tags to filename");
			putValue(SHORT_DESCRIPTION, "Rename audio files according to their tags");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			doTagsToFilename();
		}
		
		private void doTagsToFilename() {
			ArrayList<AudioFile> audioFiles = getSelectedAudioFiles();
			TagsToFilenameDialog tagsToFilenameDialog =
				new TagsToFilenameDialog(AudioTaggerFrame.this, audioFiles);
			
			if(tagsToFilenameDialog.isAudioFilesRenamed())
				actions.get(ACTION_REFRESH).actionPerformed(new ActionEvent(this,0,""));
		}
	}
	
	private class TracknumbersAction extends AbstractAction {

		private static final long serialVersionUID = -3464998810923805873L;

		public TracknumbersAction() {
			super("Tracknumbers");
			putValue(SHORT_DESCRIPTION, "Fix or generate tracknumbers");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			doTracknumbers();
		}
		
		private void doTracknumbers() {
			ArrayList<AudioFile> audioFileList = getSelectedAudioFiles();
			TracknumbersDialog dialog = new TracknumbersDialog(AudioTaggerFrame.this, audioFileList);

			if(dialog.isChangedTracknumbers()) actions.get(ACTION_REFRESH).actionPerformed(new ActionEvent(this,0,""));
		}
	}
	
	private class AboutAction extends AbstractAction {
		
		private static final long serialVersionUID = 5402637635564465572L;

		public AboutAction() {
			super("About AudioTagger");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			new AboutDialog(AudioTaggerFrame.this);
		}
	}
	
	private class QuitAction extends AbstractAction {
		
		private static final long serialVersionUID = -4866443046254740959L;

		public QuitAction() {
			super("Quit AudioTagger");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			boolean quit = true;
			
			if(pnlAudioFileEditor.hasEditorChanged()) {
				quit = false;
				int result = JOptionPane.showConfirmDialog(
					AudioTaggerFrame.this,
					"You have modified the tags of the currently selected audio files.\n" +
					"Would you like to save these changes?",
					"Save changes",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
				
				switch(result) {
				case JOptionPane.YES_OPTION :
					quit = true;
					pnlAudioFileEditor.saveTags();
					break;
				case JOptionPane.NO_OPTION :
					quit= true;
					break;
				case JOptionPane.CANCEL_OPTION :  // fall through
				case JOptionPane.CLOSED_OPTION :
					quit = false;
					break;
				}
			}
			
			if(quit) System.exit(0);
		}
	}
	
	private class PreferencesAction extends AbstractAction {

		private static final long serialVersionUID = 8169533301104134390L;

		public PreferencesAction() {
			super("Preferences");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			new PreferencesDialog(AudioTaggerFrame.this);
		}
	}
	
	private class GotoInFolderHistory extends AbstractAction {

		private int direction;
		
		public static final int DIRECTION_NEXT = 1;
		public static final int DIRECTION_PREV = -1;
		private static final long serialVersionUID = 8419978043422123611L;
		
		public GotoInFolderHistory(String text, int direction) {
			super(text);
			this.direction = direction;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			positionInFolderHistory += direction;
			String path = folderHistory.get(positionInFolderHistory);
			loadFilesIntoTable(new File(path), false);
			updateFolderHistoryActions();
		}
		
	}
	
	private class CheckForUpdatesAction extends AbstractAction {

		private static final long serialVersionUID = 6136263522006394577L;

		public CheckForUpdatesAction() {
			super("Check for Updates");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			UpdateChecker checker = new UpdateChecker(UpdateChecker.CHECKER_URL, UpdateChecker.APP_ID);
			VersionInfo versionInfo = checker.checkVersion(AudioTaggerApplication.getVersion());
			
			if(versionInfo.isNewVersionAvailable()) {
				JOptionPane.showMessageDialog(
						AudioTaggerFrame.this,
						"A new version of AudioTagger is available!\n" +
						"Open " + versionInfo.getUrl() + " in your browser and download the latest update.",
						"New version availalbe",
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(
						AudioTaggerFrame.this,
						"You are running the latest version of AudioTagger.",
						"No updates available",
						JOptionPane.INFORMATION_MESSAGE);				
			}
		}
	}
	
}