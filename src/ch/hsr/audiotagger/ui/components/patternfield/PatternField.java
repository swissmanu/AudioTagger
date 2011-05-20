package ch.hsr.audiotagger.ui.components.patternfield;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.UIManager;

/**
 * 
 * @author Manuel Alabor
 * @see PatternElementModel
 * @see #createPatternElement(PatternElementModel, boolean)
 */
public class PatternField extends JComponent {
	
	private boolean removeWithDoubleClick = true;
	private boolean connectPatternElements = true;
	private int preferredWidthIfEmpty = 100;
	
	private ArrayList<PatternElementModel> patternElements = new ArrayList<PatternElementModel>();
	private ArrayList<PatternFieldElementListener> patternFieldElementListeners = new ArrayList<PatternFieldElementListener>();

	private int patternElementHeight = -1;
	private Rectangle[] patternElementRegions = new Rectangle[0];
	private int totalPatternElementRegionsWidth = -1;
	private Hashtable<Object, Dimension> prototypeCache = new Hashtable<Object, Dimension>();
	private int draggedIndex = -1;
	
	private final static int ADDITONAL_V_SPACE = 1;
	private final static int ADDITONAL_H_SPACE = 1;
	private final static int TEXT_V_PADDING = 4;
	private final static Color COLOR_FONT = new Color(24,49,82);
	private final static Color COLOR_BORDER = new Color(55,93,129);
	private final static Color COLOR_FILL_TOP = new Color(225,230,250);
	private final static Color COLOR_FILL_BOTTOM = new Color(196,215,237);
	private final static Color COLOR_FONT_DISABLED = new Color(82,82,82);
	private final static Color COLOR_BORDER_DISABLED = new Color(130,130,130);
	private final static Color COLOR_FILL_TOP_DISABLED = new Color(237,237,237);
	private final static Color COLOR_FILL_BOTTOM_DISABLED = new Color(227,227,227);
	private static final long serialVersionUID = 4724403185892706438L;
	
	/**
	 * Erstellt eine neue Instanz eines {@link PatternField}'s.
	 */
	public PatternField() {
		this(true, true);
	}
	
	/**
	 * Erstellt eine neue Instanz eines {@link PatternField}'s.<br/>
	 * 
	 * @param connectPatternElements Einzelne PatternElements miteinander verbunden zeichnen
	 * @param removeWithDoubleClick Jedes {@link PatternElementModel} kann per Doppelklick aus dem {@link PatternField} entfernt werden
	 */
	public PatternField(boolean connectPatternElements, boolean removeWithDoubleClick) {
		this.connectPatternElements = connectPatternElements;
		this.removeWithDoubleClick = removeWithDoubleClick;
		
		setBorder(UIManager.getBorder("TextField.border"));
		setBackground(UIManager.getColor("TextField.background"));
		setFont(UIManager.getFont("Label.font"));
		
		initMouseInteraction();
	}
	
