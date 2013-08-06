package menubuilder;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glVertex2f;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import menubuilder.Text.Alignment;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import util.Coord;

public class Screen {
	enum SelectionMode {
		HORIZONTAL, VERTICAL;
	}
	static final String KEYWORD_QUIT = "quit";
	static final String KEYWORD_DUNGEON = "dungeon";
	static final String KEYWORD_MAIN = "main";
	
	public static HashMap<String, Screen> screens = new HashMap<String,Screen>();
	private static Pattern commentStrip = Pattern.compile("(^.*?)(//.*)?(;.*)?$");
	private static Pattern everythingelse = Pattern.compile("(\\[(.*)\\])*(.+=.+)*");
	
	private HashMap<String, String> attributes = new HashMap<String, String>();
	private ArrayList<Option> options = new ArrayList<Option>();
	private ArrayList<Cursor> cursors = new ArrayList<Cursor>();
	private Texture background;
	int selected_default,selected_current,cursor_current,cursor_idle=0,cursor_moving=0,cursor_selecting=0,cursor_travel_time=0;
	boolean selectionwrap = false;
	SelectionMode selectionmode = SelectionMode.VERTICAL;
	Screen prevscreen = null;
	int fadeTimer = 0,fadeTime = 0,transitionTime = 0;
	
