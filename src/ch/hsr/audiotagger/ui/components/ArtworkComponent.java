package ch.hsr.audiotagger.ui.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import ch.hsr.audiotagger.ui.GUIHelper;

/**
 * Zeigt ein Cover sowie dessen Dimensionen an.
 * 
 * @author manuelalabor
 */
public class ArtworkComponent extends JComponent {

	private BufferedImage artwork = null;
	private boolean changed = false;
	
	private final ResizedArtworkDisplay resizedArtworkDisplay = new ResizedArtworkDisplay();
	private final JLabel lblDimension = new JLabel("", SwingConstants.CENTER);
	
	private final static long serialVersionUID = -3466505660576591200L;
	
	// Konstruktoren -----------------------------------------------------------
	public ArtworkComponent(BufferedImage coverImage) {
		setArtwork(coverImage);
		buildGui();
		
		changed = false;
	}
	
	public ArtworkComponent() {
		this(null);
	}
	
	private void buildGui() {
		lblDimension.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
		
		setLayout(new BorderLayout());
		add(resizedArtworkDisplay, BorderLayout.NORTH);
		add(lblDimension, BorderLayout.SOUTH);
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		lblDimension.setEnabled(enabled);
		resizedArtworkDisplay.setEnabled(enabled);
	}
	
	// Zugriff -----------------------------------------------------------------
	public void setArtwork(BufferedImage artwork) {
		this.artwork = artwork;
		resizedArtworkDisplay.setCoverImage(artwork);
		
		if(artwork != null) {
			lblDimension.setText(artwork.getWidth() + "x" + artwork.getHeight() + "px");
		} else {
			lblDimension.setText("");
		}
		
		changed = true;
	}
	
	public BufferedImage getArtwork() {
		return this.artwork;
	}
	
	/**
	 * Beschreibt, ob das Cover bearbeitet/geändert wurde.
	 * 
	 * @return boolean
	 */
	public boolean hasChanged() {
		return changed;
	}
	
	public void setChanged(boolean changed) {
		this.changed = changed;
	}
	
	// Hilfsklassen ------------------------------------------------------------
	/**
	 * Zeigt ein verkleinertes Cover an.
	 */
	private class ResizedArtworkDisplay extends JComponent {
		
		private BufferedImage resizedCoverImage = null;
		private int maxCoverSize = 150;
		
		private static final long serialVersionUID = -2747810725771727733L;
		
		public ResizedArtworkDisplay() {
			setBackground(UIManager.getColor("TextField.background"));
			setBorder(UIManager.getBorder("TextField.border"));
			
			
			Insets insets = getInsets();
			int inset = insets.left;
			if(inset < insets.top) inset = insets.top;
			
			setPreferredSize(new Dimension(maxCoverSize+2*inset, maxCoverSize+2*inset));
			setSize(getPreferredSize());
			setMaximumSize(getPreferredSize());
			
		}
		
		public void setCoverImage(BufferedImage coverImage) {
			if(coverImage != null) {
				this.resizedCoverImage = GUIHelper.resizeImage(coverImage, maxCoverSize, maxCoverSize);
			} else {
				this.resizedCoverImage = null;
			}
			
			repaint();
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
			
			if(artwork != null) {
				int x = (getWidth() - resizedCoverImage.getWidth())/2;
				int y = (getHeight() - resizedCoverImage.getHeight())/2;
				g.drawImage(resizedCoverImage, x, y, null);
			}
			
			super.paintComponent(g);			
		}
	}
	
}
