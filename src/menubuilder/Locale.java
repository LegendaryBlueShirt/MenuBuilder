package menubuilder;

import java.awt.Font;
import java.util.HashMap;

public class Locale {
	public enum Language {
		ENGLISH("english"),JAPANESE("japanese");
		
		private static HashMap<String,Language> match;
		private Language(String key){
			Language.addLanguage(key,this);
		}
		private static void addLanguage(String key,Language value) {
			if(match == null)
				match = new HashMap<String,Language>();
			match.put(key, value);
		}
		public static Language getLanguage(String key) {
			if(!match.containsKey(key)){
				return ENGLISH;
			}
			return match.get(key);
		}
	}
	//public static Language locale=Language.ENGLISH;
	public static Language locale = Language.getLanguage(Config.getInstance().getProperty("locale"));
	
	public static void setLocale(Language newLocale) {
		locale = newLocale;
	}
	
	public static Font getNameFont() {
		switch(locale) {
			case ENGLISH: return new Font ("Lucida Sans Typewriter", Font.PLAIN, 10);
			case JAPANESE: return new Font ("MS Gothic", Font.PLAIN, 12);
		}
		return null;
	}
	
	public static Font getDialogFont() {
		switch(locale) {
			case ENGLISH: return new Font ("Lucida Sans Typewriter", Font.PLAIN, 10);
			case JAPANESE: return new Font ("MS Gothic", Font.PLAIN, 12);
		}
		return null;
	}
	
	public static Font getHPFont() {
		return new Font ("Arial", Font.PLAIN, 8);
	}
	
	public static int[] getFontOffset() {
		switch(locale) {
			case ENGLISH: return new int[]{-1,0};
			default:
			case JAPANESE: return new int[]{0,-1};
		}
	}
}
