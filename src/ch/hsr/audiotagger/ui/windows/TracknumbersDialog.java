package ch.hsr.audiotagger.ui.windows;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.GroupLayout.Alignment;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.tag.Tag;

import ch.hsr.audiotagger.ui.GUIHelper;

/**
 * Stellt einen Dialog zur Verfügung, welcher eine Liste von {@link AudioFile}'s
 * mit generierten/korrigierten Tracknumbers versehen kann.<br/>
 * <br/>
 * Der User hat die Wahl zwischen zwei Funktionen:<br/>
 *  - Methode 1: Tracknumbers fixen<br/>
 * Sind bereits Tracknumbers vorhanden, kann versucht werden, anhand von diesen
 * alle Tracknumbers herauszurechnen (bspw. wird "01/39" zu "01" usw.<br/>
 *  - Methode 2: Tracknumbers generieren<br/>
 * Mit dieser Variante werden einfach alle Tracknumbers neu erstellt. Es gibt
 * zudem die Möglichkeit, einen Offset als Startnummer anzugeben.
 * 
 * @author Manuel Alabor
 */
public class TracknumbersDialog extends JDialog {

	private ArrayList<AudioFile> audioFiles;
	private DialogResult dialogResult = DialogResult.NOT_CHANGED;
	
	private final static JRadioButton optFixNumbering = new JRadioButton("Try to fix the available track numbers");
	private final static JRadioButton optNewNumbering = new JRadioButton("Create new track numbers with offset:");
	private final static JTextField txtOffset = new JTextField("1");
	private final static JCheckBox chkLeadingZero = new JCheckBox("Ensure having leading zeros (01, 02, ...)");
	private final static JCheckBox chkSaveTotalTracks = new JCheckBox("Save total track count");

	private final OKAction actionOK = new OKAction();
	private final CancelAction actionCancel = new CancelAction();
	
	private static final long serialVersionUID = 7114765356024649530L;
	
	private enum ChangeMethod { FIX_NUMBERING, GENERATE_NUMBERING }
	private enum DialogResult { NOT_CHANGED, CHANGED, CHANGED_WITH_ERRORS }
	
	public TracknumbersDialog(Frame owner, ArrayList<AudioFile> audioFiles) {
		super(owner,"Tracknumbers");
		this.audioFiles = audioFiles;
		setContentPane(buildGui());
		
		// Dialog anpassen:
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
		
		/* Direkt anzeigen: */
		setVisible(true);
	}
	
	private JComponent buildGui() {
		/* Vorbereiten: */
		JPanel gui = new JPanel();
		GroupLayout layout = new GroupLayout(gui);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		gui.setLayout(layout);
		initKeyBindings(gui);
		
		// RadioButtons:
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(optFixNumbering);
		buttonGroup.add(optNewNumbering);
		RadioButtonActionListener radioButtonActionListener = new RadioButtonActionListener();
		optFixNumbering.addActionListener(radioButtonActionListener);
		optNewNumbering.addActionListener(radioButtonActionListener);
		optFixNumbering.setSelected(true);
		
		// Textfelder:
		txtOffset.setEnabled(false);
		
		// CheckBoxes:
		chkLeadingZero.setSelected(true);
		
		// Buttons:
		JButton btnOK = new JButton(actionOK);
		JButton btnCancel = new JButton(actionCancel);
		getRootPane().setDefaultButton(btnOK);
		
		// Labels:
		JLabel lblMethod = new JLabel("Method");
		JLabel lblLeadingZero = new JLabel("Leading Zeros");
		JLabel lblSaveTotalTracks = new JLabel("Track count");
		
		/* GroupLayout konfigurieren: */
		// Vertikales Layout:
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(lblMethod)
				.addComponent(optFixNumbering)
			)
			.addComponent(optNewNumbering)
			.addComponent(txtOffset)
			
