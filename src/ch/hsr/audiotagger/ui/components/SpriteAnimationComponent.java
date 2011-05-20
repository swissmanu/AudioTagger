package ch.hsr.audiotagger.ui.components;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.Timer;

/**
 * Zeigt eine Bilddatei mit Sprites als Animation an.
 * 
 * @author Manuel Alabor
 * @see http://de.wikipedia.org/wiki/Sprite_(Computergrafik)
 */
public class SpriteAnimationComponent extends JComponent {

	private BufferedImage animationImage = null;
	private Timer animationTimer = new Timer(40, new AnimationActionListener());
	private int animationCycle = 0;
	private int frameWidth = 32;
	private int frameHeight = 32;
	private int totalFrames = 31;
	
	private static final long serialVersionUID = -1389426942075855238L;
	
	/**
	 * @param animationImage BufferedImage mit allen Frames
	 * @param frameWidth Breite eines Frames
	 * @param frameHeight Hšhe eines Frames
	 * @param totalFrames Totale Anzahl Frames
	 */
	public SpriteAnimationComponent(BufferedImage animationImage, int frameWidth, int frameHeight, int totalFrames) {
		this.animationImage = animationImage;
		this.frameWidth = frameWidth;
		this.frameHeight = frameHeight;
		this.totalFrames = totalFrames;
		
		setPreferredSize(new Dimension(frameWidth, frameHeight));
		
		animationTimer.start();
	}
	
	
	// Grafikausgabe -----------------------------------------------------------
	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		
		g2.drawImage(animationImage,
				0, 0, frameWidth, frameHeight,
				animationCycle*frameWidth, 0, (animationCycle*frameWidth)+frameWidth, frameHeight,
				null);
	}
	
	
	// Hilfsklassen ------------------------------------------------------------
	private class AnimationActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if(animationCycle >= (totalFrames-1)) animationCycle = -1;
			
			animationCycle++;
			repaint();
		}
	}
	
}