	private void initMouseInteraction() {
		/* Doppelklick: */
		// Wenn aktiviert, kann per Doppelklick ein PatternElement aus dem
		// PatternField entfernt werden.
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if(e.getClickCount() == 2 && PatternField.this.removeWithDoubleClick) {
					int index = getPatternElementIndexFromLocation(e.getX(), e.getY());
					
					if(index > -1) {
						removePatternElement(index);
					}
				}
			}
		});
		
		/* Drag'n'Drop: */
		// DragTarget:
		new DropTarget(this, new PatternFieldDropTargetListener());
		
		// DragSource:
		DragSource dragSource = new DragSource();
		@SuppressWarnings("unused")
		DragGestureRecognizer gestureRecognizer
			= dragSource.createDefaultDragGestureRecognizer(
					this,
					DnDConstants.ACTION_MOVE,
					new MovePatternElementDragGestureListener(dragSource)
		);
	}
	
	
	// Berechnungen ------------------------------------------------------------
	/**
	 * Berechnet die Positionen und Abmessungen der einzelnen {@link PatternElement}'s,
	 * welche per {@link #paintComponent(Graphics)} gerendert werden.
	 * 
	 * @see #getPatternElementPrototype(PatternElement)
	 */
	private void calcPatternElementRegions() {
		Rectangle[] regions = new Rectangle[patternElements.size()];
		Insets insets = getInsets();
		int totalWidth = 0;
		int x = ADDITONAL_H_SPACE + insets.left;
		int y = ADDITONAL_V_SPACE + insets.top;
		
		for(int i = 0, l = patternElements.size(); i < l; i++) {
			PatternElementModel patternElement = patternElements.get(i);
			Object data = patternElement.getData();

			if(data != null) {
				Dimension prototype = prototypeCache.get(data);
				if(prototype == null) {
					prototype = getPatternElementPrototype(patternElement);
					prototypeCache.put(data, prototype);
				}
				
				regions[i] = new Rectangle(x, y, prototype.width, prototype.height);
				x += prototype.width;
				totalWidth += prototype.width;
			}
		}
		
		if(totalWidth == 0) totalWidth = preferredWidthIfEmpty;
		
		this.totalPatternElementRegionsWidth = totalWidth;
		this.patternElementRegions = regions;
	}
	
	/**
	 * Berechnet die Dimensionen für ein gerendertes {@link PatternElement}.
	 * 
	 * @param patternElement
	 * @return
	 * @see #calcPatternElementRegions()
	 */
	private Dimension getPatternElementPrototype(PatternElementModel patternElement) {
		FontRenderContext context = new FontRenderContext(null, true, false);
		TextLayout textLayout = new TextLayout(patternElement.getText(), getFont(), context);
		int  patternElementHeight = getPatternElementHeight();
		
		int width = (int)textLayout.getBounds().getWidth() + patternElementHeight;
		int height = patternElementHeight;
		
		return new Dimension(width, height);
	}
	
	private int getPatternElementHeight() {
		if(this.patternElementHeight == -1) getPreferredSize();
		return this.patternElementHeight;
	}
	
	/**
	 * Ermittelt aufgrund einer Koordinate (normalerweise die eines Mausklicks)
	 * den Index des {@link PatternElement}'s, welches sich an dieser Position
	 * befindet.<br/>
	 * Konnte kein {@link PatternElement} ermittelt werden, wird <code>-1</code>
	 * zurückgegeben.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private int getPatternElementIndexFromLocation(int x, int y) {
		int index = -1;
		
		for (int i = 0, l = patternElementRegions.length; i < l; i++) {
			Rectangle region = patternElementRegions[i];
			if(region.contains(x, y)) {
				index = i;
				break;
			}
		}
		
		return index;
	}
	
	/**
	 * Berechnet aufgrund einer Koordinate (Mausposition) den Index, an welchem
	 * ein PatternElement während eines Drag'n'Drop-Vorgangs abgelegt werden
	 * soll.<br/>
	 * Hierbei wird das PatternElement unter dem Mauszeiger geholt. Befindet
	 * sich der Mauszeiger in der linken Hälfte, wird der Index des ermittelten
	 * PatternElements zurückgegeben. Befindet sich der Cursor in der rechten
	 * Hälfte, wird der Index des nächsten PatternElements zurückgegeben.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private int getDropIndexFromLocation(int x, int y) {
		int index = -1;
		
		index = getPatternElementIndexFromLocation(x, y);
		if(index > -1) {
			Rectangle region = patternElementRegions[index];
			if(x > region.x + region.width/2) index++;
		}
		
		return index;
	}
	
	
	// Zugriff -----------------------------------------------------------------
	/**
	 * Definiert, ob die einzelnen gerenderten PatternElements im
	 * {@link PatternField} miteinander verbunden oder einzeln gezeichnet
	 * werden sollen.
	 * 
	 * @param connectPatternElements
	 */
	public void setConnectPatternElements(boolean connectPatternElements) {
		boolean old = this.connectPatternElements;
		this.connectPatternElements = connectPatternElements;
		
		if(old != connectPatternElements) repaint();
	}
	
	/**
	 * Ermittelt ob die einzelenen PatternElements miteinander verbunden oder
	 * alleinstehend gerendert werden.
	 * 
	 * @return
	 */
	public boolean isConnectPatternElements() {
		return connectPatternElements;
	}
	
	/**
	 * Definiert, ob ein PatternElement in diesem {@link PatternField} per
	 * Doppelklick entfernt werden kann.
	 * 
	 * @param removeWithDoubleClick
	 */
	public void setRemoveWithDoubleClick(boolean removeWithDoubleClick) {
		this.removeWithDoubleClick = removeWithDoubleClick;
	}
	
	/**
	 * Fügt diesem {@link PatternField} ein {@link PatternElementModel}
	 * hinzu.
	 * 
	 * @param patternElement
	 */
	public void addPatternElement(PatternElementModel patternElement) {
		try {
			PatternElementModel clonedModel = (PatternElementModel)patternElement.clone();
			
			patternElements.add(clonedModel);
			calcPatternElementRegions();
			
			firePatternElementAdded(patternElement);
			repaint();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Entfernt das {@link PatternElementModel} mit dem Index <code>index</code>
	 * von diesem {@link PatternField}.
	 * 
	 * @param index
	 */
	public void removePatternElement(int index) {
		PatternElementModel patternElement = patternElements.get(index);
		patternElements.remove(index);
		calcPatternElementRegions();
		
		firePatternElementRemoved(patternElement);
		repaint();
	}
	
	/**
	 * Entfernt das {@link PatternElementModel} mit dem Index <code>index</code>
	 * von diesem {@link PatternField}.
	 * 
	 * @param index
	 */
	public void removeAllPatternElements() {
		for(int i = patternElements.size()-1, l = 0; i >= l; i--) {
			firePatternElementRemoved(patternElements.get(i));
			patternElements.remove(i);
		}
		
		calcPatternElementRegions();
		repaint();
	}
	
	/**
	 * Fügt einen neuen {@link PatternFieldElementListener} hinzu.<br/>
	 * Der hinzugefügte Listener wird per sofort über hinzugefügte und entfernte
	 * {@link PatternElementModel}'s informiert.
	 * 
	 * @param l
	 * @see #removePatternFieldElementListener(PatternFieldElementListener)
	 */
	public void addPatternFieldElementListener(PatternFieldElementListener l) {
		patternFieldElementListeners.add(l);
	}

	/**
	 * Enfernt einen {@link PatternFieldElementListener}.<br/>
	 * 
	 * @param l
	 * @see #addPatternFieldElementListener(PatternFieldElementListener)
	 */
	public void removePatternFieldElementListener(PatternFieldElementListener l) {
		patternFieldElementListeners.remove(l);
	}
	
	/**
	 * Erstellt aus einem {@link PatternElementModel} eine alleinstehende
	 * Komponente, welche das Hinzufügen des entsprechenden
	 * {@link PatternElementModel}'s zu diesem {@link PatternField} ermöglicht.
	 * 
	 * @param model
	 * @param permitMultipleUse
	 * @see PatternElement
	 * @return
	 */
	public JComponent createPatternElement(PatternElementModel model, boolean permitMultipleUse) {
		PatternElement patternElement = new PatternElement(model, permitMultipleUse);
		patternFieldElementListeners.add(patternElement);
		
		return patternElement;
	}
	
	/**
	 * Liefert die Daten der momentan vorhandenen {@link PatternElementModel}'s
	 * als zusammenhŠngenden {@link String} zurŸck.
	 * 
	 * @return
	 */
	public String getPatternElementsAsString() {
		StringBuilder builder = new StringBuilder();
		
		for (PatternElementModel patternElement : patternElements) {
			Object data = patternElement.getData();
			if(data != null) builder.append(data.toString());
		}
		
		return builder.toString();
	}
	
	/**
	 * Befinden sich keine {@link PatternElement}'s im {@link PatternField}, wird
	 * diese Zahl verwendet, um die Breite fŸr die PreferredSize zu berechnen.
	 * 
	 * @param preferredWidthIfEmpty
	 */
	public void setPreferredWidthIfEmpty(int preferredWidthIfEmpty) {
		this.preferredWidthIfEmpty = preferredWidthIfEmpty;
		
		calcPatternElementRegions();
		repaint();
	}
	
	// †berschreibungen --------------------------------------------------------
	@Override
	public Dimension getPreferredSize() {
		Insets insets = getInsets();
		
		/* Hšhe fŸr PatternElement-Rendering: */
		// Falls die Hšhe fŸr ein gerendertes PatterElement noch nie berechnet
		// wurde, wird dies hier nachgeholt.
		if(patternElementHeight == -1) {
			TextLayout textLayout = new TextLayout("_qÄ^",getFont(), new FontRenderContext(null, true, false));
			patternElementHeight = (int)textLayout.getBounds().getHeight() + TEXT_V_PADDING*2;
		}
		
		/* PreferredSize berechnen & zurŸckgeben: */
		int width =  insets.left + totalPatternElementRegionsWidth + insets.right + 2*ADDITONAL_H_SPACE;
		int height = insets.top + patternElementHeight + insets.bottom + 2*ADDITONAL_V_SPACE;
		
		return new Dimension(width, height);
	}

	
	// Event-Handling ----------------------------------------------------------
	/**
	 * Informiert alle {@link PatternFieldElementListener}, welche bei diesem
	 * {@link PatternField} angemeldet sind darüber, dass ein neues {@link PatternElementModel}
	 * hinzugefügt worden ist.
	 * 
	 * @param patternElement
	 */
	protected void firePatternElementAdded(PatternElementModel patternElement) {
		for (PatternFieldElementListener l : patternFieldElementListeners) {
			l.patternElementAdded(patternElement);
		}
	}
	
	/**
	 * Informiert alle {@link PatternFieldElementListener}, welche bei diesem
	 * {@link PatternField} angemeldet sind darüber, dass ein {@link PatternElementModel}
	 * entfernt worden ist.
	 * 
	 * @param patternElement
	 */	
	protected void firePatternElementRemoved(PatternElementModel patternElement) {
		for (PatternFieldElementListener l : patternFieldElementListeners) {
			l.patternElementRemoved(patternElement);
		}
	}
	
	// Grafikausgabe -----------------------------------------------------------
	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g2.setFont(getFont());
		Insets insets = getInsets();
		int x = insets.left + ADDITONAL_H_SPACE;
		int y = insets.top + ADDITONAL_V_SPACE;
		
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(getBackground());
		g2.fillRect(0, 0, getWidth(), getHeight());
		
		for(int i = 0, l = patternElements.size(); i < l; i++) {
			PatternElementModel patternElement = patternElements.get(i);
			boolean roundedLeft = (i == 0 || !connectPatternElements);
			boolean roundedRight = (i == l-1 || !connectPatternElements);
			boolean dragged = (draggedIndex != -1 && i == draggedIndex);
						
			g2.translate(x,y);
			Dimension drawedDimension = paintPatternElement(g2, patternElement, roundedLeft, roundedRight, true, dragged);
			g2.translate(-x,-y);
			
			x += drawedDimension.getWidth();
		}
		
//		g2.setColor(Color.red);
//		for(Rectangle r: patternElementRegions) {
//			g2.draw(r);
//		}
		
		super.paintComponent(g2);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}
	
	/**
	 * Zeichnet ein {@link PatternElementModel} in den Grafikkontext <code>g2</code>.
	 * 
	 * @param g2 Grafikkontext
	 * @param patternElement Zu zeichnendes {@link PatternElementModel}
	 * @param roundedOnLeft Soll das Element links abgerundet werden?
	 * @param roundedOnRight Soll das Element rechts abgerundet werden?
	 * @param enabled Ist das PatternElement momentan aktiv?
	 * @return Grösse des gerenderten {@link PatternElementModel}'s
	 * @see PatternField#paintComponent(Graphics)
	 * @see PatternElement
	 */
	private Dimension paintPatternElement(Graphics2D g2, PatternElementModel patternElement, boolean roundedOnLeft, boolean roundedOnRight, boolean enabled, boolean dragged) {
		/* Vorbereiten: */
		Shape initialClip = g2.getClip();
		Composite initialComposite = g2.getComposite();
		String text = patternElement.getText();
		int patternElementHeight = getPatternElementHeight();
		
		TextLayout textLayout = new TextLayout(text, g2.getFont(), g2.getFontRenderContext());
		int endWidth = patternElementHeight/2;
		int width = patternElementHeight + (int)textLayout.getBounds().getWidth();
		int height = patternElementHeight;
		
		/* Hintergrund zeichnen: */
		// Farben vorbereiten:
		Paint fillPaint = null;
		if(enabled) fillPaint = new GradientPaint(0, 0, COLOR_FILL_TOP, 0, height, COLOR_FILL_BOTTOM); 
		else fillPaint = new GradientPaint(0, 0, COLOR_FILL_TOP_DISABLED, 0, height, COLOR_FILL_BOTTOM_DISABLED);
		
		Color borderColor = COLOR_BORDER;
		if(!enabled) borderColor = COLOR_BORDER_DISABLED;
		
		Color fontColor = COLOR_FONT;
		if(!enabled) fontColor = COLOR_FONT_DISABLED;
		
		if(dragged) g2.setComposite(AlphaComposite.SrcOver.derive(0.5f));
		
		// Element zeichnen:
		if(!roundedOnLeft && !roundedOnRight) {
			// PatternElement ohne runde Ecken:
			g2.setPaint(fillPaint);
			g2.fillRect(0, 0, width, height);
			
			g2.setColor(borderColor);
			g2.drawLine(0, 0, width-1, 0);
			g2.drawLine(0, height-1, width-1, height-1);
			
			g2.drawLine(0, 1, 0, height-2);
			
		} else if(roundedOnLeft && !roundedOnRight) {
			// PatternElement mit rundem linken Ecken:
			g2.setClip(0, 0, endWidth, height);
			g2.setPaint(fillPaint);
			g2.fillOval(0, 0, endWidth*2, height);
			g2.setColor(borderColor);
			g2.drawOval(0, 0, endWidth*2-1, height-1);
			
			g2.setClip(initialClip);
			g2.setPaint(fillPaint);
			g2.fillRect(endWidth, 0, width-endWidth, height);
			g2.setColor(borderColor);
			g2.drawLine(endWidth, 0, width - 1, 0);
			g2.drawLine(endWidth, height-1, width - 1, height-1);
			
		} else if(!roundedOnLeft && roundedOnRight) {
			// PatternElement mit rundem rechten Ecken:
			g2.setClip(width-endWidth, 0, endWidth, height);
			g2.setPaint(fillPaint);
			g2.fillOval(width-endWidth*2, 0, height, height);
			g2.setColor(borderColor);
			g2.drawOval(width-endWidth*2, 0, height-1, height-1);
			
			g2.setClip(initialClip);
			g2.setPaint(fillPaint);
			g2.fillRect(0, 0, width-endWidth, height);
			g2.setColor(borderColor);
			g2.drawLine(0, 0, width-endWidth - 1, 0);
			g2.drawLine(0, height-1, width-endWidth - 1, height-1);
			
			g2.drawLine(0, 1, 0, height-2);
			
		} else if(roundedOnLeft && roundedOnRight) {
			// PatternElement mit rundem linken und rechten Ecken:
			g2.setClip(0, 0, endWidth, height);
			g2.setPaint(fillPaint);
			g2.fillOval(0, 0, endWidth*2, height);
			g2.setColor(borderColor);
			g2.drawOval(0, 0, endWidth*2-1, height-1);
			
			g2.setClip(width-endWidth, 0, endWidth, height);
			g2.setPaint(fillPaint);
			g2.fillOval(width-endWidth*2, 0, height, height);
			g2.setColor(borderColor);
			g2.drawOval(width-endWidth*2, 0, height-1, height-1);
			
			g2.setClip(initialClip);
			g2.setPaint(fillPaint);
			g2.fillRect(endWidth, 0, width-endWidth*2, height);
			g2.setColor(borderColor);
			g2.drawLine(endWidth, 0, width - endWidth - 1, 0);
			g2.drawLine(endWidth, height-1, width - endWidth - 1, height-1);
		}
		
		g2.setClip(initialClip);
		g2.setComposite(initialComposite);
		
		/* Text zeichnen: */
		g2.setColor(fontColor);
		textLayout.draw(g2, (width-(int)textLayout.getBounds().getWidth())/2, (int)(TEXT_V_PADDING*3.5));
		
		/* Grösse des gezeichnenten PatternElement zurückgeben: */
		if(roundedOnLeft && roundedOnRight) width += ADDITONAL_H_SPACE;
		return new Dimension(width, height);
	}
	
	
	// Drag'N'Drop-Implementierungen -------------------------------------------
	/**
	 * Reagiert auf die Events eines {@link DropTarget}'s fŸr ein
	 * {@link PatternField}.
	 */
	private class PatternFieldDropTargetListener extends DropTargetAdapter {

		/**
		 * Setzt die Position fŸr die Markierung, welche die mšgliche Position
		 * fŸr das abzulegende {@link PatternElement} anzeigt.
		 */
		@Override
		public void dragOver(DropTargetDragEvent dtde) {
			
			try {
				Object data = dtde.getTransferable().getTransferData(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType));
				
				if(data instanceof PatternElementModel) {
					PatternElementModel patternElement = (PatternElementModel)data;
					Point pos = dtde.getLocation();
					
					int newIndex = getDropIndexFromLocation(pos.x, pos.y);
					int currentIndex = patternElements.indexOf(patternElement);
					
					// TODO if's vereinfachen
					if((newIndex != -1 && newIndex != currentIndex) || (newIndex == -1 && currentIndex == -1)) {
						if(newIndex == -1) {
							// Am Ende anfŸgen:
							newIndex = patternElements.size();
						} else {
							// Index korrigieren:
							if(newIndex > currentIndex) newIndex--;	
						}
						
						if(currentIndex != -1) patternElements.remove(currentIndex);
						patternElements.add(newIndex, patternElement);
						draggedIndex = newIndex;

						firePatternElementAdded(patternElement);
						calcPatternElementRegions();
						repaint();
					}
					
				}
			
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		/**
		 * 
		 */
		@Override
		public void drop(DropTargetDropEvent dtde) {
			
			try {
				dtde.acceptDrop(dtde.getDropAction());
				Object value = dtde.getTransferable().getTransferData(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType));
				
				if(value instanceof PatternElementModel) {
					draggedIndex = -1;
					calcPatternElementRegions();
					repaint();
					
					dtde.dropComplete(true);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void dragExit(DropTargetEvent dte) {
			if(draggedIndex != -1) {
				PatternElementModel patternElement = patternElements.get(draggedIndex);
				patternElements.remove(draggedIndex);
				firePatternElementRemoved(patternElement);
			}
			draggedIndex = -1;
			repaint();
		}
		
	}
	
	/**
	 * 
	 * @author Manuel Alabor
	 */
	private class MovePatternElementDragGestureListener implements DragGestureListener {
		
		private DragSource dragSource;
		
		public MovePatternElementDragGestureListener(DragSource dragSource) {
			this.dragSource = dragSource;
		}
		
		@Override
		public void dragGestureRecognized(DragGestureEvent dge) {
			Point pos = dge.getDragOrigin();
			int index = getPatternElementIndexFromLocation(pos.x, pos.y);
			
			if(index > -1) {
				PatternElementModel patternElement = patternElements.get(index);
				BufferedImage dummyDragImage = new BufferedImage(1,1, BufferedImage.TYPE_INT_ARGB);
				draggedIndex = index;
				repaint();
				
				dragSource.startDrag(
					dge,
					DragSource.DefaultCopyDrop,
					dummyDragImage,
					new Point(0,0),
					new PatternElementTransferable(patternElement),
					new DragSourceAdapter(){
						@Override
						public void dragDropEnd(DragSourceDropEvent dsde) {
							if(!dsde.getDropSuccess()) {
								draggedIndex = -1;
								repaint();
							}
						}
					}
				);
				
			}
			
		}
	}
	
	/**
	 * 
	 * @author Manuel Alabor
	 */
	private class AddPatternElementDragGestureListener implements DragGestureListener {
		
		private DragSource dragSource;
		private PatternElementModel patternElementModel;
		private PatternElement patternElement;
		
		public AddPatternElementDragGestureListener(DragSource dragSource, PatternElementModel patternElementModel, PatternElement patternElement) {
			this.dragSource = dragSource;
			this.patternElementModel = patternElementModel;
			this.patternElement = patternElement;
		}
		
		@Override
		public void dragGestureRecognized(DragGestureEvent dge) {
			if(patternElement.isEnabled()) {
				PatternElementModel clone = new PatternElementModel(patternElementModel.getText(), patternElementModel.getData(), true);
				
				BufferedImage dragImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2 = (Graphics2D)dragImage.getGraphics();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setComposite(AlphaComposite.SrcOver.derive(0.5f));
				paintPatternElement(g2, patternElementModel, true, true, true, false);
				g2.dispose();
				
				Point offset = dge.getDragOrigin();
				offset.x = -offset.x;
				offset.y = -offset.y;
				
				dragSource.startDrag(
					dge,
					DragSource.DefaultCopyDrop,
					dragImage,
					offset,
					new PatternElementTransferable(clone),
					new DragSourceAdapter() {
						@Override
						public void dragDropEnd(DragSourceDropEvent dsde) {
							if(!dsde.getDropSuccess()) {
								draggedIndex = -1;
								repaint();
							}
						}
					}
					
				);				
			}
			
		}
	}
	
	/**
	 * Transferable-Implementierung fŸr ein {@link PatternElement}
	 * 
	 * @author Manuel Alabor
	 */
	private class PatternElementTransferable implements Transferable {

		private PatternElementModel patternElement;
		
		public PatternElementTransferable(PatternElementModel patternElement) {
			this.patternElement = patternElement;
		}
		
		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			return patternElement;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() { return null; }
		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) { return false; }
	}
	
	
	// Hilfskomponenten --------------------------------------------------------
	/**
	 * Dient zur einzelnen reprŠsentation eines {@link PatternElementModel}'s.<br/>
	 * Damit die Rendering-Methoden nicht nocheinmal implementiert werden mŸssen,
	 * wird {@link PatternElement} als private Klasse von {@link PatternField}
	 * implementiert, welche die entsprechenden Routinen bereits enthŠlt.<br/>
	 * <br/>
	 * Um ein {@link PatternElement} fŸr das GUI zu erstellen, muss die Methode
	 * {@link PatternField#createPatternElement(PatternElementModel)}
	 * verwendet werden.
	 * 
	 * @author Manuel Alabor
	 * @see PatternField#createPatternElement(PatternElementModel)
	 * @see PatternField#paintPatternElement(Graphics2D, PatternElementModel, boolean, boolean, boolean)
	 */
	private class PatternElement extends JComponent implements PatternFieldElementListener {
		
		private PatternElementModel model;
		private boolean permitMultipleUse = false;
		private static final long serialVersionUID = 4609512988282741161L;

		public PatternElement(PatternElementModel model, boolean permitMultipleUse) {
			this.model = model;
			this.permitMultipleUse = permitMultipleUse;
			setFont(PatternField.this.getFont());
			
			initMouseInteraction();
		}
		
		private void initMouseInteraction() {
			/* Doppelklick: */
			// Ein Doppelklick soll das PatternElementModel dieses PatternElements
			// direkt am dazugehörigen PatternElementField anhängen. 
			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if(isEnabled() && e.getClickCount() == 2) {
						PatternField.this.addPatternElement(PatternElement.this.model);
					}
				}
			});
			
			/* Drag'n'Drop: */
			// Das PatternElement soll per Drag'N'Drop dem dazugehörigen PatternField
			// hinzugefügt werden können.
			DragSource dragSource = new DragSource();
			@SuppressWarnings("unused")
			DragGestureRecognizer gestureRecognizer
				= dragSource.createDefaultDragGestureRecognizer(
						this,
						DnDConstants.ACTION_MOVE,
						new AddPatternElementDragGestureListener(dragSource, model, this)
						);
		}
		
		
		@Override
		public Dimension getPreferredSize() {
			Dimension prototype = getPatternElementPrototype(model);
			return prototype;
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D)g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			paintPatternElement(g2, model, true, true, isEnabled(), false);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		}
		
		
		// PatternFieldElementListener-Implementierung -------------------------
		@Override
		public void patternElementAdded(PatternElementModel patternElementModel) {
			doSwitchState(false, patternElementModel.getData());
		}
		
		@Override
		public void patternElementRemoved(PatternElementModel patternElementModel) {
			doSwitchState(true, patternElementModel.getData());
		}
		
		private void doSwitchState(boolean enabled, Object otherData) {			
			if(permitMultipleUse && otherData.equals(model.getData())) {
				setEnabled(enabled);
				repaint();
			}
		}
		
	}
	
	
	// Testingcode -------------------------------------------------------------
