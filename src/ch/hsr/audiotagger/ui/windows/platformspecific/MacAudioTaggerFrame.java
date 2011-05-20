package ch.hsr.audiotagger.ui.windows.platformspecific;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ch.hsr.audiotagger.io.FavoriteFolders;
import ch.hsr.audiotagger.io.FavoriteFolders.Favorite;
import ch.hsr.audiotagger.ui.GUIHelper;
import ch.hsr.audiotagger.ui.GUIImageManager;
import ch.hsr.audiotagger.ui.components.BetterJTable;
import ch.hsr.audiotagger.ui.windows.AudioTaggerFrame;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;
import com.explodingpixels.macwidgets.MacWidgetFactory;
import com.explodingpixels.macwidgets.SourceList;
import com.explodingpixels.macwidgets.SourceListCategory;
import com.explodingpixels.macwidgets.SourceListControlBar;
import com.explodingpixels.macwidgets.SourceListItem;
import com.explodingpixels.macwidgets.SourceListModel;
import com.explodingpixels.macwidgets.SourceListSelectionListener;
import com.explodingpixels.macwidgets.UnifiedToolBar;

/**
 * @author Manuel Alabor
 * @see http://developer.apple.com/mac/library/technotes/tn2007/tn2196.html
 */
public class MacAudioTaggerFrame extends AudioTaggerFrame {

	private final JDialog dlgInspector = new JDialog(this, "Editor");
	
	private final static int MIN_WIDTH = 700;
	private final static  int MIN_HEIGHT = 400;
	private final static long serialVersionUID = 5094299414812917728L;	
	
	private final static String APPLE_BRUSH_METAL_LOOK = "apple.awt.brushMetalLook";
	private final static String APPLE_WINDOW_STYLE = "Window.style";
	private final static String APPLE_BUTTON_TYPE = "JButton.buttonType";
	
