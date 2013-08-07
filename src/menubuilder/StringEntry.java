package menubuilder;

import static org.lwjgl.opengl.ARBTextureRectangle.GL_TEXTURE_RECTANGLE_ARB;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glVertex2d;
import static org.lwjgl.opengl.GL11.glVertex2f;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class StringEntry implements Option {
	static ArrayList<char[]> characterBanks;
	HashMap<String, String> attributes;
	String type = "string";
	StringBuffer value = new StringBuffer(7);
	int[] location;
	int selectedBank = 0;
	Color fontcolor = Color.white;
	Color selectcolor = Color.blue;
	float scale = 1.0f;
	Font font = Locale.getNameFont();
	boolean inputLocked = false, hidden = false;
	int select_x = 0, select_y = 0;
	Texture background;
	
	public StringEntry(HashMap<String, String> attributes) {
		this.attributes = attributes;
		generateBankLatin();
		generateBankHiragana();
		generateBankKatakana();
		
		if(attributes.containsKey("background_img")) {
			try {
				String resource = attributes.get("background_img");
				if(!resource.equals(""))
					background = TextureLoader.getInstance().getTexture(Menu.dir+"/"+attributes.get("background_img"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} }
		
		if(attributes.containsKey("value_default")) {
			value = new StringBuffer(attributes.get("value_default"));
		}
		
		String[] axes = attributes.get("location").split("\\s*[,x\\s]\\s*");
		location = new int[2];
		location[0] = Integer.parseInt(axes[0]);
		location[1] = Integer.parseInt(axes[1]);
		
		if(attributes.containsKey("text_color")){
			String[] colors = attributes.get("text_color").split("\\s*,\\s*");
			this.fontcolor = new Color(Integer.parseInt(colors[0]),Integer.parseInt(colors[1]),Integer.parseInt(colors[2]));
		}
		if(attributes.containsKey("select_color")){
			String[] colors = attributes.get("select_color").split("\\s*,\\s*");
			this.selectcolor = new Color(Integer.parseInt(colors[0]),Integer.parseInt(colors[1]),Integer.parseInt(colors[2]));
		}
		if(attributes.containsKey("font")) {
			this.font = new Font(attributes.get("font"),Font.PLAIN,Integer.parseInt(attributes.get("font_size")));
		}
		if(attributes.containsKey("font_scale")) {
			this.scale = Float.parseFloat(attributes.get("font_scale"));
		}
		if(attributes.containsKey("hidden")) {
			hidden = Boolean.parseBoolean(attributes.get("hidden"));
		}
	}
	
	public static void generateBankLatin() {
		if(characterBanks == null) {
			characterBanks = new ArrayList<char[]>(); }
		char[] newBank = new char[100];
		char pointer = '\u0020';
		
		int place = 0;
		for(;pointer <= '\u0040';pointer++) {
			newBank[place++] = pointer;
		}
		
		for(pointer = '\u005b';pointer <= '\u0060';pointer++) {
			newBank[place++] = pointer;
		}
		
		for(pointer = '\u007b';pointer <= '\u007e';pointer++) {
			newBank[place++] = pointer;
		}
		
		place++;
		
		for(pointer = '\u0041';pointer <= '\u005a';pointer++) {
			newBank[place++] = pointer;
		}
		
		place++;
		
		for(pointer = '\u0061';pointer <= '\u007a';pointer++) {
			newBank[place++] = pointer;
		}
		
		characterBanks.add(newBank);
	}
	
	public static void generateBankHiragana() {
		if(characterBanks == null) {
			characterBanks = new ArrayList<char[]>(); }
		char[] newBank = new char[100];
		char pointer = '\u0020';
		
		int place = 0;
		newBank[place++] = pointer;
		
		for(pointer = '\u3041';pointer <= '\u3096';pointer++) {
			newBank[place++] = pointer;
		}
		
		for(pointer = '\u3099';pointer <= '\u309f';pointer++) {
			newBank[place++] = pointer;
		}
		
		characterBanks.add(newBank);
	}
	
	public static void generateBankKatakana() {
		if(characterBanks == null) {
			characterBanks = new ArrayList<char[]>(); }
		char[] newBank = new char[100];
		char pointer = '\u0020';
		
		int place = 0;
		newBank[place++] = pointer;
		
		for(pointer = '\u30a0';pointer <= '\u30ff';pointer++) {
			newBank[place++] = pointer;
		}
		
		characterBanks.add(newBank);
	}
	
	/*public static void main(String... args) {
		generateBankLatin();
		generateBankHiragana();
		generateBankKatakana();
		System.out.println(characterBanks.get(0));
		System.out.println(characterBanks.get(1));
		System.out.println(characterBanks.get(2));
	}*/

	@Override
	public int[] getLocation() {
		return location;
	}

	@Override
	public void drawSelf(double alpha) {
		int f_size = Integer.parseInt(attributes.get("font_size"));
		int xspacing = (int) ((f_size+3)*scale);
		int yspacing = (int) ((f_size+6)*scale);
		
		if(inputLocked || !hidden) {
		if(background != null) {
			int[] offset = new int[2];
			if(attributes.containsKey("background_img_offset")) {
				String[] axes = attributes.get("background_img_offset").split("\\s*[,x\\s]\\s*");
				offset[0] = Integer.parseInt(axes[0]);
				offset[1] = Integer.parseInt(axes[1]);
			}
			
			glPushMatrix();
			GL11.glTranslatef(location[0]+offset[0],location[1]+offset[1]-background.getImageHeight(),0);
			GL11.glEnable(GL_TEXTURE_RECTANGLE_ARB);

			// bind to the appropriate texture for this sprite
			background.bind();
			
			GL11.glColor4d(1,1,1,alpha);

			// draw a quad textured to match the sprite
			glBegin(GL_QUADS);
			{
				glTexCoord2f(0, background.getImageHeight());
				glVertex2f(0, 0);
				
				glTexCoord2f(background.getImageWidth(), background.getImageHeight());
				glVertex2f(background.getImageWidth(), 0);
				
				glTexCoord2f(background.getImageWidth(), 0);
				glVertex2f(background.getImageWidth(), background.getImageHeight());

				glTexCoord2f(0, 0);
				glVertex2f(0, background.getImageHeight());
			}
			glEnd();

			GL11.glDisable(GL_TEXTURE_RECTANGLE_ARB);
			// restore the model view matrix to prevent contamination
			glPopMatrix();
			
		}
		}
		
		if(inputLocked) {
			glPushMatrix();
			GL11.glColor4ub((byte)selectcolor.getRed(),(byte)selectcolor.getGreen(),(byte)selectcolor.getBlue(),(byte)(selectcolor.getAlpha()*alpha));
			
			if(select_y != 5) {
				GL11.glTranslatef(location[0]+select_x*xspacing-4, location[1]-(select_y+2)*yspacing, 0);
				glBegin(GL_QUADS);
				glVertex2d(0,0);
				glVertex2d(xspacing,0);
				glVertex2d(xspacing,yspacing);
				glVertex2d(0,yspacing);
				glEnd();
			} else {
				if(select_x < 10) {
					GL11.glTranslatef(location[0]+2*xspacing, location[1]-(select_y+2)*yspacing, 0);
				} else {
					GL11.glTranslatef(location[0]+14*xspacing, location[1]-(select_y+2)*yspacing, 0);
				}
				glBegin(GL_QUADS);
				glVertex2d(0,0);
				glVertex2d(100,0);
				glVertex2d(100,yspacing);
				glVertex2d(0,yspacing);
				glEnd();
			}
			glPopMatrix();
		}
		
		int[] offset = new int[2];
		if(attributes.containsKey("value_offset")) {
			String[] axes = attributes.get("value_offset").split("\\s*[,x\\s]\\s*");
			offset[0] = Integer.parseInt(axes[0]);
			offset[1] = Integer.parseInt(axes[1]);
		}
		Text.renderText(value.toString(), location[0]+offset[0], location[1]+offset[1], font, new Color(fontcolor.getRed(),fontcolor.getGreen(),fontcolor.getBlue(),(int) (selectcolor.getAlpha()*alpha)), Text.Alignment.ALIGN_LEFT,scale);
		
		if(attributes.containsKey("text")) {
			offset = new int[2];
			if(attributes.containsKey("text_offset")) {
				String[] axes = attributes.get("text_offset").split("\\s*[,x\\s]\\s*");
				offset[0] = Integer.parseInt(axes[0]);
				offset[1] = Integer.parseInt(axes[1]);
			}
			Text.renderText(attributes.get("text"), location[0]+offset[0], location[1]+offset[1], font, new Color(fontcolor.getRed(),fontcolor.getGreen(),fontcolor.getBlue(),(int) (selectcolor.getAlpha()*alpha)), Text.Alignment.ALIGN_LEFT,scale);
		}
		
		if(inputLocked || !hidden) {
		for(int y = 0;y < 5;y++) {
			for(int x = 0;x < 20;x++) {
				Text.renderText(characterBanks.get(selectedBank)[x+y*20]+"", location[0]+x*xspacing, location[1]-(y+1)*yspacing, font, new Color(fontcolor.getRed(),fontcolor.getGreen(),fontcolor.getBlue(),(int) (255*alpha)), Text.Alignment.ALIGN_LEFT,scale);
			}
		}
		Text.renderText("More", location[0]+3*xspacing, location[1]-6*yspacing, font, new Color(fontcolor.getRed(),fontcolor.getGreen(),fontcolor.getBlue(),(int) (255*alpha)), Text.Alignment.ALIGN_LEFT,scale);
		Text.renderText("Done", location[0]+15*xspacing, location[1]-6*yspacing, font, new Color(fontcolor.getRed(),fontcolor.getGreen(),fontcolor.getBlue(),(int) (255*alpha)), Text.Alignment.ALIGN_LEFT,scale);
		}
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getValue() {
		return value.toString();
	}

	@Override
	public boolean inputLocked() {
		return inputLocked;
	}

	@Override
	public String input(int key) {
		switch(key) {
			case Keyboard.KEY_RETURN: 
				if(inputLocked) {
				if(select_y == 5) {
					if(select_x > 9) {
						inputLocked = false;
					} else {
						selectedBank++;
						if(selectedBank == characterBanks.size())
							selectedBank = 0;
					}
				} else {
					if(value.length() < 7)
						value.append(characterBanks.get(selectedBank)[select_x+select_y*20]);
				}} else {
					inputLocked = true;
				}
				break;
			case Keyboard.KEY_ESCAPE:
				inputLocked = false;
				break;
			case Keyboard.KEY_LEFT:
				if(select_y == 5) {
					select_x -= 10;
				} else {
					select_x --;
				}
				break;
			case Keyboard.KEY_RIGHT:
				if(select_y == 5) {
					select_x += 10;
				} else {
					select_x ++;
				}
				break;
			case Keyboard.KEY_UP:
				select_y --;
				break;
			case Keyboard.KEY_DOWN:
				select_y ++;
				break;
			case Keyboard.KEY_DELETE:
			case Keyboard.KEY_BACK:
				if(value.length() > 0)
					value.deleteCharAt(value.length()-1);
			default:
		}
		if(select_x < 0)
			select_x = 19;
		if(select_x > 19)
			select_x = 0;
		if(select_y < 0)
			select_y = 5;
		if(select_y > 5)
			select_y = 0;
		return null;
	}
}
