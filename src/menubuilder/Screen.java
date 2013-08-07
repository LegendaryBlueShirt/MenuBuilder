package menubuilder;

import static org.lwjgl.opengl.ARBTextureRectangle.GL_TEXTURE_RECTANGLE_ARB;
import static org.lwjgl.opengl.GL11.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class Screen {
	enum SelectionMode {
		HORIZONTAL, VERTICAL;
	}
	
	private HashMap<String, String> attributes;
	private ArrayList<Option> options = new ArrayList<Option>();
	private ArrayList<Cursor> cursors = new ArrayList<Cursor>();
	private Texture background;
	int selected_default=0,selected_current=0,cursor_current,cursor_idle=0,cursor_moving=0,cursor_selecting=0,cursor_travel_time=0;
	boolean selectionwrap = false;
	SelectionMode selectionmode = SelectionMode.VERTICAL;
	int timer = 0;
	int backgroundFadeOutDelay = 0,backgroundFadeInDelay = 0, optionsFadeOutDelay = 0,optionsFadeInDelay = 0;
	double backgroundFadeOut,backgroundFadeIn,optionsFadeOut,optionsFadeIn;
	
	boolean fadingOut = false, fadingIn = false;
	
	public void init() {
		if(attributes.containsKey("background_img")) {
		try {
			String resource = attributes.get("background_img");
			if(!resource.equals(""))
				background = TextureLoader.getInstance().getTexture(Menu.dir+"/"+attributes.get("background_img"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} }
		selected_default = Integer.parseInt(attributes.get("default"));
		selectionwrap = attributes.get("wrap_selection").equalsIgnoreCase("true");
		cursor_idle = Integer.parseInt(attributes.get("cursor_idle"));
		cursor_moving = Integer.parseInt(attributes.get("cursor_moving"));
		cursor_selecting = Integer.parseInt(attributes.get("cursor_selecting"));
		cursor_travel_time = Integer.parseInt(attributes.get("cursor_travel_time"));
		if(attributes.containsKey("background_fade_out_delay"))
			backgroundFadeOutDelay = Integer.parseInt(attributes.get("background_fade_out_delay"));
		if(attributes.containsKey("background_fade_in_delay"))
			backgroundFadeInDelay = Integer.parseInt(attributes.get("background_fade_in_delay"));
		if(attributes.containsKey("background_fade_out"))
			backgroundFadeOut = Integer.parseInt(attributes.get("background_fade_out"));
		if(attributes.containsKey("background_fade_in"))
			backgroundFadeIn = Integer.parseInt(attributes.get("background_fade_in"));
		if(attributes.containsKey("options_fade_out_delay"))
			optionsFadeOutDelay = Integer.parseInt(attributes.get("options_fade_out_delay"));
		if(attributes.containsKey("options_fade_in_delay"))
			optionsFadeInDelay = Integer.parseInt(attributes.get("options_fade_in_delay"));
		if(attributes.containsKey("options_fade_out"))
			optionsFadeOut = Integer.parseInt(attributes.get("options_fade_out"));
		if(attributes.containsKey("options_fade_in"))
			optionsFadeIn = Integer.parseInt(attributes.get("options_fade_in"));
		for(Cursor c:cursors) {
			c.setLocation(options.get(selected_current).getLocation());
		}
		prep();
	}
	
	public void prep() {
		selected_current = selected_default;
		cursor_current = cursor_idle;
		for(Cursor c:cursors) {
			c.move(options.get(selected_current).getLocation(),cursor_travel_time);
		}
	}
	
	public void fadeOut() {
		timer = 0;
		fadingIn = false;
		fadingOut = true;
	}
	
	public void fadeIn() {
		timer = 0;
		fadingOut = false;
		fadingIn = true;
	}
	
	public boolean fadeComplete() {
		if(fadingIn){
		if(timer > (optionsFadeInDelay+optionsFadeIn) && timer > (backgroundFadeInDelay+backgroundFadeIn)) {
			return true;
		} } else if(fadingOut) {
		if(timer > (optionsFadeOutDelay+optionsFadeOut) && timer > (backgroundFadeOutDelay+backgroundFadeOut)) {
			return true;
		}} else {
			return true;
		}
		return false;
	}
	
	private double getBGAlpha() {
		double alpha = 1.0;
		
		if(fadingOut) {
			if(timer < backgroundFadeOutDelay) {
				alpha = 1.0;
			} else if(backgroundFadeOut == 0) {
				alpha = 0.0;
			} else {
				alpha = 1.0-(timer-backgroundFadeOutDelay)/backgroundFadeOut;
				if(alpha < 0)
					alpha = 0.0;
			}
		} else if(fadingIn) {
			if(timer < backgroundFadeInDelay) {
				alpha = 0.0;
			} else if(backgroundFadeIn == 0) {
				alpha = 1.0;
			} else {
				alpha = (timer-backgroundFadeInDelay)/backgroundFadeIn;
				if(alpha > 1)
					alpha = 1.0;
			}
		}
		return alpha;
	}
	
	private double getOptionsAlpha() {
		double alpha = 1.0;
		
		if(fadingOut) {
			if(timer < optionsFadeOutDelay) {
				alpha = 1.0;
			} else if(optionsFadeOut == 0) {
				alpha = 0.0;
			} else {
				alpha = 1.0-(timer-optionsFadeOutDelay)/optionsFadeOut;
				if(alpha < 0)
					alpha = 0.0;
			}
		} else if(fadingIn) {
			if(timer < optionsFadeInDelay) {
				alpha = 0.0;
			} else if(optionsFadeIn == 0) {
				alpha = 1.0;
			} else {
				alpha = (timer-optionsFadeInDelay)/optionsFadeIn;
				if(alpha > 1)
					alpha = 1.0;
			}
		}
		return alpha;
	}
	
	public void drawSelf() {
		timer++;
		
		if(background != null) {
		// store the current model matrix
		glPushMatrix();
		
		GL11.glEnable(GL_TEXTURE_RECTANGLE_ARB);

		// bind to the appropriate texture for this sprite
		background.bind();
		
		GL11.glColor4d(1,1,1,getBGAlpha());

		// draw a quad textured to match the sprite
		glBegin(GL_QUADS);
		{
			glTexCoord2f(0, background.getImageHeight());
			glVertex2f(-Menu.RES_X/2, -Menu.RES_Y/2);
			
			glTexCoord2f(background.getImageWidth(), background.getImageHeight());
			glVertex2f(Menu.RES_X/2, -Menu.RES_Y/2);
			
			glTexCoord2f(background.getImageWidth(), 0);
			glVertex2f(Menu.RES_X/2, Menu.RES_Y/2);

			glTexCoord2f(0, 0);
			glVertex2f(-Menu.RES_X/2, Menu.RES_Y/2);
		}
		glEnd();

		GL11.glDisable(GL_TEXTURE_RECTANGLE_ARB);
		// restore the model view matrix to prevent contamination
		glPopMatrix();
		
		}
		
		for(Option o: options) {
			o.drawSelf(getOptionsAlpha());
		}
		
		if(fadeComplete()){
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
	
	public void setAttributes(HashMap<String,String> attributes) {
		this.attributes = attributes;
	}
	public void addOption(Option option) {
		options.add(option);
	}
	public void addCursor(Cursor cursor) {
		cursors.add(cursor);
	}
	
	public String input(int key) {
		
		String returnval = String.format("%s,%s", 
				options.get(selected_current).input(key),
				options.get(selected_current).getValue().toLowerCase());
		
		if(!options.get(selected_current).inputLocked()) {
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
		}
		return returnval;
	}
}