	public MacAudioTaggerFrame() {
		super();
		
		setSize(MIN_WIDTH, MIN_HEIGHT);
		getRootPane().putClientProperty(APPLE_BRUSH_METAL_LOOK, Boolean.TRUE);
		GUIHelper.centerOnScreen(this);
		setContentPane(buildGui());
		
		initInspector();
		
		Application.getApplication().setEnabledPreferencesMenu(true);
		Application.getApplication().addApplicationListener(new ApplicationAdapter() {
			@Override
			public void handleQuit(ApplicationEvent arg0) {
				actions.get(ACTION_QUIT).actionPerformed(new ActionEvent(MacAudioTaggerFrame.this, 0, "MacQuitMenuClicked"));
			}
			@Override
			public void handlePreferences(ApplicationEvent applicationEvent) {
				actions.get(ACTION_PREFERENCES).actionPerformed(new ActionEvent(MacAudioTaggerFrame.this, 0, "MacSettingsMenuClicked"));
				applicationEvent.setHandled(true);
			}
			
			@Override
			public void handleAbout(ApplicationEvent applicationEvent) {
				actions.get(ACTION_HELP_ABOUT).actionPerformed(new ActionEvent(MacAudioTaggerFrame.this, 0, "MacAboutMenuClicked"));
				applicationEvent.setHandled(true);
			}
		});
		
		addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				if(arg.equals(AudioTaggerFrame.OBSERVABLE_FILES_CHANGED)) {
					getRootPane().putClientProperty("Window.documentFile", getSelectedFolder());
				}
			}
		});
		
		tblAudioFiles.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(tblAudioFiles.getModel().getRowCount() > 0 && !dlgInspector.isVisible()) {
					dlgInspector.setVisible(true);
				}
			}
		});
		
		loadFilesIntoTable(new File("/Users/manuelalabor/Music/"));
	}
	
	// GUI-Erstellung ----------------------------------------------------------
	private JComponent buildGui() {
		JComponent gui = new JPanel(new BorderLayout());
		
		gui.add(buildToolbar(), BorderLayout.NORTH);
		gui.add(buildMainUI(), BorderLayout.CENTER);
//		gui.add(buildBottomBar(), BorderLayout.SOUTH);
		
		return gui;
	}
	
	private JComponent buildMainUI() {
		JComponent ui = new JPanel(new BorderLayout());
		
		
		SourceList sourceList = buildSourceList();
		
		JSplitPane splitter = MacWidgetFactory.createSplitPaneForSourceList(sourceList, BetterJTable.createStripedJScrollPane(tblAudioFiles));
		splitter.setDividerLocation(200);
		ui.add(splitter, BorderLayout.CENTER);
		
		return ui;
	}
	
	private JComponent buildToolbar() {
		
		UnifiedToolBar toolbar = new UnifiedToolBar();
		toolbar.addComponentToLeft(buildToolbarButton(actions.get(ACTION_REFRESH)));
		
		toolbar.addComponentToCenter(buildToolbarButton(actions.get(ACTION_TAGS_IMPORT)));
		toolbar.addComponentToCenter(buildToolbarButton(actions.get(ACTION_ARTWORK_IMPORT)));
		toolbar.addComponentToCenter(buildToolbarButton(actions.get(ACTION_TAGS_EDIT_TRACKNUMBERS)));
		
		toolbar.disableBackgroundPainter();
		toolbar.installWindowDraggerOnWindow(this);
		
		return toolbar.getComponent();
	}
	
	
	private SourceList buildSourceList() {
		/* Vorbereiten: */
		JFileChooser chooser = new JFileChooser();
		SourceListModel model = new SourceListModel();
		
		/* Systemordner hinzufügen: */
		SourceListCategory systemCat = new SourceListCategory("System");
		model.addCategory(systemCat);
		model.addItemToCategory(new SourceListItem("Desktop", chooser.getIcon(new File("/Users/manuelalabor/Desktop/"))), systemCat);
		model.addItemToCategory(new SourceListItem("Music", chooser.getIcon(new File("/Users/manuelalabor/Music/"))), systemCat);
		
		/* Favorites hinzufügen: */
		SourceListCategory favoritesCat = new SourceListCategory("Favorites");
		model.addCategory(favoritesCat);
		
		FavoriteFolders favorites = FavoriteFolders.getInstance();
		for(int i = 0, l = favorites.getFavoriteCount(); i < l; i++) {
			Favorite favorite = favorites.getFavorite(i);
			if(favorite.getFile().exists()) {
				Icon icon = chooser.getIcon(favorite.getFile());
				model.addItemToCategory(new SourceListItem(favorite.getName(), icon), favoritesCat);	
			}
		}
		
		
		/* SourceList erstellen und anpassen: */
		SourceList sourceList = new SourceList(model);
		sourceList.addSourceListSelectionListener(new SourceListSelectionListener() {
			@Override
			public void sourceListItemSelected(SourceListItem item) {
				String name = item.getText();
				
				if(name.equals("Desktop")) {
					loadFilesIntoTable(new File("/Users/manuelalabor/Desktop/"));
				} else if(name.equals("Music")) {
					loadFilesIntoTable(new File("/Users/manuelalabor/Music/"));
				} else {
					FavoriteFolders favorites = FavoriteFolders.getInstance();
					for(int i = 0, l = favorites.getFavoriteCount(); i < l; i++) {
						if(favorites.getFavorite(i).getName().equals(name)) {
							loadFilesIntoTable(favorites.getFavorite(i).getFile());
							return;
						}
					}
					
					
				}
			}
		});
		SourceListControlBar controlBar = new SourceListControlBar();
		controlBar.createAndAddButton(new ImageIcon(Toolkit.getDefaultToolkit().getImage("NSImage://NSAddTemplate")), null);
		controlBar.createAndAddButton(new ImageIcon(Toolkit.getDefaultToolkit().getImage("NSImage://NSRemoveTemplate")), null);
		sourceList.installSourceListControlBar(controlBar);
		
		
		
		return sourceList;
	}
	
	private void initInspector() {
		dlgInspector.getRootPane().putClientProperty(APPLE_WINDOW_STYLE, "small");
		dlgInspector.setContentPane(pnlAudioFileEditor);
		dlgInspector.pack();
		dlgInspector.setResizable(false);
		dlgInspector.setLocation(getLocation().x + getWidth() + 10, getLocation().y);
		
		for(Component comp: dlgInspector.getContentPane().getComponents()) {
			JComponent component = (JComponent)comp;
			component.putClientProperty("JComponent.sizeVariant", "small");
			
			if(component instanceof JComboBox) {
				component.putClientProperty("JComboBox.isPopDown", Boolean.TRUE);
				component.putClientProperty("JComboBox.isSquare", Boolean.TRUE);
			}
		}
	}
	
	// Hilfsmethoden -----------------------------------------------------------
	private JButton buildToolbarButton(Action action) {
		JButton button = new JButton(action);
		button.putClientProperty(APPLE_BUTTON_TYPE, "toolbar");
		button.setHorizontalTextPosition(SwingConstants.CENTER);
		button.setVerticalTextPosition(SwingConstants.BOTTOM);
		button.setFocusable(false);
		
		return button;
	}
	
	// Initialisierung ---------------------------------------------------------
	@Override
	protected void customizeActions() {
		actions.get(ACTION_REFRESH).putValue(Action.LARGE_ICON_KEY, GUIImageManager.getInstance().getImageIcon("mac/toolbar/refresh.png"));
		
		actions.get(ACTION_ARTWORK_IMPORT).putValue(Action.LARGE_ICON_KEY, GUIImageManager.getInstance().getImageIcon("mac/toolbar/import_artwork.png"));
		actions.get(ACTION_ARTWORK_IMPORT).putValue(Action.NAME, "Artwork");
		
		actions.get(ACTION_TAGS_IMPORT).putValue(Action.LARGE_ICON_KEY, GUIImageManager.getInstance().getImageIcon("mac/toolbar/import_tags.png"));
		actions.get(ACTION_TAGS_IMPORT).putValue(Action.NAME, "Tags");
		
		actions.get(ACTION_TAGS_EDIT_TRACKNUMBERS).putValue(Action.LARGE_ICON_KEY, GUIImageManager.getInstance().getImageIcon("mac/toolbar/tracknumbers.png"));
		actions.get(ACTION_TAGS_EDIT_TRACKNUMBERS).putValue(Action.NAME, "Tracknumb.");
	}
	
}
