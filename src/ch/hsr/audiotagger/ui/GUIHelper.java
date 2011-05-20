package ch.hsr.audiotagger.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * Bietet diverse Hilfsmethoden fŸr das GUI. 
 * 
 * @author Manuel Alabor
 */
public class GUIHelper {

	/**
	 * Verkleinert ein {@link BufferedImage}.
	 * 
	 * @param source
	 * @param targetWidth
	 * @param targetHeight
	 * @return
	 */
	public static BufferedImage resizeImage(BufferedImage source, int targetWidth, int targetHeight) {
		BufferedImage target = source;
		
		int sourceWidth = source.getWidth();
		int sourceHeight = source.getHeight();
		if(sourceWidth > targetWidth || sourceHeight > targetHeight) {
			double ratio = (double)sourceWidth/(double)sourceHeight;
			int newWidth = 0;
			int newHeight = 0;
			
			if(ratio > 1) {
				newWidth = targetWidth;
				newHeight = (int)((double)newWidth/ratio);
			} else {
				newHeight = targetHeight;
				newWidth = (int)((double)newHeight*ratio);
			}
			
			target = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = target.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
	                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	        g2.drawImage(source, 0, 0, newWidth, newHeight, null);
	        g2.dispose();
	        
	        target = GUIHelper.toCompatibleImage(target);
		}
		
		return target;
	}
	
    /**
     * Erstellt aus einem {@link BufferedImage} ein Systemkompatibles
     * {@link BufferedImage}. Beim Zeichnen solcher Bilder resultiert eine
     * bessere Performance.
     * 
     * @param image
     * @return
     */
    public static BufferedImage toCompatibleImage(BufferedImage image) {
        GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice d = e.getDefaultScreenDevice();
        GraphicsConfiguration c = d.getDefaultConfiguration();
        
        BufferedImage compatibleImage = c.createCompatibleImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics g = compatibleImage.getGraphics();
        
        g.drawImage(image, 0, 0, null);
        g.dispose();
        
        return compatibleImage;
    }
	
	/**
	 * Zentriert eine {@link Component} auf dem Bildschirm.
	 * 
	 * @param toCenter Ÿblicherweise ein {@link JFrame} o.€.
	 */
	public static void centerOnScreen(Component toCenter) {
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		toCenter.setLocation(
				(int)((screen.getWidth()-toCenter.getWidth())/2),
				(int)((screen.getHeight()-toCenter.getHeight())/2)
			);
	}
	
	/**
	 * Zentriert eine {@link Component} auf einer Anderen.
	 * 
	 * @param toCenter Ÿblicherweise ein {@link JFrame}, {@link JDialog} o.€.
	 */
	public static void centerOnComponent(Component toCenter, Component component) {
		Point componentLocation = component.getLocation();
		toCenter.setLocation(
				(int)((component.getWidth()-toCenter.getWidth())/2) + componentLocation.x,
				(int)((component.getHeight()-toCenter.getHeight())/2) + componentLocation.y
				);
	}
	
}
