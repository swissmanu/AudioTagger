package ch.hsr.audiotagger.ui.windows;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.GroupLayout.Alignment;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.tag.Tag;

import ch.hsr.audiotagger.ui.GUIHelper;
import ch.hsr.audiotagger.ui.components.MultilineLabel;
import ch.hsr.audiotagger.ui.components.patternfield.PatternElementModel;
import ch.hsr.audiotagger.ui.components.patternfield.PatternField;

/**
 * Stellt einen {@link JDialog} zur Verfügung, in welchem mit einem {@link PatternField}
 * ein Dateinamenmuster erstellt werden kann.<br/>
 * Anhand von diesem Muster können anschliessend die übergebenen Audiodateien
 * umbenannt werden.
 * 
 * @author Manuel Alabor
 * @see PatternField
 */
public class TagsToFilenameDialog extends JDialog {

	private ArrayList<AudioFile> audioFiles = null;
	private DialogResult dialogResult = DialogResult.NOT_RENAMED;
	
	private final static PatternField ptfPattern = new PatternField();
	private final static JLabel lblHint = new JLabel();
	
	private final OKAction actionOK = new OKAction();
	private final CancelAction actionCancel = new CancelAction();
	
	private final static String PATTERN_TRACK = "%track%";
	private final static String PATTERN_ARTIST = "%artist%";
	private final static String PATTERN_ALBUM = "%album%";
	private final static String PATTERN_TITLE = "%title%";
	private final static String PATTERN_YEAR = "%year%";
	
	private static final long serialVersionUID = -5419359049725778056L;
	
	private enum DialogResult {
		NOT_RENAMED, RENAMED, RENAMED_WITH_ERRORS
	}
	
