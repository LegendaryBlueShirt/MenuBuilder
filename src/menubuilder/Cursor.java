package menubuilder;

import static org.lwjgl.opengl.ARBTextureRectangle.GL_TEXTURE_RECTANGLE_ARB;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glVertex2f;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;

import menubuilder.Text.Alignment;

import org.lwjgl.opengl.GL11;

public class Cursor {
	Texture img;
	Color color,blinkColor;
	Alignment alignment;
	int width,height;
	boolean blink, blinkOut = false, moving = false;
	int blinkTime, blinkTimer = 0, blinkMode;
	int[] currentLoc, destinationLoc;
	
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
		int x = currentLoc[0];
		int y = currentLoc[1];
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
			x += (int) Math.floor((destinationLoc[0] - currentLoc[0]) * (movetimer/timetomove));
			y += (int) Math.floor((destinationLoc[1] - currentLoc[1]) * (movetimer/timetomove));
			movetimer++;
			if(movetimer == timetomove) {
				moving = false;
				currentLoc = destinationLoc;
			}
		}
		
		glPushMatrix();
		boolean hasTex = false;
		if(img != null) {
			GL11.glEnable(GL_TEXTURE_RECTANGLE_ARB);
			img.bind();
			hasTex = true;
		} else
			GL11.glDisable(GL_TEXTURE_RECTANGLE_ARB);
		GL11.glColor4ub((byte)drawColor.getRed(),(byte)drawColor.getGreen(),(byte)drawColor.getBlue(),(byte)drawColor.getAlpha());
		
		GL11.glTranslatef(x-4, y, 0);
		switch(alignment){
			case ALIGN_RIGHT: GL11.glTranslatef(-width, 0, 0);break;
			case ALIGN_CENTER: GL11.glTranslatef(-width/2, 0, 0);break;
			case ALIGN_LEFT:
		}
		
		glBegin(GL_QUADS);
		{
			if(hasTex)
				glTexCoord2f(0, img.getImageHeight());
			glVertex2f(0, -height);
			
			if(hasTex)
				glTexCoord2f(img.getImageWidth(), img.getImageHeight());
			glVertex2f(width, -height);
			
			if(hasTex)
				glTexCoord2f(img.getImageWidth(), 0);
			glVertex2f(width, 0);

			if(hasTex)
				glTexCoord2f(0, 0);
			glVertex2f(0, 0);
		}
		glEnd();
		GL11.glDisable(GL_TEXTURE_RECTANGLE_ARB);
		glPopMatrix();
		
	}
	
	public void setLocation(int[] coord) {
		currentLoc = coord;
	}
	
	public void move(int[] coord, int timetomove) {
		if(destinationLoc != null) {
			currentLoc = destinationLoc; }
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