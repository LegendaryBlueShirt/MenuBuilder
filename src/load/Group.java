package load;

import java.util.HashMap;

public class Group {
	String name;
	HashMap<String, String> attributes;
	
	public Group(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public HashMap<String,String> getAttributes() {
		return attributes;
	}
	
	public void setAttributes(HashMap<String,String> attributes) {
		this.attributes = attributes;
	}
	
	public boolean hasAttribute(String key) {
		return attributes.containsKey(key);
	}
	
	public String getAttribute(String key) {
		return attributes.get(key);
	}
}