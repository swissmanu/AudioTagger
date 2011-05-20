package ch.hsr.audiotagger.ui.windows;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.GroupLayout.Alignment;

import ch.hsr.audiotagger.io.artwork.AbstractArtworkProvider;
import ch.hsr.audiotagger.io.artwork.amazon.AmazonArtworkProvider;
import ch.hsr.audiotagger.ui.GUIHelper;

/**
 * Stellt einen {@link JDialog} zur Abfrage und Auswahl von Cover-Artworks zur
 * verfügung.<br/>
 * Nach dem Schliessen des Dialogs ist das gewählte Artwork per {@link #getSelectedArtwork()}
 * nach aussen verfügbar. (Wurde nichts ausgewählt oder wurde "Abbrechen" geklickt,
 * wird <code>null</code> zurückgegeben).
 * 
 * @author Manuel Alabor
 * @see AbstractArtworkProvider
 */
public class ArtworkSearchDialog extends JDialog {
	
	private BufferedImage selectedArtwork = null;
	
	private final static JComboBox comArtists = new JComboBox();
	private final static JComboBox comAlbums = new JComboBox();
	private final static JList lstArtworks = new JList(new DefaultListModel());
	private final static JLabel lblHint = new JLabel("");
	
	private final OKAction actionOK = new OKAction();
	private final CancelAction actionCancel = new CancelAction();
	private final FetchAction actionFetch = new FetchAction();
	
	private AbstractArtworkProvider artworkProvider = new AmazonArtworkProvider();
//	private AbstractArtworkProvider artworkProvider = new DummyArtworkProvider();
	
	private static final long serialVersionUID = 807737415198886386L;
	
	/**
	 * Ein {@link JDialog} zur Auswahl von Cover-Artworks.
	 * 
	 * @param owner {@link Frame}, zu welchem dieser {@link JDialog} gehört.
	 * @param artist Interpret, zu welchem das Artwork gesucht werden soll
	 * @param album Album, zu welchem das Artwork gesucht werden soll
	 */
	public ArtworkSearchDialog(Frame owner, String[] artists, String[] albums) {
		super(owner,"Search artwork");
		
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
		
		/* GUI erstellen: */
		// Erstellen:
		setContentPane(buildGui());
		
		// ComboBoxes befüllen:
		comArtists.setModel(new DefaultComboBoxModel(artists));
		comAlbums.setModel(new DefaultComboBoxModel(albums));
		
		/* Direkt Artworks laden & Dialog anzeigen: */
		fetchArtworks(comArtists.getSelectedItem().toString(), comAlbums.getSelectedItem().toString());
		setVisible(true);
	}
	
	public ArtworkSearchDialog(Frame owner) {
		this(owner, new String[]{}, new String[]{});
	}
	
