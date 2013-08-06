package menubuilder;

import java.awt.Font;

public class Locale {
	public enum Language {
		ENGLISH,JAPANESE;
	}
	public static Language locale = Language.JAPANESE;
	
	public static void setLocale(Language newLocale) {
		locale = newLocale;
	}
	
	public static Font getNameFont() {
		switch(locale) {
			case ENGLISH: return new Font ("Arial Narrow", Font.PLAIN, 12);
			case JAPANESE: return new Font ("MS Gothic", Font.PLAIN, 12);
		}
		return null;
	}
	
	public static Font getDialogFont() {
		switch(locale) {
			case ENGLISH: return new Font ("Arial Narrow", Font.PLAIN, 12);
			case JAPANESE: return new Font ("MS Gothic", Font.PLAIN, 12);
		}
		return null;
	}
	
	public static Font getHPFont() {
		return new Font ("Arial", Font.PLAIN, 8);
	}
}
