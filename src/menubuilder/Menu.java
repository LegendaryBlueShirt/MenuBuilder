package menubuilder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.opengl.GL11;

import load.Group;
import load.IniReader;

public class Menu {
	enum SelectionMode {
		HORIZONTAL, VERTICAL;
	}
	static final String KEYWORD_QUIT = "quit";
	static final String KEYWORD_MAIN = "main";
	
	public static HashMap<String, Screen> screens = new HashMap<String,Screen>();

	int selected_default,selected_current,cursor_current,cursor_idle=0,cursor_moving=0,cursor_selecting=0,cursor_travel_time=0;
	boolean selectionwrap = false;
	private static Screen currentScreen = null,prevScreen = null;
	
	public static int RES_X,RES_Y;
	private static ArrayList<String> reservedWords;
	public static String dir;
	
	public static void getMainMenu(int RES_X, int RES_Y, ArrayList<String> reservedWords,String path) {
		Menu.RES_X = RES_X;
		Menu.RES_Y = RES_Y;
		Menu.reservedWords = reservedWords;
		
		dir = path;
		File start = new File(dir+"/main.txt");
		if(!screens.containsKey(KEYWORD_MAIN)) {
			try {
				loadScreen(start);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Menu.currentScreen = screens.get(KEYWORD_MAIN);
		Menu.prevScreen = null;
	}
	
	public static void loadScreen(String name) {
		if(name.equalsIgnoreCase(KEYWORD_MAIN))
			return;
		if(name.equalsIgnoreCase(KEYWORD_QUIT))
			return;
		if(reservedWords.contains(name))
			return;
		
		File start = new File(dir+"/"+name+".txt");
		try {
			loadScreen(start);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void loadScreen(File file) throws IOException {
		Screen newScreen = new Screen();
		IniReader reader = new IniReader(file);
		while(reader.hasGroup()) {
			Group g = reader.getGroup();
			if(g.getName().equalsIgnoreCase("option")) {
				try {
					Constructor<?> c = Class.forName("menubuilder."+g.getAttribute("type")).getConstructor(HashMap.class);
					newScreen.addOption((Option) c.newInstance(g.getAttributes()));
					if(g.hasAttribute("action"))
						if(!screens.containsKey(g.getAttribute("action").toLowerCase())) {
							loadScreen(g.getAttribute("action").toLowerCase());
						}
				} catch (ClassNotFoundException e) {
					System.err.println("Error loading class "+g.getAttribute("type"));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if(g.getName().equalsIgnoreCase("cursor")) {
				newScreen.addCursor(new Cursor(g.getAttributes()));
			} else {
				screens.put(g.getName().toLowerCase(),newScreen);
				newScreen.setAttributes(g.getAttributes());
			}
		}
		newScreen.init();
	}
	
	public static void render() {
		
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(-RES_X/2,RES_X/2,-RES_Y/2,RES_Y/2, 200, -200);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		
		
		if(prevScreen != null) {
			if(prevScreen.fadeComplete()) {
				prevScreen = null;
			} else {
				prevScreen.drawSelf();
			}
		}
		currentScreen.drawSelf();
	}
	
	public static String input(int key) {
		
		if(!currentScreen.fadeComplete())
			return "";
		
		String[] command = currentScreen.input(key).split(",");
		
		if(command[0].equals("goto")) {
			if(command[1].equalsIgnoreCase(KEYWORD_QUIT)) {
				System.exit(0);
			} else if(reservedWords.contains(command[1])) {
				return command[1];
			} else if(screens.get(command[1]) != currentScreen){
				screens.get(command[1]).prep();
				prevScreen = currentScreen;
				prevScreen.fadeOut();
				currentScreen = screens.get(command[1]);
				currentScreen.fadeIn();
			}
		} else if(command[0].equals("set")) {
			System.out.println(":D");
		}
		
		return "";
	}
}

