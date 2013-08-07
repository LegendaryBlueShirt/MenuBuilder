package load;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IniReader {
	private static Pattern commentStrip = Pattern.compile("(^.*?)(//.*)?(;.*)?$");
	private static Pattern iniMatch = Pattern.compile("(\\[(.*)\\])*(.+=.+)*");
	BufferedReader reader;
	boolean hasGroup = false;
	int linecount = 0;
	
	String currentGroup = "";
	
	public IniReader(String path) {
		this(new File(path));
	}
	
	public IniReader(File file) {
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			getGroup();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public boolean hasGroup() {
		return hasGroup;
	}
	
	public Group getGroup() throws IOException {
		Group newGroup = new Group(currentGroup);
		HashMap<String, String> attributes = new HashMap<String, String>();
		Matcher matcher;
		while(reader.ready()) {
			linecount++;
			String line = stripComments(reader.readLine());
			if(line.length() == 0)
				continue;
			matcher = iniMatch.matcher(line);
			matcher.find();
			if(matcher.group(2) != null) {
				hasGroup = true;
				newGroup.setAttributes(attributes);
				currentGroup = matcher.group(2);
				return newGroup;
			} else if(matcher.group(3) != null) {
				String[] pair = matcher.group(3).split("\\s*=\\s*");
				attributes.put(pair[0].toLowerCase(), pair[1]);
			}
		}
		hasGroup = false;
		newGroup.setAttributes(attributes);
		reader.close();
		return newGroup;
	}
	
	public static String stripComments(String input) {
		Matcher m = commentStrip.matcher(input);
		m.find();
		return m.group(1).trim();
	}
	
}