	public ArtworkSearchDialog(Frame owner, String artist, String album) {
		this(owner, new String[]{artist}, new String[]{album});
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
		
		// Labels:
		JLabel lblArtist = new JLabel("Artist");
		JLabel lblAlbum = new JLabel("Album");
		
		// ComboBoxes:
		comArtists.setEditable(true);
		comAlbums.setEditable(true);
		
		// lstArtworks:
		lstArtworks.setCellRenderer(new ArtworkListCellRenderer());
		lstArtworks.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		lstArtworks.setVisibleRowCount(-1);
		lstArtworks.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lstArtworks.addMouseListener(new MouseAdapter() {  // Doppelklick-Listener
			@Override
			public void mouseClicked(MouseEvent e) {
				if(lstArtworks.getSelectedValue() != null
					&& e.getClickCount() == 2
					&& !e.isPopupTrigger()) {
					actionOK.actionPerformed(new ActionEvent(lstArtworks,0,"artworkSelected"));
				}
			}
		});
		JScrollPane scpArtwork = new JScrollPane(lstArtworks);
		
		
		// Buttons:
		JButton btnFetch = new JButton(actionFetch);
		JButton btnOK = new JButton(actionOK);
		JButton btnCancel = new JButton(actionCancel);
		getRootPane().setDefaultButton(btnOK);
		
		/* GroupLayout konfigurieren: */
		// Vertikales Layout:
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(lblArtist)
				.addComponent(comArtists)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(lblAlbum)
				.addComponent(comAlbums)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(scpArtwork)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(lblHint)
				.addComponent(btnFetch)
				.addComponent(btnOK)
				.addComponent(btnCancel)
			)
		);
		
		// Horizontales Layout:
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup()
						.addComponent(lblArtist)
						.addComponent(lblAlbum)
					)
					.addGroup(layout.createParallelGroup()
						.addComponent(comArtists)
						.addComponent(comAlbums)
					)
				)
			.addGroup(layout.createParallelGroup()
				.addComponent(scpArtwork)
				.addGroup(Alignment.LEADING, layout.createSequentialGroup()
					.addComponent(lblHint)
				)
				.addGroup(Alignment.TRAILING, layout.createSequentialGroup()
					.addGap(13)
					.addComponent(btnFetch)
					.addGap(13)
					.addComponent(btnOK)
					.addComponent(btnCancel)
				)
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
	
	/**
	 * Aktiviert bzw. deaktiviert GUI-Komponenten.
	 * 
	 * @param enabled
	 */
	private void updateGui(boolean enabled, int cursorType) {
		setCursor(new Cursor(cursorType));
		
		comArtists.setEnabled(enabled);
		comAlbums.setEnabled(enabled);
		lstArtworks.setEnabled(enabled);
		actionFetch.setEnabled(enabled);
		actionOK.setEnabled(enabled);
		actionCancel.setEnabled(enabled);
	}
	
	
	// Zugriff -----------------------------------------------------------------
	/**
	 * Gibt das gewählte Artwork zurück. Wurde der {@link ArtworkSearchDialog}
	 * mit btnCancel geschlossen, wird <code>null</code> zurückgegeben.
	 * 
	 * @return BufferedImage
	 */
	public BufferedImage getSelectedArtwork() {
		return selectedArtwork;
	}
	
	// Artworks holen ----------------------------------------------------------
	/**
	 * Holt zu einem Interpreten und einem Album passende Artworks aus dem
	 * Internet.
	 * 
	 * @param artsit
	 * @param album
	 * @return ArrayList mit Bild-URL's
	 */
	private void fetchArtworks(final String artist, final String album) {
		/* Vorbereiten: */
		// GUI vorbehalten:
		lblHint.setText("Fetching artworks from provider. Please wait...");
		updateGui(false, Cursor.WAIT_CURSOR);
		
		// Evtl. vorhandene Artworks im Speicher löschen:
		DefaultListModel model = (DefaultListModel)lstArtworks.getModel();
		model.clear();
		
		
		/* Runnable für Thread erstellen: */
		Runnable loader = new Runnable() {
			@Override
			public void run() {
				ArrayList<String> artworkUrls = new ArrayList<String>();
				DefaultListModel model = (DefaultListModel)lstArtworks.getModel();
				boolean error = false;
				
				/* Artworks holen: */
				try {
					artworkUrls = artworkProvider.searchArtwork(artist, album);					
				} catch(Exception e) {
					error = true;
				}
				
				
				/* Artwork URL's verarbeiten: */
				for(String urlString: artworkUrls) {
					try {
						URL url = new URL(urlString);
						BufferedImage artwork = ImageIO.read(url);
						
						ArtworkContainer artworkContainer = new ArtworkContainer(urlString, artwork);
						model.addElement(artworkContainer);							
					} catch(Exception e) {
						// do nothing; Artwork einfach nicht zur Liste
						// hinzufügen, denn:
						// Entweder war Artwork URL falsch, oder es konnte
						// einfach nicht geladen werden.
					}
				}				
				
				
				/* GUI aktualisieren: */
				updateGui(true, Cursor.DEFAULT_CURSOR);
				if(model.size() > 0) {
					lstArtworks.setSelectedIndex(0);
					lblHint.setText(model.getSize() + " artwork(s) fetched");
					lstArtworks.requestFocus();
				} else {
					if(!error) {
						lblHint.setText("No artwork found");
						actionOK.setEnabled(false);
						JOptionPane.showMessageDialog(
								ArtworkSearchDialog.this,
								"Could not find any artwork for selected audiofile(s).\n" +
								"Try to enter another artist and/or album and click \"Refetch artworks\".",
								"No artwork found",
								JOptionPane.ERROR_MESSAGE
						);
						comArtists.requestFocus();						
					} else {
						JOptionPane.showMessageDialog(
								ArtworkSearchDialog.this,
								"Could not retrive any artwork from the specified provider.\n" +
								"Please check your internet connection and have a look at the proxy settings.",
								"Artwork provider error",
								JOptionPane.ERROR_MESSAGE
						);
						dispose();
					}
				}
				
			}
		};
		
		Thread t = new Thread(loader);
		t.start();
	}
	
	
	// Hilfsklassen ------------------------------------------------------------
	/**
	 * Rendert die verfügbaren Artworks in einer {@link JList}.<br/>
	 * Hierbei wird das Bild in einer verkleinerten Version (Thumbnail) angezeigt
	 * sowie die Pixel-Dimensionen darunter.
	 */
	private class ArtworkListCellRenderer extends JComponent implements ListCellRenderer {
		
		private BufferedImage thumbnail = null;
		private String artworkDimension = "";
		private boolean selected = false;
		private JList list;
		
		private static final long serialVersionUID = 2439036352485568260L;
		private final static int DIMENSION_GAP = 22;
		
		public ArtworkListCellRenderer() {
			setPreferredSize(new Dimension(120,120 + DIMENSION_GAP));
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D)g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			if(this.selected) {
				g2.setColor(list.getSelectionBackground());
				g2.fillRoundRect(1, 1, getWidth()-2, getHeight()-2,9,9);
			}
			
			if(thumbnail != null) {
				int x = (getWidth()-thumbnail.getWidth())/2;
				int y = ((getHeight()-DIMENSION_GAP)-thumbnail.getHeight())/2;
				g2.drawImage(thumbnail, x,y, null);
				
				if(artworkDimension.length() > 0) {
					g2.setFont(UIManager.getFont("Label.font"));
					TextLayout textLayout = new TextLayout(artworkDimension, g2.getFont(), g2.getFontRenderContext());
					Rectangle2D bounds = textLayout.getBounds();
					g2.setColor(Color.black);
					textLayout.draw(g2, (float)(getWidth()-bounds.getWidth())/2, (float)(getHeight() - bounds.getHeight()));
				}
			}
		}
		
		@Override
		/**
		 * Speichert alle Informationen zur zum rendernden Item zwischen, damit
		 * <code>paintComponent</code> diese anschliessend rendern kann.
		 * 
		 * @param list
		 * @param value
		 * @param index
		 * @param isSelected
		 * @param cellHasFocus
		 * @return Component
		 */
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			ArtworkContainer artworkContainer = (ArtworkContainer)value;
			this.selected = isSelected;
			this.list = list;
			this.thumbnail = artworkContainer.getThumbnail();
			
			BufferedImage artwork = artworkContainer.getArtwork();
			if(artwork != null) artworkDimension = artwork.getWidth() + "x" + artwork.getHeight();
			else artworkDimension = "";
			
			return this;
		}
		
	}
	
	/**
	 * Speichert Informationen zu einem Artwork, geliefert von einem
	 * {@link AbstractArtworkProvider}.
	 * 
	 * @author Manuel Alabor
	 */
	private class ArtworkContainer {
		
		private String url;
		private BufferedImage artwork;
		private BufferedImage thumbnail;
		
		public ArtworkContainer(String url, BufferedImage artwork) {
			this.url = url;
			this.artwork = artwork;
		}
		
		/**
		 * Gibt die Artwork-URL als String zurück.
		 */
		@SuppressWarnings("unused")
		public String getUrlAsString() {
			return url;
		}
		
		/**
		 * Gibt eine URL-Instanz der Artwork-URL zurück.
		 * 
		 * @return URL
		 */
		@SuppressWarnings("unused")
		public URL getUrl() {
			URL realURL = null;
			
			try {
				realURL = new URL(this.url);
			} catch (MalformedURLException e) {
				realURL = null;
			}
			
			return realURL;
		}
		
		/**
		 * Gibt das Artwork aus diesem {@link ArtworkContainer} zurück.
		 * 
		 * @return BufferedImage
		 */
		public BufferedImage getArtwork() {
			return artwork;
		}
		
		/**
		 * Liefert das Thumbnail eines Artworks.<br/>
		 * Wurde das Thumbnail nicht bereits erstellt, wird es automatisch
		 * generiert.
		 * 
		 * @return BufferedImage
		 */
		public BufferedImage getThumbnail() {
			if(this.thumbnail == null) {
				BufferedImage thumbnail = GUIHelper.resizeImage(this.artwork, 110, 110);
				this.thumbnail = thumbnail;
			}
			
			return thumbnail;
		}
		
	}
	
	// Actions -----------------------------------------------------------------
	private class FetchAction extends AbstractAction {
		
		private static final long serialVersionUID = 3371273591885118846L;

		public FetchAction() {
			super("Refetch artwork");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			String artist = comArtists.getSelectedItem().toString();
			String album = comAlbums.getSelectedItem().toString();
			fetchArtworks(artist, album);
		}
	}
	
	private class OKAction extends AbstractAction {
		
		private static final long serialVersionUID = -6012803321647683416L;

		public OKAction() {
			super("Select");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			selectedArtwork = null;
			
			Object selected = lstArtworks.getSelectedValue();
			if(selected != null) {
				ArtworkContainer selectedArtworkContainer = (ArtworkContainer)lstArtworks.getSelectedValue();
				selectedArtwork = selectedArtworkContainer.getArtwork();
			}
			
			dispose();
		}
	}
	
	private class CancelAction extends AbstractAction {

		private static final long serialVersionUID = 6175555201509266861L;

		public CancelAction() {
			super("Cancel");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			selectedArtwork = null;
			dispose();
		}
	}
	
}
