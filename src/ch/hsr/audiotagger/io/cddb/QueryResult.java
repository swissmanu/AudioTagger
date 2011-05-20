package ch.hsr.audiotagger.io.cddb;

/**
 * Ermöglicht das zusammengefasste Speichern eines Resultats aus dem
 * Query-Kommando einer CDDB-Datenbank.
 * 
 * @author Manuel Alabor
 * @see CDDB
 */
public class QueryResult {

	private String category;
	private String discId;
	private String discTitle;
	
	public QueryResult(String category, String discId, String discTitle) {
		this.category = category;
		this.discId = discId;
		this.discTitle = discTitle;
	}
	
	public String getCategory() {
		return category;
	}
	
	public String getDiscId() {
		return discId;
	}
	
	public String getDiscTitle() {
		return discTitle;
	}
	
	@Override
	public String toString() {
		return "[QueryResult: " + getCategory() + ", " + getDiscId() + ", " + getDiscTitle() + "]";
	}
	
	// Factory -----------------------------------------------------------------
	/**
	 * Das Resultat des CDDB-Query-Kommandos hat folgendes Format:<br/>
	 * <code>[category] [discid] [disctitle ..... ]</code><br/>
	 * <br/>
	 * Diese Factory-Methode parst dieses Format und gibt anschliessend eine
	 * {@link QueryResult}-Instanz mit eben diesen Werten aus dem Resultat zurück.<br/>
	 * Konnte etwas nicht korrekt geparst werden, wird <code>null</code> zurückgegeben.
	 * 
	 * @param queryResultLine
	 * @return QueryResult
	 */
	public static QueryResult parseQueryResult(String queryResultLine) {
		QueryResult queryResult = null;
		
		if(!queryResultLine.isEmpty()) {
			String[] fields = queryResultLine.split(" ", 3);
			
			if(fields.length == 3) {
				queryResult = new QueryResult(fields[0], fields[1], fields[2]);
			}
		}
		
		return queryResult;
	}
}
