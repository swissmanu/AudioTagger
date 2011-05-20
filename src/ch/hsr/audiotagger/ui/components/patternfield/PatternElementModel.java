package ch.hsr.audiotagger.ui.components.patternfield;
/**
 * 
 * 
 * @author Manuel Alabor
 */
public class PatternElementModel implements Cloneable {

	private String text;
	private Object data;
	private boolean isNew = false;

	public PatternElementModel(String text, Object data, boolean isNew) {
		this.text = text;
		this.data = data;
		this.isNew = isNew;
	}
	
	public PatternElementModel(String text, Object data) {
		this(text, data, false);
	}

	public PatternElementModel(String text) {
		this(text, null);
	}

	public String getText() {
		return text;
	}

	public Object getData() {
		return data;
	}
	
	public boolean isNew() {
		return isNew;
	}
	
//	@Override
//	public boolean equals(Object obj) {
//		boolean equals = false;
//		
//		if(obj instanceof PatternElementModel) {
//			PatternElementModel otherModel = (PatternElementModel)obj;
//			
//			Object data1 = otherModel.getData();
//			Object data2 = this.getData();
//			String id1 = data1.hashCode() + data1.toString();
//			String id2 = data2.hashCode() + data2.toString();
//			
//			equals = id1.equals(id2);
//		}
//		
//		return equals;
//	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		PatternElementModel clone = new PatternElementModel(getText(), getData());
		return clone;
	}

}