			.addGap(15)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(lblLeadingZero)
				.addComponent(chkLeadingZero)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(lblSaveTotalTracks)
				.addComponent(chkSaveTotalTracks)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(btnOK)
				.addComponent(btnCancel)
			)
		);
		
		// Horizontales Layout:
		layout.setHorizontalGroup(layout.createParallelGroup()
				
			.addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
					.addComponent(lblMethod)
					.addComponent(lblLeadingZero)
					.addComponent(lblSaveTotalTracks)
				)
				.addGroup(layout.createParallelGroup()
					.addComponent(optFixNumbering)
					.addComponent(optNewNumbering)
					.addComponent(txtOffset)
					
					.addComponent(chkLeadingZero)
					.addComponent(chkSaveTotalTracks)
				)
			)
			
			.addGroup(Alignment.TRAILING, layout.createSequentialGroup()
				.addComponent(btnOK)
				.addComponent(btnCancel)
			)		
		);
		
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
	 * Ermittelt, ob Tracknumbers bearbeitet wurden oder nicht.
	 * 
	 * @return boolean
	 */
	public boolean isChangedTracknumbers() {
		return (dialogResult == DialogResult.CHANGED || dialogResult == DialogResult.CHANGED_WITH_ERRORS);
	}
	
	
	// Tracknumbers ------------------------------------------------------------
	/**
	 * @param audioFiles
	 * @param method
	 * @param addLeadingZeros
	 * @param offset
	 */
	private void updateTracknumbers(final ArrayList<AudioFile> audioFiles, final ChangeMethod method, final boolean addLeadingZeros, final boolean saveTotalTracks, final int offset) {
		
		Runnable tracknumberChanger = new Runnable() {
			public void run() {
				int trackLeadingZeros = calcLeadingZeros(audioFiles.size(), offset);
				String totalTracks = "";
				if(saveTotalTracks) totalTracks = addLeadingZeros(audioFiles.size(), calcLeadingZeros(audioFiles.size()), addLeadingZeros);
				
				/* Verarbeiten: */
				if(method == ChangeMethod.FIX_NUMBERING) {
					fixTracknumbers(audioFiles, addLeadingZeros, trackLeadingZeros, totalTracks);
				} else if(method == ChangeMethod.GENERATE_NUMBERING) {			
					generateTracknumbers(audioFiles, addLeadingZeros, trackLeadingZeros, totalTracks, offset);
				}
			};
		};
		
		WaitDialog waitDialog = new WaitDialog(
				TracknumbersDialog.this,
				"Fix Tracknumbers",
				"Fixing tracknumbers... Please wait.",
				tracknumberChanger);
		waitDialog.setVisible(true);
	}

	
	private int calcLeadingZeros(int totalItems) {
		return calcLeadingZeros(totalItems, -1);
	}
	
	private int calcLeadingZeros(int totalItems, int startIndex) {
		int leadingZeros = 1;
		if(startIndex > -1) totalItems += (startIndex-1);
		
		if(totalItems > 99) leadingZeros = 2;
		else if(totalItems > 999) leadingZeros = 3;
		else if(totalItems > 9999) leadingZeros = 4;
		else if(totalItems > 99999) leadingZeros = 5;
		else if(totalItems > 999999) leadingZeros = 6;
		
		return leadingZeros;
	}
	
	private void fixTracknumbers(ArrayList<AudioFile> audioFiles, boolean addLeadingZeros, int trackLeadingZeros, String totalTracks) {
		int lastValidTracknumber = 1;
		
		for (AudioFile audioFile : audioFiles) {
			Tag tags = audioFile.getTag();
			String currentTracknumber = tags.getFirstTrack();
			int tracknumber = -1;
			
			/* Try 1: Einfach versuchen zu parsen: */
			tracknumber = parseStringTracknumber(currentTracknumber);
			
			/* Try 2: Versuchen zu splitten und zu parsen: */
			if(tracknumber == -1) {
				String[] splitted = currentTracknumber.split("/");
				if(splitted.length == 1) splitted = currentTracknumber.split("\\");
				
				if(splitted.length > 1) tracknumber = parseStringTracknumber(splitted[0]);
			}
			
			/* Abschluss: Tracknumber speichern: */
			// Wenn keine Tracknumber geparst werden konnte, aufgrund der letzten
			// gültigen Tracknumber die Nummer für das aktuelle AudioFile errechnen.
			if(tracknumber == -1) {
				tracknumber = lastValidTracknumber;
				lastValidTracknumber++;
			}
			
			lastValidTracknumber = tracknumber;
			String tracknumberWithLeadingZeros = addLeadingZeros(tracknumber, trackLeadingZeros, addLeadingZeros);
			
			if(!totalTracks.equals("")) tracknumberWithLeadingZeros += "/" + totalTracks;
			
			try {
				tags.setTrack(tracknumberWithLeadingZeros);
				audioFile.commit();				
			} catch(Exception e) {
				dialogResult = DialogResult.CHANGED_WITH_ERRORS;
			}
			
		}
		
		if(dialogResult != DialogResult.CHANGED_WITH_ERRORS) dialogResult = DialogResult.CHANGED;
	}
	
	private int parseStringTracknumber(String tracknumber) {
		int result = -1;
		
		try {
			result = Integer.parseInt(tracknumber);
		} catch(NumberFormatException ne) {
			result = -1;
		}
		
		return result;
	}
	
	private void generateTracknumbers(ArrayList<AudioFile> audioFiles, boolean addLeadingZeros, int trackLeadingZeros, String totalTracks, int offset) {
		
		for(int i = offset, l = i+audioFiles.size(), j = 0; i < l; i++, j++) {
			AudioFile audioFile = audioFiles.get(j);
			Tag tags = audioFile.getTag();
			
			try {
				String tracknumber = addLeadingZeros(i, trackLeadingZeros, addLeadingZeros);
				if(!totalTracks.equals("")) tracknumber += "/" + totalTracks;
				tags.setTrack(tracknumber);
				audioFile.commit();
			} catch (Exception e) {
				dialogResult = DialogResult.CHANGED_WITH_ERRORS;
			}
		}
		
		if(dialogResult != DialogResult.CHANGED_WITH_ERRORS) dialogResult = DialogResult.CHANGED;
	}
	
	
	/**
	 * Fügt einer Tracknumber, wenn aktiviert, führende Nullen hinzu.<br/>
	 * 
	 * @param tracknumber Zu verarbeitende Tracknumber
	 * @param leadingZeros Max. Anzahl führende Nullen
	 * @param enabled Aktiviert?
	 * @return
	 */
	private String addLeadingZeros(int tracknumber, int leadingZeros, boolean enabled) {
		String result = Integer.toString(tracknumber);
		
		if(enabled) {
			StringBuffer zeros = new StringBuffer();
			
			int toAdd = leadingZeros - result.length();
			for(int i = 0; i <= toAdd; i++) zeros.append("0");
			zeros.append(result);
			
			result = zeros.toString();
		}
		
		return result;
	}
	
	// Hilfsklassen ------------------------------------------------------------
	private class RadioButtonActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			txtOffset.setEnabled((source == optNewNumbering));
		}
	}
	
	
	// Actions -----------------------------------------------------------------
	private class OKAction extends AbstractAction {
		
		private static final long serialVersionUID = 8732985761254340850L;

		public OKAction() {
			super("Apply");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			/* Tracknumbers bearbeiten: */
			ChangeMethod method = ChangeMethod.FIX_NUMBERING;
			if(optNewNumbering.isSelected()) method = ChangeMethod.GENERATE_NUMBERING;
			
			int offset = 1;
			try {
				offset = Integer.parseInt(txtOffset.getText());
			} catch(NumberFormatException ne) { offset = 1; }
			
			updateTracknumbers(audioFiles, method, chkLeadingZero.isSelected(), chkSaveTotalTracks.isSelected(), offset);
			
			if(dialogResult == DialogResult.CHANGED_WITH_ERRORS) {
				JOptionPane.showMessageDialog(
						SwingUtilities.getWindowAncestor(TracknumbersDialog.this),
						"Could not fix tracknumbers for all selected audio files!",
						"Fix Tracknumbers",
						JOptionPane.ERROR_MESSAGE);
			}
			
			/* Schliessen: */
			dispose();
		}
	}
	
	private class CancelAction extends AbstractAction {

		private static final long serialVersionUID = 6848985986213545726L;

		public CancelAction() {
			super("Cancel");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			dialogResult = DialogResult.NOT_CHANGED;
			dispose();
		}
	}
	
}