//	public static void main(String[] args) {
//        try {
//            UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
//            UIManager.put("jgoodies.popupDropShadowEnabled", Boolean.TRUE);
//        } catch (UnsupportedLookAndFeelException e) {
//            e.printStackTrace();
//        }
//		
//		final JFrame frame = new JFrame("Test");
//        
//        frame.setLocation(100, 100);
//        frame.setSize(500,300);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        
//        JPanel contentPane = new JPanel(new BorderLayout());
//        contentPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
//        final PatternField patternField = new PatternField();
//        
//        PatternElementModel e2 = new PatternElementModel(" - ", " - ");
//        PatternElementModel e1 = new PatternElementModel("Tracknumber", "%track%");
//        PatternElementModel e3 = new PatternElementModel("Artist", "%artist%");
//        PatternElementModel e5 = new PatternElementModel("Title", "%title%");
//        
//        JPanel elements = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        elements.add(patternField.createPatternElement(e1, true));
//        elements.add(patternField.createPatternElement(e2, false));
//        elements.add(patternField.createPatternElement(e3, true));
//        elements.add(patternField.createPatternElement(e5, true));
//        
//        patternField.addPatternElement(e1);
//        patternField.addPatternElement(e2);
//        patternField.addPatternElement(e3);
//        patternField.addPatternElement(e2);
//        patternField.addPatternElement(e5);
//        
//        contentPane.add(elements, BorderLayout.CENTER);
//        
//        
//        
//        JPanel buttons = new JPanel(new FlowLayout());
//        JButton btnAdd = new JButton("Add");
//        buttons.add(btnAdd);
//        btnAdd.addActionListener(new ActionListener() {
//        	@Override
//        	public void actionPerformed(ActionEvent e) {
//				Object value = JOptionPane.showInputDialog(
//						frame,
//						"Please enter the Pattern:");
//				if(value != null && value.toString().length() > 0)
//					patternField.addPatternElement(new PatternElementModel(value.toString(), value));
//        	}
//        });
//        JButton btnAddBlind = new JButton("Add \"Test\"");
//        buttons.add(btnAddBlind);
//        btnAddBlind.addActionListener(new ActionListener() {
//        	@Override
//        	public void actionPerformed(ActionEvent e) {
//				patternField.addPatternElement(new PatternElementModel("Test", "Test"));
//        	}
//        });
//        JButton btnRemoveAll = new JButton("Remove all");
//        buttons.add(btnRemoveAll);
//        btnRemoveAll.addActionListener(new ActionListener() {
//        	@Override
//        	public void actionPerformed(ActionEvent e) {
//				patternField.removeAllPatternElements();
//        	}
//        });
//
//        buttons.add(Box.createHorizontalStrut(5));
//        
//        JButton btnConnected = new JButton("Switch connected");
//        buttons.add(btnConnected);
//        btnConnected.addActionListener(new ActionListener() {
//        	@Override
//        	public void actionPerformed(ActionEvent e) {
//				patternField.setConnectPatternElements(!patternField.isConnectPatternElements());
//        	}
//        });
//        
//        buttons.add(Box.createHorizontalStrut(5));
//        
//        JButton btnGetPattern = new JButton("Get pattern");
//        buttons.add(btnGetPattern);
//        btnGetPattern.addActionListener(new ActionListener() {
//        	@Override
//        	public void actionPerformed(ActionEvent e) {
//				JOptionPane.showMessageDialog(
//						frame,
//						patternField.getPatternElementsAsString(),
//						"Pattern",
//						JOptionPane.INFORMATION_MESSAGE);
//        	}
//        });
//        
//        
//        contentPane.add(patternField, BorderLayout.NORTH);
//        contentPane.add(buttons, BorderLayout.SOUTH);
//        
//        frame.setContentPane(contentPane);
//		frame.setVisible(true);
//	}
	
}