	public TagsToFilenameDialog(Frame owner, ArrayList<AudioFile> audioFiles) {
		super(owner,"Tags to filename");
		
		setContentPane(buildGui());
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		pack();
		GUIHelper.centerOnComponent(this, owner);
		setResizable(false);
		setModal(true);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if(actionCancel.isEnabled())
					actionCancel.actionPerformed(new ActionEvent(this,0,"windowClosed"));
			}
		});
		
		this.audioFiles = audioFiles;
		
		setVisible(true);
	}
	
	/**
	 * Erstellt das GUI für den {@link ArtworkSearchDialog} mit einem
	 * {@link GroupLayout}.
	 * 
	 * @return GUI als {@link JComponent}
	 */
	private JComponent buildGui() {
		/* Vorbereiten: */
		// Container & Layout:
		JComponent gui = new JPanel();
		GroupLayout layout = new GroupLayout(gui);
		gui.setLayout(layout);
		initKeyBindings(gui);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		// PatternField:
		ptfPattern.setPreferredWidthIfEmpty(140);
		
		// Panel mit verfügbaren Tag-PatternElements:
		JPanel pnlTagBricks = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
		pnlTagBricks.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		
		PatternElementModel trackBrick = new PatternElementModel("Tracknumber", PATTERN_TRACK);
		PatternElementModel artistBrick = new PatternElementModel("Artist", PATTERN_ARTIST);
		PatternElementModel titleBrick = new PatternElementModel("Title", PATTERN_TITLE);
		PatternElementModel albumBrick = new PatternElementModel("Album", PATTERN_ALBUM);
		PatternElementModel yearBrick = new PatternElementModel("Year", PATTERN_YEAR);
		pnlTagBricks.add(ptfPattern.createPatternElement(trackBrick, true));
		pnlTagBricks.add(ptfPattern.createPatternElement(artistBrick, true));
		pnlTagBricks.add(ptfPattern.createPatternElement(titleBrick, true));
		pnlTagBricks.add(ptfPattern.createPatternElement(albumBrick, true));
		pnlTagBricks.add(ptfPattern.createPatternElement(yearBrick, true));
		
		// Panel mit verfügbaren Spezial-PatternElements:
		JPanel pnlSpecialBricks = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
		PatternElementModel spacedDashBrick = new PatternElementModel(" - ", " - ");
		pnlSpecialBricks.add(ptfPattern.createPatternElement(spacedDashBrick, false));
		pnlSpecialBricks.add(ptfPattern.createPatternElement(new PatternElementModel(" _ ", " _ "), false));
		pnlSpecialBricks.add(ptfPattern.createPatternElement(new PatternElementModel(" . ", " . "), false));
		pnlSpecialBricks.add(ptfPattern.createPatternElement(new PatternElementModel("-", "-"), false));
		pnlSpecialBricks.add(ptfPattern.createPatternElement(new PatternElementModel("_", "_"), false));
		pnlSpecialBricks.add(ptfPattern.createPatternElement(new PatternElementModel(".", "."), false));
		pnlSpecialBricks.add(ptfPattern.createPatternElement(new PatternElementModel("(", "("), false));
		pnlSpecialBricks.add(ptfPattern.createPatternElement(new PatternElementModel(") ", ")"), false));
		
		ptfPattern.addPatternElement(trackBrick);
		ptfPattern.addPatternElement(spacedDashBrick);
		ptfPattern.addPatternElement(artistBrick);
		ptfPattern.addPatternElement(spacedDashBrick);
		ptfPattern.addPatternElement(titleBrick);
		
		// Labels:
		MultilineLabel lblIntro = new MultilineLabel(
				"Drag and drop the predefined bricks to the pattern field to create a filename pattern.\n" +
				"You can move already added bricks by dragging them to their new position. Doubleclick " +
				"on a brick to\n" +
				"remove it from the pattern.");
		JLabel lblPattern = new JLabel("Pattern");
		JLabel lblTagBricks = new JLabel("Tags");
		JLabel lblSpecialBricks = new JLabel("Separators");
		
		lblIntro.setBorder(BorderFactory.createEmptyBorder(0,0,15,0));
		lblTagBricks.setBorder(pnlTagBricks.getBorder());
		
		// Buttons:
		JButton btnOK = new JButton(actionOK);
		JButton btnCancel = new JButton(actionCancel);
		getRootPane().setDefaultButton(btnOK);
		
		/* GroupLayout konfigurieren: */
		// Vertikales Layout:
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(lblIntro, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(lblPattern)
				.addComponent(ptfPattern, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(lblTagBricks)
				.addComponent(pnlTagBricks, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(lblSpecialBricks)
				.addComponent(pnlSpecialBricks, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(lblHint)
				.addComponent(btnOK)
				.addComponent(btnCancel)
			)
		);
		
		// Horizontales Layout:
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(lblIntro)
			.addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
					.addComponent(lblPattern)
					.addComponent(lblTagBricks)
					.addComponent(lblSpecialBricks)
				)
				.addGroup(layout.createParallelGroup()
					.addComponent(ptfPattern)
					.addComponent(pnlTagBricks)
					.addComponent(pnlSpecialBricks)
				)
			)
			.addGroup(Alignment.LEADING, layout.createSequentialGroup()
				.addComponent(lblHint)
			)
			.addGroup(Alignment.TRAILING, layout.createSequentialGroup()
				.addComponent(btnOK)
				.addComponent(btnCancel)
			)		
		);
		
		/* Rückgabe: */
		return gui;
	}
	
	private void initKeyBindings(JComponent component) {
		InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "OK");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Cancel");
		
		ActionMap actionMap = component.getActionMap();
		actionMap.put("OK", actionOK);
		actionMap.put("Cancel", actionCancel);
	}
	
	
	// Zugriff -----------------------------------------------------------------
	/**
	 * Falls Dateien umbenannt wurden, gibt diese Methode nach schliessen des
	 * Dialogs true zurück.
	 * 
	 * @return true, wenn Dateien umbenannt wurden
	 */
	public boolean isAudioFilesRenamed() {
		return (dialogResult == DialogResult.RENAMED || dialogResult == DialogResult.RENAMED_WITH_ERRORS);
	}
	
	
	// Renaming ----------------------------------------------------------------
	/**
	 * Benennt die {@link AudioFile}'s <code>audioFiles</code> gemäss <code>pattern</code>
	 * um.<br/>
	 * <br/>
	 * <code>pattern</code> ist hierbei eine Zeichenfolge, welche als Vorlage
	 * für die Dateinamen steht.<br/>
	 * Beispiel:<br/>
	 *  - Pattern: %track% - %artist% - %title%
	 *  - AudioFile mit den Tags: Track "01", Artist "Bethoven", Title "5. Symphonie"<br/>
	 * <br/>
	 * Dies wird in folgenden Dateinamen umgesetzt:<br/>
	 *  - 01 - Bethoven - 5. Symphone.mp3<br/>
	 *  <br/>
	 *  Folgende Platzhalter für das Pattern sind verfügbar:<br/>
	 *  <ul>
	 *  	<li>%track%</li>
	 *  	<li>%artist%</li>
	 *  	<li>%album%</li>
	 *  	<li>%year%</li>
	 *  	<li>%genre%</li>
	 *  </ul>
	 * 
	 * @param audioFiles
	 * @param pattern
	 * @return boolean Erfolg/Misserfolg
	 * @see PatternField#getPatternElementsAsString()
	 */
	private void renameAudioFiles(final ArrayList<AudioFile> audioFiles, final String pattern) {
		
		Runnable renamer = new Runnable() {
			@Override
			public void run() {
				for(AudioFile audioFile: audioFiles) {
					File file = audioFile.getFile();
					String path = file.getParentFile().getAbsolutePath();
					String filename = file.getName();
					String extension = filename.substring(filename.lastIndexOf('.'));
					String newFilename = replacePattern(audioFile.getTag(), pattern);
					
					boolean result = file.renameTo(new File(path + File.separator + newFilename + extension));
					if(!result) dialogResult = DialogResult.RENAMED_WITH_ERRORS;
				}
				
				if(dialogResult != DialogResult.RENAMED_WITH_ERRORS)
					dialogResult = DialogResult.RENAMED;
			}
		};
		
		WaitDialog waitDialog = new WaitDialog(
				TagsToFilenameDialog.this,
				"Rename Files",
				"Rename files... Please wait.",
				renamer);
		waitDialog.setVisible(true);
	}
	
	private String replacePattern(Tag tags, String pattern) {
		String replaced = pattern;
		
		String[] splittedTracknumber = tags.getFirstTrack().split("/");
		replaced = replaced.replace(PATTERN_TRACK, splittedTracknumber[0]);
		replaced = replaced.replace(PATTERN_ARTIST, tags.getFirstArtist());
		replaced = replaced.replace(PATTERN_TITLE, tags.getFirstTitle());
		replaced = replaced.replace(PATTERN_ALBUM, tags.getFirstAlbum());
		replaced = replaced.replace(PATTERN_YEAR, tags.getFirstYear());
		
		return replaced;
	}
	
	
	// Actions -----------------------------------------------------------------
	private class OKAction extends AbstractAction {
		
		private static final long serialVersionUID = -4617753173812434740L;

		public OKAction() {
			super("Rename");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
		 	renameAudioFiles(audioFiles, ptfPattern.getPatternElementsAsString());
		 	
			if(dialogResult == DialogResult.RENAMED_WITH_ERRORS) {
				JOptionPane.showMessageDialog(
						SwingUtilities.getWindowAncestor(TagsToFilenameDialog.this),
						"Could not rename all selected audio files!",
						"Tags to filename",
						JOptionPane.ERROR_MESSAGE);
			}
		 	
			dispose();
		}
	}
	
	private class CancelAction extends AbstractAction {

		private static final long serialVersionUID = -4965254201227669376L;

		public CancelAction() {
			super("Cancel");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			dialogResult = DialogResult.NOT_RENAMED;
			dispose();
		}
	}
	
}
