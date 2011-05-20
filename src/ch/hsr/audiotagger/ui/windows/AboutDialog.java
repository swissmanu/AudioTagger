package ch.hsr.audiotagger.ui.windows;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.GroupLayout.Alignment;

import ch.hsr.audiotagger.AudioTaggerApplication;
import ch.hsr.audiotagger.ui.GUIHelper;
import ch.hsr.audiotagger.ui.GUIImageManager;

/**
 * @author Manuel Alabor
 */
public class AboutDialog extends JDialog {

	private final OKAction actionOK = new OKAction();
	private static final long serialVersionUID = -8148828120927650898L;
	
	public AboutDialog(Frame owner) {
		super(owner,"About AudioTagger");
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		/* GUI erstellen: */
		// Erstellen:
		setContentPane(buildGui());
		
		// Dialog anpassen:
		pack();
		GUIHelper.centerOnComponent(this, owner);
		setResizable(false);
		setModal(true);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				actionOK.actionPerformed(new ActionEvent(this,0,"windowClosed"));
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
		
		// Buttons:
		JButton btnOK = new JButton(actionOK);
		getRootPane().setDefaultButton(btnOK);
		
		// Textkomponenten:
		JLabel lblTitle = new JLabel("AudioTagger", GUIImageManager.getInstance().getImageIcon("appicons/icon.png"), SwingConstants.CENTER);
		lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, lblTitle.getFont().getSize()+10f));
		lblTitle.setHorizontalTextPosition(SwingConstants.CENTER);
		lblTitle.setVerticalTextPosition(SwingConstants.BOTTOM);
		
		JTextPane txtText = new JTextPane();
		txtText.setText(
				"AudioTagger " + AudioTaggerApplication.getVersion() + "\n\n" +
				"AudioTagger is an opensource audio file tagging software developed by Manuel Alabor.\n" +
				"This program and its source is released under the LGP-License\n" +
				"Go to http://www.msites.net/audiotagger/ for updates, sources and other information for and about the official version.\n\n" +
				"Further following third party products are used by AudioTagger:\n" +
				" - JAudioTagger by http://www.jthink.net/jaudiotagger/\n" +
				" - JGoodies Looks by Karsten Lentzsch\n" +
				" - SimpleInternalFrame.java by Karsten Lentzsch\n" +
				" - UIFSplitPane.java by Karsten Lentzsch\n" +
				" - UI icon ressources by famfamfam\n" +
				" - AudioTagger trompet icon by templay.de"
				
				);
		txtText.setEditable(false);
		txtText.setBackground(lblTitle.getBackground());
		txtText.setPreferredSize(new Dimension(450,180));
		txtText.setFocusable(false);
		txtText.setCaretPosition(0);
		JScrollPane scroller = new JScrollPane(txtText);
		scroller.setBorder(BorderFactory.createEmptyBorder());
		
		/* GroupLayout konfigurieren: */
		// Vertikales Layout:
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(lblTitle)
			.addComponent(scroller)
			
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(btnOK)
			)
		);
		
		// Horizontales Layout:
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addGroup(layout.createParallelGroup(Alignment.CENTER)
				.addComponent(lblTitle)
				.addComponent(scroller)
			)
			
			.addGroup(Alignment.TRAILING, layout.createSequentialGroup()
				.addComponent(btnOK)
			)		
		);
		
		return gui;
	}
	
	private void initKeyBindings(JComponent component) {
		InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "OK");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "OK");
		
		ActionMap actionMap = component.getActionMap();
		actionMap.put("OK", actionOK);
	}
	
	// Hilfsklassen ------------------------------------------------------------
	private class OKAction extends AbstractAction {
		
		private static final long serialVersionUID = -8646558219477397834L;

		public OKAction() {
			super("Close");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			dispose();
		}
	}
	
}
