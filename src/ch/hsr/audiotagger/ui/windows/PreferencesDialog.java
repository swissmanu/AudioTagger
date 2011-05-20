package ch.hsr.audiotagger.ui.windows;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.GroupLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.GroupLayout.Alignment;

import ch.hsr.audiotagger.io.Settings;
import ch.hsr.audiotagger.ui.GUIHelper;

public class PreferencesDialog extends JDialog {
	
	private final static JCheckBox chkRemoveWriteOnly = new JCheckBox("Try removing write protection from audio files");
	private final static JCheckBox chkAutosaveWhenHotkeys = new JCheckBox("Autosave changes when navigate with hotkeys");
	private final static JCheckBox chkCheckForUpdates = new JCheckBox("Check for AudioTagger updates on the internet automaticaly");
	private final static JComboBox comCddbHost = new JComboBox(new String[]{"http://freedb.freedb.org/~cddb/cddb.cgi"});
	private final static JCheckBox chkUseProxy = new JCheckBox("Use this proxy server to connect to the internet:");
	private final static JTextField txtProxyHost = new JTextField();
	private final static JTextField txtProxyPort = new JTextField();
	private final static JTabbedPane tbpTabs = new JTabbedPane();
	
	private final OKAction actionOK = new OKAction();
	private final CancelAction actionCancel = new CancelAction();
	
	@SuppressWarnings("unused")
	private final static int TAB_COMMON = 0;
	private final static int TAB_IMPORT = 1;
	@SuppressWarnings("unused")
	private final static int TAB_NETWORK = 2;
	
	private static final long serialVersionUID = 2210696029666112622L;	
	
	public PreferencesDialog(Frame owner) {
		super(owner,"Preferences");
		
		setContentPane(buildGui());
		pack();
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
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
		
		/* Dialog anzeigen: */
		readSettings();
		setVisible(true);
	}
	
	/**
	 * Erstellt das GUI für den {@link PreferencesDialog} mit einem
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
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		initKeyBindings(gui);
		
		// Buttons:
		JButton btnOK = new JButton(actionOK);
		JButton btnCancel = new JButton(actionCancel);
		getRootPane().setDefaultButton(btnOK);
		
		// Tabs:
		tbpTabs.setPreferredSize(new Dimension(420, 200));
		tbpTabs.add(buildCommonSettings(), "Common");
		tbpTabs.add(buildImportSettings(), "Import");
		tbpTabs.add(buildProxySettings(), "Proxy");
		
		/* Zusammensetzen: */
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(tbpTabs)
			.addGroup(layout.createParallelGroup()
				.addComponent(btnOK)
				.addComponent(btnCancel)
			)
		);
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(tbpTabs)
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
	
	private JComponent buildCommonSettings() {
		JComponent commonSettings = new JPanel();
		GroupLayout layout = new GroupLayout(commonSettings);
		commonSettings.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		// Labels:
		JLabel lblRemoveWriteOnly = new JLabel("Fix Writeonly");
		JLabel lblAutosave = new JLabel("Autosave");
//		JLabel lblCheckForUpdates = new JLabel("Updates");
		
		/* GroupLayout konfigurieren: */
		// Vertikales Layout:
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(lblRemoveWriteOnly)
				.addComponent(chkRemoveWriteOnly)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(lblAutosave)
				.addComponent(chkAutosaveWhenHotkeys)
			)
//			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
//				.addComponent(lblCheckForUpdates)
//				.addComponent(chkCheckForUpdates)
//			)
		);
		
		// Horizontales Layout:
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
					.addComponent(lblRemoveWriteOnly)
					.addComponent(lblAutosave)
