package ch.hsr.audiotagger.ui.windows.platformspecific;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import ch.hsr.audiotagger.ui.GUIHelper;
import ch.hsr.audiotagger.ui.GUIImageManager;
import ch.hsr.audiotagger.ui.components.BetterJTable;
import ch.hsr.audiotagger.ui.components.FileTree;
import ch.hsr.audiotagger.ui.components.jgoodies.SimpleInternalFrame;
import ch.hsr.audiotagger.ui.components.jgoodies.UIFSplitPane;
import ch.hsr.audiotagger.ui.windows.AudioTaggerFrame;

import com.jgoodies.looks.Options;

public class UniversalAudioTaggerFrame extends AudioTaggerFrame {
	
	private final static JTabbedPane tbpTabs = new JTabbedPane();
	private final static FileTree treFolderBrowser = new FileTree();
	
	private final static int TAB_BROWSE = 0;
	private final static int TAB_EDIT = 1;
	private final static int MIN_WIDTH = 800;
	private final static int MIN_HEIGHT = 520;
	
	private static final long serialVersionUID = -2189184920290241075L;
	
	public UniversalAudioTaggerFrame() {
		super();
		setSize(MIN_WIDTH, MIN_HEIGHT);
		GUIHelper.centerOnScreen(this);
		setIconImage(GUIImageManager.getInstance().getImage("appicons/small_icon.png"));
		
		addComponentListener(new ComponentAdapter() {
		    public void componentResized(final ComponentEvent e) {
		        int width = getWidth();
		        int height = getHeight();
		        boolean resize = false;
		        
		        if (width < MIN_WIDTH) {
		            resize = true;
		            width = MIN_WIDTH;
		        }
		        if (height < MIN_HEIGHT) {
		            resize = true;
		            height = MIN_HEIGHT;
		        }
		        
		        if (resize) { setSize(width, height); }
		    }
		});
		
		addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				if(arg.equals(AudioTaggerFrame.OBSERVABLE_FILES_CHANGED)) {
					setTitle("AudioTagger - [" + getSelectedFolder().getPath() + "]");
				}
			}
		});
		
		setContentPane(buildGui());
		setJMenuBar(buildMenuBar());
	}
	
	
	// GUI-Erstellung ----------------------------------------------------------
	/**
	 * Erstellt und initialisiert alle Komponenten im Hauptfenster und gibt
	 * diese auf einer {@link JComponent} angeordnet zurück.
	 * 
	 * @return JComponent
	 */
	private JComponent buildGui() {
		JPanel container = new JPanel(new BorderLayout());
		initKeyBindings(container);
		
		/* Vorbereiten: */
		// Ordnerbrowser:
		// Stellt sicher, dass der ausgewählte Ordner im Browser nach Audiofiles
		// durchsucht wird.
		treFolderBrowser.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				TreePath path = e.getPath();
				if(path != null) {
					FileTree tree = (FileTree)e.getSource();
					File folder = tree.getSelectedFile();
					setSelectedFolder(folder);
					
					loadFilesIntoTable(folder);
				}
			}
		});
		
		// Dateitabelle:
		tblAudioFiles.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// Warum so kompliziert?
				// Wenn die Abfragen aufs Event fehlen, wird beim "löschen" der aktuellen
				// Selektion der Tabelle ebenfalls der Tab gewechselt.
				if(!e.getValueIsAdjusting() && e.getFirstIndex() != -1 && tbpTabs.getSelectedIndex() != TAB_EDIT)  {
					tbpTabs.setSelectedIndex(TAB_EDIT);
				}
			}
		});
		
		// Tabs:
		// Ist der Browse-Tab ausgewählt, darf der User die Action SaveTags
		// nicht verwenden.
		tbpTabs.setFocusable(false);
		tbpTabs.addChangeListener(new ChangeListener()  {
			public void stateChanged(ChangeEvent e) {
				int selectedTab = tbpTabs.getSelectedIndex();
				if(selectedTab == TAB_BROWSE) {
					enableActions(false);
					treFolderBrowser.requestFocusInWindow();
				} else {
					if(tblAudioFiles.getSelectedRowCount() > 0) {
						enableActions(true);
					}
				}
			}
		});
		
		/* Zusammensetzen: */
		tbpTabs.add(new JScrollPane(treFolderBrowser), "Browse");
		tbpTabs.add(pnlAudioFileEditor, "Edit");
		tbpTabs.putClientProperty(Options.NO_CONTENT_BORDER_KEY, Boolean.TRUE);
		
		SimpleInternalFrame leftFrame = new SimpleInternalFrame("Manager", null, tbpTabs);
		SimpleInternalFrame rightFrame = new SimpleInternalFrame("Audio files", null, BetterJTable.createStripedJScrollPane(tblAudioFiles));
		
		JSplitPane splitter = UIFSplitPane.createStrippedSplitPane(
				JSplitPane.HORIZONTAL_SPLIT,
				leftFrame,
				rightFrame);
		splitter.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		container.add(buildToolBar(), BorderLayout.NORTH);
		container.add(splitter, BorderLayout.CENTER);
		
		
		return container;
	}
	
	protected JComponent initKeyBindings(JComponent onComponent) {
		super.initKeyBindings(onComponent);
		
		/* JFileTree KeyBindings: */
		// [Enter] : Falls Audiofiles vorhanden, das erste im Editor anzeigen
		InputMap inputMap = treFolderBrowser.getInputMap(JComponent.WHEN_FOCUSED);
		inputMap.put(KeyStroke.getKeyStroke("ENTER"), "selectFolder");
		ActionMap actionMap = treFolderBrowser.getActionMap();
		actionMap.put("selectFolder", new AbstractAction() {
			private static final long serialVersionUID = -4140773433264378878L;
			@Override
			public void actionPerformed(ActionEvent e) {
				if(tblAudioFiles.getRowCount() > 0) {
					tblAudioFiles.getSelectionModel().setSelectionInterval(0, 0);
					pnlAudioFileEditor.setFocusOnFirstComponent();
				}
			}
		});
		
		return onComponent;
	}
	
	/**
	 * Erstellt die Toolbar mit allen zugehörigen Buttons.
	 * 
	 * @return
	 */
	private JComponent buildToolBar() {
		JToolBar toolbar = new JToolBar();
		toolbar.setRollover(true);
		
		toolbar.add(actions.get(ACTION_REFRESH));
		toolbar.addSeparator();
		toolbar.add(actions.get(ACTION_TAGS_SAVE));
		toolbar.addSeparator();
		toolbar.add(actions.get(ACTION_TAGS_IMPORT));
		toolbar.add(actions.get(ACTION_TAGS_TO_FILENAME));
		toolbar.add(actions.get(ACTION_TAGS_EDIT_TRACKNUMBERS));
		toolbar.addSeparator();
		toolbar.add(actions.get(ACTION_ARTWORK_IMPORT));
		toolbar.add(actions.get(ACTION_ARTWORK_SELECT_LOCAL));
		toolbar.add(actions.get(ACTION_ARTWORK_REMVOE));
		
		return toolbar;
	}
	
	private JMenuBar buildMenuBar() {
		JMenuBar menu = new JMenuBar();
		
		JMenu mnuFile = menu.add(new JMenu("File"));
		mnuFile.add(actions.get(ACTION_REFRESH));
		mnuFile.add(actions.get(ACTION_TAGS_SAVE));
		mnuFile.addSeparator();
		mnuFile.add(actions.get(ACTION_PREFERENCES ));
		mnuFile.add(actions.get(ACTION_CHECK_FOR_UPDATES));
		mnuFile.addSeparator();
		mnuFile.add(actions.get(ACTION_QUIT));
		
		JMenu mnuGoTo = menu.add(new JMenu("Goto"));
		mnuGoTo.add(actions.get(ACTION_GOTO_PREV_FILE));
		mnuGoTo.add(actions.get(ACTION_GOTO_NEXT_FILE));
		mnuGoTo.addSeparator();
		mnuGoTo.add(actions.get(ACTION_GOTO_PREV_FOLDER));
		mnuGoTo.add(actions.get(ACTION_GOTO_NEXT_FOLDER));

		JMenu mnuTags = menu.add(new JMenu("Tags"));
		mnuTags.add(actions.get(ACTION_TAGS_IMPORT));
		mnuTags.add(actions.get(ACTION_TAGS_TO_FILENAME));
		mnuTags.addSeparator();
		mnuTags.add(actions.get(ACTION_TAGS_EDIT_TRACKNUMBERS));
		
		
		JMenu mnuArtwork = menu.add(new JMenu("Artwork"));
		mnuArtwork.add(actions.get(ACTION_ARTWORK_IMPORT));
		mnuArtwork.addSeparator();
		mnuArtwork.add(actions.get(ACTION_ARTWORK_SELECT_LOCAL));
		mnuArtwork.add(actions.get(ACTION_ARTWORK_REMVOE));

		JMenu mnuHelp = menu.add(new JMenu("Help"));
		mnuHelp.add(actions.get(ACTION_HELP_ABOUT));
		
		return menu;
	}
	
	// Überschreibungen --------------------------------------------------------
	@Override
	protected void enableActions(boolean enableTagActions) {
		if(tbpTabs.getSelectedIndex() == TAB_BROWSE) enableTagActions = false;
		super.enableActions(enableTagActions);
	}
	
	@Override
	protected void enableControls(boolean enabled) {
		super.enableControls(enabled);
		treFolderBrowser.setEnabled(enabled);
		tbpTabs.setEnabled(enabled);
		
		if(enabled) treFolderBrowser.requestFocus(); // gefährlich!
	}
	
	// Action-Anpassungen ------------------------------------------------------
	@Override
	protected void customizeActions() {
		actions.get(ACTION_QUIT).putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Q"));
		
		actions.get(ACTION_TAGS_SAVE).putValue(Action.LARGE_ICON_KEY, GUIImageManager.getInstance().getImageIcon("universal/disk.png"));
		actions.get(ACTION_TAGS_SAVE).putValue(Action.SMALL_ICON, GUIImageManager.getInstance().getImageIcon("universal/disk.png"));
		actions.get(ACTION_TAGS_SAVE).putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
		
		actions.get(ACTION_PREFERENCES).putValue(Action.LARGE_ICON_KEY, GUIImageManager.getInstance().getImageIcon("universal/control_equalizer_blue.png"));
		actions.get(ACTION_PREFERENCES).putValue(Action.SMALL_ICON, GUIImageManager.getInstance().getImageIcon("universal/control_equalizer_blue.png"));
		actions.get(ACTION_PREFERENCES).putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, KeyEvent.CTRL_MASK));
		
		actions.get(ACTION_REFRESH).putValue(Action.LARGE_ICON_KEY, GUIImageManager.getInstance().getImageIcon("universal/arrow_refresh.png"));
		actions.get(ACTION_REFRESH).putValue(Action.SMALL_ICON, GUIImageManager.getInstance().getImageIcon("universal/arrow_refresh.png"));
		actions.get(ACTION_REFRESH).putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK));
		
		actions.get(ACTION_ARTWORK_IMPORT).putValue(Action.LARGE_ICON_KEY, GUIImageManager.getInstance().getImageIcon("universal/picture_link.png"));
		actions.get(ACTION_ARTWORK_IMPORT).putValue(Action.SMALL_ICON, GUIImageManager.getInstance().getImageIcon("universal/picture_link.png"));
		actions.get(ACTION_ARTWORK_IMPORT).putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK));
		
		actions.get(ACTION_ARTWORK_SELECT_LOCAL).putValue(Action.LARGE_ICON_KEY, GUIImageManager.getInstance().getImageIcon("universal/picture_add.png"));
		actions.get(ACTION_ARTWORK_SELECT_LOCAL).putValue(Action.SMALL_ICON, GUIImageManager.getInstance().getImageIcon("universal/picture_add.png"));
		actions.get(ACTION_ARTWORK_SELECT_LOCAL).putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_MASK));
		
		actions.get(ACTION_ARTWORK_REMVOE).putValue(Action.LARGE_ICON_KEY, GUIImageManager.getInstance().getImageIcon("universal/picture_delete.png"));
		actions.get(ACTION_ARTWORK_REMVOE).putValue(Action.SMALL_ICON, GUIImageManager.getInstance().getImageIcon("universal/picture_delete.png"));
		actions.get(ACTION_ARTWORK_REMVOE).putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, KeyEvent.CTRL_MASK));

		actions.get(ACTION_TAGS_IMPORT).putValue(Action.LARGE_ICON_KEY, GUIImageManager.getInstance().getImageIcon("universal/page_link.png"));
		actions.get(ACTION_TAGS_IMPORT).putValue(Action.SMALL_ICON, GUIImageManager.getInstance().getImageIcon("universal/page_link.png"));
		actions.get(ACTION_TAGS_IMPORT).putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_MASK));
		
		actions.get(ACTION_TAGS_TO_FILENAME).putValue(Action.LARGE_ICON_KEY, GUIImageManager.getInstance().getImageIcon("universal/page_edit.png"));
		actions.get(ACTION_TAGS_TO_FILENAME).putValue(Action.SMALL_ICON, GUIImageManager.getInstance().getImageIcon("universal/page_edit.png"));
		actions.get(ACTION_TAGS_TO_FILENAME).putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK));
		
		actions.get(ACTION_TAGS_EDIT_TRACKNUMBERS).putValue(Action.LARGE_ICON_KEY, GUIImageManager.getInstance().getImageIcon("universal/lightning.png"));
		actions.get(ACTION_TAGS_EDIT_TRACKNUMBERS).putValue(Action.SMALL_ICON, GUIImageManager.getInstance().getImageIcon("universal/lightning.png"));
		actions.get(ACTION_TAGS_EDIT_TRACKNUMBERS).putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_MASK));

		actions.get(ACTION_GOTO_NEXT_FILE).putValue(Action.LARGE_ICON_KEY, GUIImageManager.getInstance().getImageIcon("universal/arrow_down.png"));
		actions.get(ACTION_GOTO_NEXT_FILE).putValue(Action.SMALL_ICON, GUIImageManager.getInstance().getImageIcon("universal/arrow_down.png"));
		actions.get(ACTION_GOTO_NEXT_FILE).putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.CTRL_MASK + KeyEvent.ALT_MASK));

		actions.get(ACTION_GOTO_PREV_FILE).putValue(Action.LARGE_ICON_KEY, GUIImageManager.getInstance().getImageIcon("universal/arrow_up.png"));
		actions.get(ACTION_GOTO_PREV_FILE).putValue(Action.SMALL_ICON, GUIImageManager.getInstance().getImageIcon("universal/arrow_up.png"));
		actions.get(ACTION_GOTO_PREV_FILE).putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.CTRL_MASK + KeyEvent.ALT_MASK));
		
		actions.get(ACTION_GOTO_NEXT_FOLDER).putValue(Action.LARGE_ICON_KEY, GUIImageManager.getInstance().getImageIcon("universal/arrow_right.png"));
		actions.get(ACTION_GOTO_NEXT_FOLDER).putValue(Action.SMALL_ICON, GUIImageManager.getInstance().getImageIcon("universal/arrow_right.png"));
		actions.get(ACTION_GOTO_NEXT_FOLDER).putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_MASK + KeyEvent.ALT_MASK));

		actions.get(ACTION_GOTO_PREV_FOLDER).putValue(Action.LARGE_ICON_KEY, GUIImageManager.getInstance().getImageIcon("universal/arrow_left.png"));
		actions.get(ACTION_GOTO_PREV_FOLDER).putValue(Action.SMALL_ICON, GUIImageManager.getInstance().getImageIcon("universal/arrow_left.png"));
		actions.get(ACTION_GOTO_PREV_FOLDER).putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_MASK + KeyEvent.ALT_MASK));
	}
	
}
