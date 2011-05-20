package ch.hsr.audiotagger.io.artwork.amazon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ch.hsr.audiotagger.io.artwork.AbstractArtworkProvider;


/**
 * {@link AbstractArtworkProvider}-Implementierung zur Suche von Coverbildern auf
 * Amazon.
 * 
 * @author Manuel Alabor
 * @see AmazonProductAdvAPI
 */
public class AmazonArtworkProvider extends AbstractArtworkProvider {
	
	private final static String ACCESS_KEY = "AKIAIODA4CRMD4XDRLIQ";
	private final static String SECRET_KEY = "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";
	private final static String URL_AMAZON_US = "ecs.amazonaws.com";
	private final static String URI = "/onca/xml";
	
	// Implementierung ---------------------------------------------------------
	@Override
	public String getName() {
		return "Amazon";
	}
	
	@Override
	public ArrayList<String> searchArtwork(String artist) throws Exception {
		return searchArtwork(artist, null);
	}
	
	@Override
	public ArrayList<String> searchArtwork(String artist, String title) throws Exception {
		ArrayList<String> urls = new ArrayList<String>();
		
		/* AmazonProductAdvAPI vorbereiten: */
		AmazonProductAdvAPI api = new AmazonProductAdvAPI(URL_AMAZON_US, URI, ACCESS_KEY, SECRET_KEY);
		Map<String, String> wsParams = new HashMap<String, String>();
		
		if(artist != null) wsParams.put(AmazonProductAdvAPI.PARAM_ARTIST, artist);
		if(title != null) wsParams.put(AmazonProductAdvAPI.PARAM_TITLE, title);
		wsParams.put(AmazonProductAdvAPI.PARAM_OPERATION, "ItemSearch");
		wsParams.put(AmazonProductAdvAPI.PARAM_RESPONSEGROUP, "Images,ItemAttributes");
		wsParams.put(AmazonProductAdvAPI.PARAM_SEARCHINDEX,"Music");
		wsParams.put(AmazonProductAdvAPI.PARAM_SERVICE, "AWSECommerceService");
		
		/* WS-URL generieren & per SAX parsen: */
		String url = api.generateURL(wsParams);
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		parser.parse(url, new AmazonSAXHandler(urls));
		
		/* URLs ausgeben: */
		return urls;
	}
	
	
	// Hilfsklassen ------------------------------------------------------------
	/**
	 * Implementierung eines {@link DefaultHandler}, welche das Parsen des
	 * AmazonWS-Outputs (XML) ermöglicht.<br/>
	 * Alle URL's von "LargeImages" werden gefiltert und in einer {@link ArrayList}
	 * zurückgegeben.
	 */
	private class AmazonSAXHandler extends DefaultHandler {

		private ArrayList<String> resultArrayList;
		private boolean insideItem = false;
		private boolean insideLargeImage = false;
		private boolean insideURL = false;
		private StringBuilder buffer;
		
		public AmazonSAXHandler(ArrayList<String> resultArrayList) {
			this.resultArrayList = resultArrayList;
		}
		
	    public void startDocument() throws SAXException {
	        //System.out.println("Start!");
	    }

	    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
	    	if(qName.equals("Item")) {
	    		insideItem = true;
	    	}
	    	
	    	if(insideItem && qName.equals("LargeImage")) {
	    		insideLargeImage = true;
	    	}
	    	
	    	if(insideItem && insideLargeImage && qName.equals("URL")) {
	    		insideURL = true;
	    		buffer = new StringBuilder();
	    	}
	    	
	    }
	    
	    public void endElement(String namespaceURI, String localName, String qName) {
	    	if(!insideLargeImage && !insideURL && qName.equals("Item")) insideItem = false;
	    	if(!insideURL && qName.equals("LargeImage")) insideLargeImage = false;
	    	if(insideLargeImage && qName.equals("URL")) {
	    		insideURL = false;
	    		resultArrayList.add(buffer.toString());
	    	}
	    }

	    public void characters(char ch[], int start, int length) {
	    	if(insideURL) {
	    		String s = new String(ch, start, length).trim();
	    		buffer.append(s);
	    	}
	    }
	
	}
	
	
	
//	public static void main(String[] args) {
//		
//		JFrame frame = new JFrame("Amazon Cover Searcher");
//		frame.setSize(500, 500);
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		
//		final DefaultListModel coverModel = new DefaultListModel();
//		final JTextField txtKeywords = new JTextField();
//		final JButton btnSearch = new JButton("Search");
//		btnSearch.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				coverModel.clear();
//				
//				AmazonArtworkEngine engine = new AmazonArtworkEngine();
//				try {
//					ArrayList<String> urls = engine.search(txtKeywords.getText(), null);
//					
//					for (String url : urls) {
//						System.out.println(url);
//						coverModel.addElement(url);
//					}	
//				} catch (Exception e1) {
//					e1.printStackTrace();
//				}
//				
//			}
//		});
//		
//		JPanel search = new JPanel(new BorderLayout());
//		search.add(txtKeywords, BorderLayout.CENTER);
//		search.add(btnSearch, BorderLayout.EAST);
//		
//		
//		JList lstCovers = new JList(coverModel);
//		lstCovers.setCellRenderer(new DefaultListCellRenderer() {
//			@Override
//			public Component getListCellRendererComponent(JList list,
//					Object value, int index, boolean isSelected,
//					boolean cellHasFocus) {
//				super.getListCellRendererComponent(list, value, index, isSelected,
//						cellHasFocus);
//				
//				setText("");
//				
//				try {
//					URL url = new URL(value.toString());
//					setIcon(new ImageIcon(url));
//				} catch (MalformedURLException e) {
//					e.printStackTrace();
//				}
//				
//				return this;
//			}
//		});
//		
//		JPanel gui = new JPanel(new BorderLayout());
//		gui.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//		gui.add(search, BorderLayout.NORTH);
//		gui.add(new JScrollPane(lstCovers), BorderLayout.CENTER);
//		
//		frame.setContentPane(gui);
//		frame.setVisible(true);
//		
//	}
	
}