	public static Screen getMainScreen() {
		if(!screens.containsKey(KEYWORD_MAIN)) {
			try {
				loadScreen(new File("resource/screens/main.txt"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return screens.get(KEYWORD_MAIN);
	}
	
	public static void loadScreen(String name) {
		if(name.equalsIgnoreCase(KEYWORD_MAIN))
			return;
		if(name.equalsIgnoreCase(KEYWORD_QUIT))
			return;
		if(name.equalsIgnoreCase(KEYWORD_DUNGEON))
			return;
		
		try {
			loadScreen(new File("resource/screens/"+name+".txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void loadScreen(File file) throws IOException {
		Screen newScreen = new Screen();
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		String line = null, name = null;
		Matcher matcher;
		boolean inBlock = false;
		boolean optionMode = false, cursorMode = false;
		HashMap<String, String> optionattribs = new HashMap<String, String>();
		HashMap<String, String> cursorattribs = new HashMap<String, String>();
		int linecount = 0;
		
		while(reader.ready()) {
			matcher = commentStrip.matcher(reader.readLine());
			linecount++;
			matcher.find();
			line = matcher.group(1).trim();
			if(line.length() == 0)
				continue;
			matcher = everythingelse.matcher(line);
			matcher.find();
			if(matcher.group(2) != null) {
				if(optionMode){
					newScreen.addOption(new Option(optionattribs));
					if(!screens.containsKey(optionattribs.get("action"))) {
						loadScreen(optionattribs.get("action"));
					}
					optionattribs.clear();
					optionMode = false;
				} else if(cursorMode) {
					newScreen.addCursor(new Cursor(cursorattribs));
					cursorattribs.clear();
					cursorMode = false;
				}
				if(!inBlock){
					name = matcher.group(2).toLowerCase();
					screens.put(name,newScreen);
					inBlock = true;
				} else if(matcher.group(2).toLowerCase().equals("option")) {
					optionMode = true;
				} else if(matcher.group(2).toLowerCase().equals("cursor")) {
					cursorMode = true;
				} else {
					System.err.println("Invalid block type "+matcher.group(2)+" at line "+linecount);
				}
			} else if(matcher.group(3) != null) {
				String[] pair = matcher.group(3).split("\\s*=\\s*");
				if(inBlock && !optionMode && !cursorMode){
					newScreen.addAttribute(pair[0].toLowerCase(), pair[1]);
				} else if(optionMode) {
					optionattribs.put(pair[0].toLowerCase(), pair[1]);
				} else if(cursorMode) {
					cursorattribs.put(pair[0].toLowerCase(), pair[1]);
				}
			}
		}
		if(optionMode) {
			newScreen.addOption(new Option(optionattribs));
			if(!screens.containsKey(optionattribs.get("action"))) {
				loadScreen(optionattribs.get("action"));
			}
		}
		if(cursorMode) {
			newScreen.addCursor(new Cursor(cursorattribs));
		}
		//System.out.println(newScreen.attributes);
		newScreen.init();
		reader.close();
	}
	
	private void init() {
		try {
			background = TextureLoader.getInstance().getTexture("resource/screens/"+attributes.get("background_img"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		selected_default = Integer.parseInt(attributes.get("default"));
		selectionwrap = attributes.get("wrap_selection").equalsIgnoreCase("true");
		cursor_idle = Integer.parseInt(attributes.get("cursor_idle"));
		cursor_moving = Integer.parseInt(attributes.get("cursor_moving"));
		cursor_selecting = Integer.parseInt(attributes.get("cursor_selecting"));
		cursor_travel_time = Integer.parseInt(attributes.get("cursor_travel_time"));
		if(attributes.containsKey("transition_time"))
			transitionTime = Integer.parseInt(attributes.get("transition_time"));
		for(Cursor c:cursors) {
			c.setLocation(options.get(selected_current).getLocation());
		}
		prep();
	}
	
	private Screen prep() {
		selected_current = selected_default;
		cursor_current = cursor_idle;
		for(Cursor c:cursors) {
			c.move(options.get(selected_current).getLocation(),cursor_travel_time);
		}
		return this;
	}
	
	public void setPrevScreen(Screen screen, int transitionTime) {
		prevscreen = screen;
		fadeTimer = 0;
		this.fadeTime = transitionTime; 
	}
	
	public void drawSelf() {
		drawSelf(1,1);
	}
	
	public void drawSelf(int fadetime, int fadeduration) {
		// store the current model matrix
		glPushMatrix();
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);

		// bind to the appropriate texture for this sprite
		background.bind();
		
		GL11.glColor4d(1,1,1,1.0*fadetime/fadeduration);

		// draw a quad textured to match the sprite
		glBegin(GL_QUADS);
		{
			glTexCoord2f(0, background.getHeight());
			glVertex2f(-160, -120);

			glTexCoord2f(0, 0);
			glVertex2f(-160, 120);

			glTexCoord2f(background.getWidth(), 0);
			glVertex2f(160, 120);

			glTexCoord2f(background.getWidth(), background.getHeight());
			glVertex2f(160, -120);
		}
		glEnd();

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		// restore the model view matrix to prevent contamination
		glPopMatrix();
		
		if(prevscreen != null) {
			prevscreen.drawSelf(this.fadeTime-this.fadeTimer, this.fadeTime);
			this.fadeTimer++;
			if(this.fadeTimer >= this.fadeTime)
				prevscreen = null;
		} else if(fadeduration == 1){
		
			for(Option o: options) {
				o.drawSelf();
			}
		
			Cursor theCursor = cursors.get(cursor_current);
			if(cursor_current == cursor_moving && !theCursor.isMoving()) {
				cursor_current = cursor_idle;
				theCursor = cursors.get(cursor_current);
				theCursor.setLocation(options.get(selected_current).getLocation());
			}
			theCursor.drawSelf();
		}
	}
	
	public void incrementSelection() {
		if(selected_current < options.size()-1)
			selected_current++;
		else if(selectionwrap)
			selected_current = 0;
		else return;
		cursor_current = cursor_moving;
		cursors.get(cursor_current).move(options.get(selected_current).getLocation(), cursor_travel_time);
	}
	
	public void decrementSelection() {
		if(selected_current > 0)
			selected_current--;
		else if(selectionwrap)
			selected_current = options.size()-1;
		else return;
		cursor_current = cursor_moving;
		cursors.get(cursor_current).move(options.get(selected_current).getLocation(), cursor_travel_time);
	}
	
	private void addAttribute(String key, String value) {
		attributes.put(key, value);
	}
	private void addOption(Option option) {
		options.add(option);
	}
	private void addCursor(Cursor cursor) {
		cursors.add(cursor);
	}
	
	public Screen input(int key) {
		if(prevscreen != null)
			return this;
		switch(key) {
			case Keyboard.KEY_RETURN: return exec();
			default:
		}
		switch(selectionmode) {
			case VERTICAL:
				switch(key) {
					case Keyboard.KEY_UP: decrementSelection(); break;
					case Keyboard.KEY_DOWN: incrementSelection(); break;
					default:
				} break;
			case HORIZONTAL:
				switch(key) {
					case Keyboard.KEY_LEFT: decrementSelection(); break;
					case Keyboard.KEY_RIGHT: incrementSelection(); break;
					default:
				} break;
		}
		return this;
	}
	
	public Screen exec() {
		String doAction = options.get(selected_current).getAction();
		if(doAction.equalsIgnoreCase(KEYWORD_QUIT)) {
			System.exit(0);
		} else if(doAction.equalsIgnoreCase(KEYWORD_DUNGEON)) {
			return null;
		} else if(screens.get(doAction) != this){
			screens.get(doAction).prep();
			screens.get(doAction).setPrevScreen(this, transitionTime);
			return screens.get(doAction);
		}
		return this;
	}
	
}

class Option {
	String action, text;
	Coord location;
	Alignment alignment;
	Color fontcolor;
	
	Option(HashMap<String,String> attributes) {
		this(attributes.get("text"),
				attributes.get("action"),
				attributes.get("location"),
				attributes.get("text_align"),
				attributes.get("text_color"));
	}
	
	Option(String text, String action, String location, String alignment, String fontcolor) {
		this.text = text;
		this.action = action.toLowerCase();
		String[] axes = location.split("\\s*[,x\\s]\\s*");
		this.location = new Coord(Integer.parseInt(axes[0]),Integer.parseInt(axes[1]));
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
	public String getAction() {
		return action;
	}
	public Coord getLocation() {
		return location;
	}
	
	public void drawSelf() {
		Text.renderText(text, location, Locale.getNameFont(), fontcolor, alignment);
	}
}

class Cursor {
	Texture img;
	Color color,blinkColor;
	Alignment alignment;
	int width,height;
	boolean blink, blinkOut = false, moving = false;
	int blinkTime, blinkTimer = 0, blinkMode;
	Coord currentLoc,destinationLoc;
	
	int movetimer;
	double timetomove;
	
	Cursor(HashMap<String, String> attributes) {
		int red=255,green=255,blue=255,alpha = 255;
		if(attributes.containsKey("alpha"))
			alpha = Integer.parseInt(attributes.get("alpha"));
		if(attributes.containsKey("color")) {
			String[] components = attributes.get("color").split(",");
			red = Integer.parseInt(components[0]);
			green = Integer.parseInt(components[1]);
			blue = Integer.parseInt(components[2]);
		}
		color = new Color(red,green,blue,alpha);
		
		if(attributes.containsKey("img")){
			try {
				img = TextureLoader.getInstance().getTexture(attributes.get("img"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		switch(attributes.get("align").charAt(0)) {
			case 'C':
			case 'c': this.alignment = Alignment.ALIGN_CENTER; break;
			case 'R':
			case 'r': this.alignment = Alignment.ALIGN_RIGHT; break;
			case 'L':
			case 'l':
			default: this.alignment = Alignment.ALIGN_LEFT; break;
		}
		
		if(attributes.containsKey("size")) {
			String[] dims = attributes.get("size").split("[,x\\s]");
			width = Integer.parseInt(dims[0]);
			height = Integer.parseInt(dims[1]);
		} else if(img != null) {
			width = img.getImageWidth();
			height = img.getImageHeight();
		} else {
			System.err.println("Undefined size for cursor!");
			System.exit(-1);
		}
		
		blink = "true".equalsIgnoreCase(attributes.get("blink"));
		if(blink) {
			red=255;green=255;blue=255;alpha = 255;
			if(attributes.containsKey("blink_alpha"))
				alpha = Integer.parseInt(attributes.get("blink_alpha"));
			if(attributes.containsKey("blink_color")) {
				String[] components = attributes.get("blink_color").split(",");
				red = Integer.parseInt(components[0]);
				green = Integer.parseInt(components[1]);
				blue = Integer.parseInt(components[2]);
			}
			blinkColor = new Color(red,green,blue,alpha);
			blinkTime = Integer.parseInt(attributes.get("blink_time"));
			
			blinkMode = 0;
			if(attributes.containsKey("blink_transition")) {
				String transition = attributes.get("blink_transition");
				if(transition.equalsIgnoreCase("linear"))
					blinkMode = 1;
				else if(transition.equalsIgnoreCase("sine"))
					blinkMode = 2;
			}
		}
	}
	
	public Color interpolateColor(Color a, Color b, int step, int distance) {
		double factor = 1.0*step/distance;
		int red = a.getRed() + (int) ((b.getRed() - a.getRed())* factor);
		int green = a.getGreen() + (int) ((b.getGreen() - a.getGreen())* factor);
		int blue = a.getBlue() + (int) ((b.getBlue() - a.getBlue())* factor);
		int alpha = a.getAlpha() + (int) ((b.getAlpha() - a.getAlpha())* factor);
		
		return new Color(red,green,blue,alpha);
	}
	
	public Color sineColor(Color a, Color b, int step, int distance) {
		double factor = Math.sin((Math.PI*step)/distance);
		int red = a.getRed() + (int) ((b.getRed() - a.getRed())* factor);
		int green = a.getGreen() + (int) ((b.getGreen() - a.getGreen())* factor);
		int blue = a.getBlue() + (int) ((b.getBlue() - a.getBlue())* factor);
		int alpha = a.getAlpha() + (int) ((b.getAlpha() - a.getAlpha())* factor);
		
		return new Color(red,green,blue,alpha);
	}
	
	public void drawSelf() {
		int x = currentLoc.x;
		int y = currentLoc.y;
		Color drawColor = color;
		if(blink) {
			blinkTimer++;
			if(blinkTimer == blinkTime) {
				blinkOut = !blinkOut;
				blinkTimer = 0;
			}
			if(blinkMode == 0) {
				if(blinkOut)
					drawColor = blinkColor;
			} else if(blinkMode == 1) {
				if(!blinkOut) {
					drawColor = interpolateColor(color,blinkColor,blinkTimer,blinkTime);
				} else {
					drawColor = interpolateColor(blinkColor,color,blinkTimer,blinkTime);
				}
			} else if(blinkMode == 2) {
				drawColor = sineColor(color,blinkColor,blinkTimer,blinkTime);
			}
		}
		
		if(moving) {
			x += (int) Math.floor((destinationLoc.x - currentLoc.x) * (movetimer/timetomove));
			y += (int) Math.floor((destinationLoc.y - currentLoc.y) * (movetimer/timetomove));
			movetimer++;
			if(movetimer == timetomove) {
				moving = false;
				currentLoc = destinationLoc;
			}
		}
		
		glPushMatrix();
		boolean hasTex = false;
		if(img != null) {
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			img.bind();
			hasTex = true;
		} else
			GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor4ub((byte)drawColor.getRed(),(byte)drawColor.getGreen(),(byte)drawColor.getBlue(),(byte)drawColor.getAlpha());
		
		GL11.glTranslatef(x, y, 0);
		switch(alignment){
			case ALIGN_RIGHT: GL11.glTranslatef(-width, 0, 0);break;
			case ALIGN_CENTER: GL11.glTranslatef(-width/2, 0, 0);break;
			case ALIGN_LEFT:
		}
		
		glBegin(GL_QUADS);
		{
			if(hasTex)
				glTexCoord2f(0, img.getHeight());
			glVertex2f(0, -height);

			if(hasTex)
				glTexCoord2f(0, 0);
			glVertex2f(0, 0);

			if(hasTex)
				glTexCoord2f(img.getWidth(), 0);
			glVertex2f(width, 0);

			if(hasTex)
				glTexCoord2f(img.getWidth(), img.getHeight());
			glVertex2f(width, -height);
		}
		glEnd();
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		glPopMatrix();
		
	}
	
	public void setLocation(Coord coord) {
		currentLoc = coord;
	}
	
	public void move(Coord coord, int timetomove) {
		if(currentLoc == null || timetomove == 0){
			currentLoc = coord;
		}
		if(currentLoc == coord)
			return;
		moving = true;
		movetimer = 0;
		this.timetomove = timetomove;
		destinationLoc = coord;
	}
	
	public boolean isMoving() {
		return moving;
	}
}