package menubuilder;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.lwjgl.opengl.GL11;

import util.Coord;

public class Text {
	public enum Alignment {
		ALIGN_LEFT,ALIGN_CENTER,ALIGN_RIGHT;
	}
	
	private static ColorModel glAlphaColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
            new int[] {8,8,8,8},
            true,
            false,
            ComponentColorModel.TRANSLUCENT,
            DataBuffer.TYPE_BYTE);
	
	public static void renderText(String input, Coord location, Alignment align) {
		renderText(input, location.x, location.y, align);
	}
	
	public static void renderText(String input, Coord location, Font style, Color color) {
		renderText(input, location.x, location.y, style, color, Alignment.ALIGN_LEFT);
	}
	
	public static void renderText(String input, Coord location, Font style, Color color, Alignment align) {
		renderText(input, location.x, location.y, style, color, align);
	}
	
	public static void renderText(String input, int x, int y, Font style, Color color) {
		renderText(input, x, y, style, color, Alignment.ALIGN_LEFT);
	}
	
	public static void renderText(String input, int x, int y) {
		renderText(input,x,y,null,null, Alignment.ALIGN_LEFT);
	}
	public static void renderText(String input, int x, int y, Alignment align) {
		renderText(input,x,y,null,null, align);
	}
	
	public static void renderText(String input, int x, int y, Font style, Color color, Alignment align) {
		BufferedImage drawnString = makeString(input,style,color);
		switch(align) {
		case ALIGN_RIGHT: x-= drawnString.getWidth(); break;
		case ALIGN_CENTER: x-= drawnString.getWidth()/2; break;
		case ALIGN_LEFT:
		}
		byte[] data = ((DataBufferByte) drawnString.getRaster().getDataBuffer()).getData();

        ByteBuffer imageBuffer = ByteBuffer.allocateDirect(data.length);
        imageBuffer.order(ByteOrder.nativeOrder());
        imageBuffer.put(data, 0, data.length);
        imageBuffer.flip();
		GL11.glPushMatrix();
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glColor3f(1, 1, 1);
			GL11.glRasterPos2i(x, y);
			GL11.glPixelZoom( 2, -2 );
			GL11.glDrawPixels(drawnString.getWidth(), drawnString.getHeight(), GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, imageBuffer);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glPopMatrix();
	}
	
	private static BufferedImage makeString(String input, Font style, Color color) {
    	if(style == null)
    		style = new Font ("MS Gothic", Font.PLAIN, 24);
    	if(color == null)
    		color = Color.black;
    	BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    	Graphics2D g = (Graphics2D)bi.getGraphics();
    	g.setFont(style);
    	FontMetrics metrics = g.getFontMetrics();
    	int w = metrics.stringWidth(input);
    	int h = metrics.getHeight() + metrics.getMaxDescent();
    	
    	WritableRaster raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,w,h,4,null);
        BufferedImage texImage = new BufferedImage(glAlphaColorModel,raster,false,null);
    	g = (Graphics2D)texImage.getGraphics();
    	g.setFont(style);
    	g.setColor(color);
    	g.drawString(input, 0, metrics.getHeight());
    	//g.drawString(input, 0, 0);
    	//System.out.println(input+"  "+w+", "+h);

        return texImage;
    }
	
	public static void main(String[] args) throws Exception {

	    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	    Font[] fonts = ge.getAllFonts();

	    for (Font f:fonts) {
	    	if(f.canDisplayUpTo("お") > -1) {
	    		System.out.print(f.getFontName() + " : ");
	    		System.out.print(f.getFamily() + " : ");
	    		System.out.print(f.getName());
	    		System.out.println();
	    	}
	    }
	  }
}
