package ch.hsr.audiotagger.io.artwork.amazon;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class AmazonProductAdvAPI {

	private String endPoint;
	private String requestUri;	
	private String accessKey;
	
	private SecretKeySpec secretKeySpec = null;
	private Mac mac = null;
	
	private static final String UTF8_CHARSET = "UTF-8";
	private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
	
	public static final String PARAM_SERVICE = "Service";
	public static final String PARAM_SEARCHINDEX = "SearchIndex";
	public static final String PARAM_RESPONSEGROUP = "ResponseGroup";
	public static final String PARAM_ARTIST = "Artist";
	public static final String PARAM_TITLE = "Title";
	public static final String PARAM_OPERATION = "Operation";

	/**
	 * @param endPoint bspw. ecs.amazonaws.com
	 * @param uri bswp. /onca/xml
	 * @param accessKey AccessKey von Amazon WS Account
	 * @param secretKey SecretKey von Amazon WS Account
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 */
	public AmazonProductAdvAPI(String endPoint, String uri, String accessKey, String secretKey) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
		this.endPoint = endPoint;
		this.requestUri = uri;
		this.accessKey = accessKey;
		
		byte[] secretyKeyBytes = secretKey.getBytes(UTF8_CHARSET);
		secretKeySpec = new SecretKeySpec(secretyKeyBytes, HMAC_SHA256_ALGORITHM);
		mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
		mac.init(secretKeySpec);
	}

	/**
	 * Erstellt mit den übergebenen Parametern eine URL, welche an die Amazon
	 * Webservices geschickt werden kann.
	 * 
	 * @param params
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public String generateURL(Map<String, String> params) throws MalformedURLException, IOException {
		/* Vorbereiten: */
		params.put("AWSAccessKeyId", accessKey);
		params.put("Timestamp", generateTimestamp());

		/* Signierte WS-URL erstellen: */
		// Params sortieren & zusammenhängen:
		SortedMap<String, String> sortedParamMap = new TreeMap<String, String>(params);
		String canonicalParams = generateQueryString(sortedParamMap);
		
		// Wichtig! Amazon erwartet, dass die Signatur inkl. "GET" (HTTP-Methode)
		// berechnet wird. Ansonsten kann Amazon die Signatur nicht prüfen.
		String toSign = "GET\n" + endPoint + "\n" + requestUri + "\n" + canonicalParams;

		// Signatur & URL erstellen:
		String hmac = hmac(toSign);
		String signature = encodeForUrl(hmac);
		String url = "http://" + endPoint + requestUri + "?" + canonicalParams + "&Signature=" + signature;

		return url;
	}

	/**
	 * Verbindet alle Key/Values aus einer {@link SortedMap} zu einem Querystring
	 * der Form <code>[key1]=[value1]&[key2]=[value2]&...</code>.<br/>
	 * Werte und Schlüssel werden so encoded, damit sie in einem URL verwendet
	 * werden können.
	 * 
	 * @param sortedParamMap
	 * @return
	 */
	private String generateQueryString(SortedMap<String, String> sortedParamMap) {
		if (sortedParamMap.isEmpty()) return "";

		StringBuilder builder = new StringBuilder();
		
		Iterator<Map.Entry<String, String>> paramsIterator = sortedParamMap.entrySet().iterator();
		while (paramsIterator.hasNext()) {
			Map.Entry<String, String> entry = paramsIterator.next();
			
			builder.append(encodeForUrl(entry.getKey()));
			builder.append("=");
			builder.append(encodeForUrl(entry.getValue()));
			
			if (paramsIterator.hasNext()) builder.append("&");
		}
		
		return builder.toString();
	}

	/**
	 * Stellt sicher, dass ein {@link String} in einem URL übergeben werden
	 * kann.
	 * 
	 * @param text
	 * @see RFC3986
	 * @return
	 */
	private String encodeForUrl(String text) {
		String out = "";
		
		try {
			out = URLEncoder.encode(text, UTF8_CHARSET);
			out = out.replace("+", "%20");
			out = out.replace("*", "%2A");
			out = out.replace("%7E", "~");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			out = text;
		}
		
		return out;
	}
	
	/**
	 * Erstellt aus einem {@link String} einen HMAC-SHA256-Hashwert.
	 * 
	 * @param stringToSign
	 * @return
	 */
	private String hmac(String stringToSign) {
		String signature = null;
		byte[] data;
		byte[] rawHmac;
		try {
			data = stringToSign.getBytes(UTF8_CHARSET);
			rawHmac = mac.doFinal(data);
			Base64 encoder = new Base64(0);
			signature = new String(encoder.encode(rawHmac));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(UTF8_CHARSET + " is unsupported!", e);
		}
		return signature;
	}

	/**
	 * Erstellt einen Timestamp im Format <code>yyyy-MM-dd'T'HH:mm:ss'Z'</code>
	 * 
	 * @return
	 */
	private String generateTimestamp() {
		String timestamp = "";
		
		// GMT-Zeitzone sicherstellen (nicht lokale Zeit!)
		Calendar cal = Calendar.getInstance();
		DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		dfm.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		// Generieren:
		timestamp = dfm.format(cal.getTime());
		
		return timestamp;
	}

}