package ch.hsr.audiotagger.ui.windows;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.GroupLayout.Alignment;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.tag.Tag;

import ch.hsr.audiotagger.io.Settings;
import ch.hsr.audiotagger.io.cddb.CDDB;
import ch.hsr.audiotagger.io.cddb.XMCD;
import ch.hsr.audiotagger.ui.GUIHelper;
import ch.hsr.audiotagger.ui.GUIImageManager;

public class TagImportDialog extends JDialog {

	private ArrayList<AudioFile> audioFiles = null;
	private ImportResults dialogResult = ImportResults.NOT_IMPORTED;
	
	private final static JList lstDiscs = new JList(new DefaultListModel());
	private final static JTextArea txtDetails = new JTextArea();
	private final static JLabel lblHint = new JLabel();
	
	private final OKAction actionOK = new OKAction();
	private final CancelAction actionCancel = new CancelAction();
	
	private static final long serialVersionUID = -5419359049725778056L;

	private enum ImportResults {
		NOT_IMPORTED, IMPORTED, IMPORTED_WITH_ERRORS
	}
	
	public TagImportDialog(Frame owner, ArrayList<AudioFile> audioFiles) {
		super(owner,"Import tags");
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setSize(530,420);
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
		
		/* GUI erstellen: */
		// Erstellen:
		setContentPane(buildGui());
		
		/* Direkt suchen & anzeigen: */
		searchDiscInformation(audioFiles);
		setVisible(true);
	}
	
	
	private JComponent buildGui() {
		/* Vorbereiten: */
		// Container & Layout:
		JComponent gui = new JPanel();
		GroupLayout layout = new GroupLayout(gui);
		gui.setLayout(layout);
		initKeyBindings(gui);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		// Liste & Details:
		lstDiscs.setCellRenderer(new DiscListCellRenderer());
		lstDiscs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lstDiscs.addListSelectionListener(new DiscListSelectionListener());
		txtDetails.setEditable(false);
		JScrollPane scpDiscs = new JScrollPane(lstDiscs);
		JScrollPane scpDetails = new JScrollPane(txtDetails);
		
		// Buttons:
		JButton btnOK = new JButton(actionOK);
		JButton btnCancel = new JButton(actionCancel);
		getRootPane().setDefaultButton(btnOK);
		
		/* GroupLayout konfigurieren: */
		// Vertikales Layout:
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(scpDiscs)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(scpDetails, 120, 120, 120)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(lblHint)
				.addComponent(btnOK)
				.addComponent(btnCancel)
			)
		);
		
		// Horizontales Layout:
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addGroup(layout.createParallelGroup()
				.addComponent(scpDiscs)
				.addComponent(scpDetails)
				
				.addGroup(Alignment.LEADING, layout.createSequentialGroup()
					.addComponent(lblHint)
				)
				.addGroup(Alignment.TRAILING, layout.createSequentialGroup()
					.addComponent(btnOK)
					.addComponent(btnCancel)
				)
			)			
		);
		
		/* RŸckgabe: */
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
	
	/**
	 * 
	 * @param audioFiles
	 */
	private void searchDiscInformation(ArrayList<AudioFile> audioFiles) {
		
		final AudioFile[] arrayBuffer = audioFiles.toArray(new AudioFile[audioFiles.size()]);
		
		Runnable loader = new Runnable() {			
			@Override
			public void run() {
				updateGui(false, Cursor.WAIT_CURSOR);
				DefaultListModel model = (DefaultListModel)lstDiscs.getModel();
				model.clear();
				lblHint.setText("Searching for matching audio discs...");
				
				CDDB cddb = new CDDB(Settings.getInstance().getString(Settings.KEY_CDDB_HOST));
				try {
					XMCD[] xmcds = cddb.getDiscInformation(arrayBuffer);
					
					if(xmcds.length == 0) {
						JOptionPane.showMessageDialog(
							TagImportDialog.this,
							"No matchting disc information found.",
							"Import tags",
							JOptionPane.ERROR_MESSAGE);
						dispose();
					} else {
						for(XMCD xmcd: xmcds) {
							model.addElement(xmcd);
						}
						lstDiscs.setSelectedIndex(0);
						lblHint.setText(model.getSize() + " matching audio disc(s) found");
						updateGui(true, Cursor.DEFAULT_CURSOR);
						lstDiscs.requestFocus();
					}

				} catch (Exception e) {
					JOptionPane.showMessageDialog(
							TagImportDialog.this,
							"There was an error during checking the online database!\n" +
							"Please check your internet connection and/or firewall settings and try again.",
							"Connection error",
							JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
					dispose();
				}
			}
		};
		
		Thread t = new Thread(loader);
		t.start();
	}
	
	/**
	 * Aktiviert bzw. deaktiviert GUI-Komponenten.
	 * 
	 * @param enabled
	 */
	private void updateGui(boolean enabled, int cursorType) {
		setCursor(new Cursor(cursorType));
		
		lstDiscs.setEnabled(enabled);
		txtDetails.setEnabled(enabled);
		actionOK.setEnabled(enabled);
		actionCancel.setEnabled(enabled);
	}
	
	
	// Tags importieren --------------------------------------------------------
	private void importTags(final XMCD xmcd, final ArrayList<AudioFile> audioFiles) {
		Runnable importer = new Runnable() {
			public void run() {
				String[] buffer = xmcd.getTitle().split("/");
				String[] titles = xmcd.getTrackTitles();
				String artist = buffer[0].trim();
				String album = buffer[1].trim();
				String year = xmcd.getYear();
				
				for(int i = 0, l = audioFiles.size(); i < l; i++) {
					String title = "";
					if(titles.length > i) title = titles[i];
					
					AudioFile audioFile = audioFiles.get(i);
					Tag tags = audioFile.getTag();
					
					try {
						tags.setTitle(title);
						tags.setArtist(artist);
						tags.setAlbum(album);
						tags.setYear(year);
						audioFile.commit();				
					} catch(Exception e) {
						dialogResult = ImportResults.IMPORTED_WITH_ERRORS;
					}
				}
				
				if(dialogResult != ImportResults.IMPORTED_WITH_ERRORS) dialogResult = ImportResults.IMPORTED;
			};
		};

		WaitDialog waitDialog = new WaitDialog(this, "Import Tags", "Import tags... Please wait.", importer);
		waitDialog.setVisible(true);
	}
	
	// Zugriff -----------------------------------------------------------------
	public boolean hasImportedTags() {
		return (dialogResult == ImportResults.IMPORTED || dialogResult == ImportResults.IMPORTED_WITH_ERRORS);
	}
	
	
	// Hilfsklassen ------------------------------------------------------------
	private class DiscListCellRenderer extends DefaultListCellRenderer {
		
		private static final long serialVersionUID = 6137460310813284016L;

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			setIcon(GUIImageManager.getInstance().getImageIcon("universal/page.png"));
			
			XMCD xmcd = (XMCD)value;
			setText(xmcd.getTitle() + " (" + xmcd.getTrackTitles().length + " tracks)");
			
			return this;
		}
	}
	
	private class DiscListSelectionListener implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if(!e.getValueIsAdjusting()) {				
				Object value = lstDiscs.getSelectedValue();
				
				if(value != null) {
					XMCD xmcd = (XMCD)value;
					
					StringBuilder details = new StringBuilder();
					details.append("Title: " + xmcd.getTitle() + "\n");
					details.append("Year: " + xmcd.getYear() + "\n");
					
					details.append("Tracks:\n");
					String[] trackTitles = xmcd.getTrackTitles();
					for(int i = 0, l = trackTitles.length; i < l; i++) {
						details.append("   " + (i+1) + ". " + trackTitles[i] + "\n");
					}
					
					txtDetails.setText(details.toString());
					txtDetails.setCaretPosition(0);
				} else {
					txtDetails.setText("");
				}
				
			}
		}
	}
	
	
	// Actions -----------------------------------------------------------------
	private class OKAction extends AbstractAction {
		
		private static final long serialVersionUID = -5385370856059848705L;

		public OKAction() {
			super("Import");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			XMCD xmcd = (XMCD)lstDiscs.getSelectedValue();
			importTags(xmcd, TagImportDialog.this.audioFiles);
			
			if(dialogResult == ImportResults.IMPORTED_WITH_ERRORS) {
				JOptionPane.showMessageDialog(
						SwingUtilities.getWindowAncestor(TagImportDialog.this),
						"Could not import all tags properly!",
						"Import tags",
						JOptionPane.ERROR_MESSAGE);
			}
			
			dispose();
		}
	}
	
	private class CancelAction extends AbstractAction {

		private static final long serialVersionUID = 1543352222470028968L;

		public CancelAction() {
			super("Cancel");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			dialogResult = ImportResults.NOT_IMPORTED;
			dispose();
		}
	}
	
	
}