//					.addComponent(lblCheckForUpdates)
				)
				.addGroup(layout.createParallelGroup()
					.addComponent(chkRemoveWriteOnly)
					.addComponent(chkAutosaveWhenHotkeys)
//					.addComponent(chkCheckForUpdates)
				)
			)		
		);
		
		return commonSettings;
	}
	
	private JComponent buildImportSettings() {
		JComponent importSettings = new JPanel();
		GroupLayout layout = new GroupLayout(importSettings);
		importSettings.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		// ComboBox:
		comCddbHost.setEditable(true);
		
		// Labels:
		JLabel lblCDDBHost = new JLabel("CDDB Host");
		
		/* GroupLayout konfigurieren: */
		// Vertikales Layout:
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(lblCDDBHost)
				.addComponent(comCddbHost)
			)
		);
		
		// Horizontales Layout:
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
					.addComponent(lblCDDBHost)
				)
				.addGroup(layout.createParallelGroup()
					.addComponent(comCddbHost)
				)
			)		
		);
		
		return importSettings;
	}
	
	private JComponent buildProxySettings() {
		JComponent proxySettings = new JPanel();
		GroupLayout layout = new GroupLayout(proxySettings);
		proxySettings.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		// Labels:
		JLabel lblUseProxy = new JLabel("Use proxy");
		JLabel lblProxyHost = new JLabel("Host");
		JLabel lblProxyPort = new JLabel("Port");
		
		// Use-Checkbox:
		chkUseProxy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean enabled = chkUseProxy.isSelected();
				txtProxyHost.setEnabled(enabled);
				txtProxyPort.setEnabled(enabled);
			}
		});
		
		
		/* GroupLayout konfigurieren: */
		// Vertikales Layout:
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(lblUseProxy)
				.addComponent(chkUseProxy)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(lblProxyHost)
				.addComponent(txtProxyHost)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(lblProxyPort)
				.addComponent(txtProxyPort)
			)
		);
		
		// Horizontales Layout:
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
					.addComponent(lblUseProxy)
					.addComponent(lblProxyHost)
					.addComponent(lblProxyPort)
				)
				.addGroup(layout.createParallelGroup()
					.addComponent(chkUseProxy)
					.addComponent(txtProxyHost)
					.addComponent(txtProxyPort)
				)
			)		
		);
		
		return proxySettings;
	}
	
	private void readSettings() {
		Settings settings = Settings.getInstance();
		
		/* Common: */
		chkRemoveWriteOnly.setSelected(settings.getBoolean(Settings.KEY_COMMON_TRY_REMOVE_WRITEONLY));
		chkAutosaveWhenHotkeys.setSelected(settings.getBoolean(Settings.KEY_COMMON_AUTOSAVE_WHEN_HOTKEYS));
		chkCheckForUpdates.setSelected(settings.getBoolean(Settings.KEY_COMMON_CHECK_FOR_UPDATES));
		
		/* Import: */
		comCddbHost.setSelectedItem(settings.getString(Settings.KEY_CDDB_HOST));
		
		/* Network: */
		chkUseProxy.setSelected(settings.getBoolean(Settings.KEY_PROXY_USE));
		txtProxyHost.setText(settings.getString(Settings.KEY_PROXY_HOST));
		txtProxyPort.setText(settings.getString(Settings.KEY_PROXY_PORT));
		txtProxyHost.setEnabled(chkUseProxy.isSelected());
		txtProxyPort.setEnabled(chkUseProxy.isSelected());
	}
	
	private void saveSettings() {
		
		Runnable saver = new Runnable() {
			@Override
			public void run() {
				Settings settings = Settings.getInstance();
				
				/* Common: */
				settings.setBoolean(Settings.KEY_COMMON_TRY_REMOVE_WRITEONLY, chkRemoveWriteOnly.isSelected());
				settings.setBoolean(Settings.KEY_COMMON_AUTOSAVE_WHEN_HOTKEYS, chkAutosaveWhenHotkeys.isSelected());
				settings.setBoolean(Settings.KEY_COMMON_CHECK_FOR_UPDATES, chkCheckForUpdates.isSelected());
				
				/* Import: */
				settings.setString(Settings.KEY_CDDB_HOST, comCddbHost.getSelectedItem().toString());
				
				/* Network: */
				settings.setBoolean(Settings.KEY_PROXY_USE, chkUseProxy.isSelected());
				settings.setString(Settings.KEY_PROXY_HOST, txtProxyHost.getText());
				settings.setString(Settings.KEY_PROXY_PORT, txtProxyPort.getText());
				
				/* In Datei schreiben: */
				settings.storeSettings();
			}
		};
		
		WaitDialog waitDialog = new WaitDialog(
				SwingUtilities.getWindowAncestor(this),
				"Apply Settings",
				"Applying settings. Please wait...",
				saver);
		waitDialog.setVisible(true);

	}
	
	private boolean verifySettings() {
		boolean ok = true;
		
		if(comCddbHost.getSelectedItem().toString().isEmpty()) {
			JOptionPane.showMessageDialog(
					PreferencesDialog.this,
					"Please enter/select a valid CDDB host!",
					"CDDB Host",
					JOptionPane.ERROR_MESSAGE);
			
			ok = false;
			tbpTabs.setSelectedIndex(TAB_IMPORT);
			comCddbHost.requestFocusInWindow();
		}
		
		return ok;
	}
	
	// Hilfsklassen ------------------------------------------------------------
	
	
	// Actions -----------------------------------------------------------------
	private class OKAction extends AbstractAction {
		
		private static final long serialVersionUID = -6012803321647683416L;

		public OKAction() {
			super("Apply");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if(verifySettings()) {
				saveSettings();
				dispose();				
			}
		}
	}
	
	private class CancelAction extends AbstractAction {

		private static final long serialVersionUID = 6175555201509266861L;

		public CancelAction() {
			super("Cancel");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			dispose();
		}
	}
	
}
