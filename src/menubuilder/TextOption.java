package menubuilder;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;

import menubuilder.Text.Alignment;

import org.lwjgl.input.Keyboard;

public class TextOption implements Option {
	
	String type = "goto",action, text;
	int[] location;
	Alignment alignment = Alignment.ALIGN_LEFT;
	Color fontcolor = Color.white;
	float scale = 1.0f;
	Font font = Locale.getNameFont();
	
	public TextOption(HashMap<String,String> attributes) {
			this.text = attributes.get("text");
			if(attributes.containsKey("action"))
				this.action = attributes.get("action").toLowerCase();
			String[] axes = attributes.get("location").split("\\s*[,x\\s]\\s*");
			location = new int[2];
			location[0] = Integer.parseInt(axes[0]);
			location[1] = Integer.parseInt(axes[1]);
				
			if(attributes.containsKey("text_align")) {
				switch(attributes.get("text_align").charAt(0)) {
					case 'C':
					case 'c': this.alignment = Alignment.ALIGN_CENTER; break;
					case 'R':
					case 'r': this.alignment = Alignment.ALIGN_RIGHT; break;
					case 'L':
					case 'l':
					default: this.alignment = Alignment.ALIGN_LEFT; break;
				}
			}
			if(attributes.containsKey("text_color")){
				String[] colors = attributes.get("text_color").split("\\s*,\\s*");
				this.fontcolor = new Color(Integer.parseInt(colors[0]),Integer.parseInt(colors[1]),Integer.parseInt(colors[2]));
			}
			if(attributes.containsKey("font")) {
				this.font = new Font(attributes.get("font"),Font.PLAIN,Integer.parseInt(attributes.get("font_size")));
			}
			if(attributes.containsKey("font_scale")) {
				this.scale = Float.parseFloat(attributes.get("font_scale"));
			}
			/*if(attributes.containsKey("type")) {
				this.type = attributes.get("type");
			}*/
	}
	
	TextOption(String text, String action, String location, String alignment, String fontcolor) {
		this.text = text;
		this.action = action.toLowerCase();
		String[] axes = location.split("\\s*[,x\\s]\\s*");
		this.location = new int[2];
		this.location[0] = Integer.parseInt(axes[0]);
		this.location[1] = Integer.parseInt(axes[1]);
		switch(alignment.charAt(0)) {
			case 'C':
			case 'c': this.alignment = Alignment.ALIGN_CENTER; break;
			case 'R':
			case 'r': this.alignment = Alignment.ALIGN_RIGHT; break;
			case 'L':
			case 'l':
			default: this.alignment = Alignment.ALIGN_LEFT; break;
		}
		String[] colors = fontcolor.split("\\s*,\\s*");
		this.fontcolor = new Color(Integer.parseInt(colors[0]),Integer.parseInt(colors[1]),Integer.parseInt(colors[2]));
	}
	
	public String getText() {
		return text;
	}
	
	@Override
	public String getType() {
		return type;
	}
	
	@Override
	public String getValue() {
		return action;
	}
	
	@Override
	public int[] getLocation() {
		return location;
	}
	
	@Override
	public void drawSelf(double alpha) {
		Text.renderText(text, location[0], location[1], font, new Color(fontcolor.getRed(),fontcolor.getGreen(),fontcolor.getBlue(),(int) (255*alpha)), alignment,scale);
	}

	@Override
	public boolean inputLocked() {
		return false;
	}

	@Override
	public String input(int key) {
		if(key == Keyboard.KEY_RETURN)
			return type;
		return "";
	}
}