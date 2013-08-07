package menubuilder;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
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
	
	public static void renderText(String input, int x, int y, Font style, Color color) {
		renderText(input, x, y, style, color, Alignment.ALIGN_LEFT,1);
	}
	
	public static void renderText(String input, int x, int y) {
		renderText(input,x,y,null,null, Alignment.ALIGN_LEFT,1);
	}
	public static void renderText(String input, int x, int y, Alignment align) {
		renderText(input,x,y,null,null, align,1);
	}
	
	public static void renderText(String input, int x, int y, Font style, Color color, Alignment align, float scale) {
		BufferedImage drawnString = makeString(input,style,color);
		if(drawnString == null)
			return;
		switch(align) {
		case ALIGN_RIGHT: x-= drawnString.getWidth()*scale; break;
		case ALIGN_CENTER: x-= drawnString.getWidth()/2*scale; break;
		case ALIGN_LEFT:
		}
		byte[] data = ((DataBufferByte) drawnString.getRaster().getDataBuffer()).getData();

        ByteBuffer imageBuffer = ByteBuffer.allocateDirect(data.length);
        imageBuffer.order(ByteOrder.nativeOrder());
        imageBuffer.put(data, 0, data.length);
        imageBuffer.flip();
        int[] offsets = Locale.getFontOffset();
		GL11.glPushMatrix();
			GL11.glColor3f(1, 1, 1);
			GL11.glRasterPos2i(x+offsets[0], y+offsets[1]);
			GL11.glPixelZoom( scale, -scale );
			GL11.glDrawPixels(drawnString.getWidth(), drawnString.getHeight(), GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, imageBuffer);
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
    	
    	if(w == 0 || h ==0)
    		return null;
    				
    	WritableRaster raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,w,h,4,null);
        BufferedImage texImage = new BufferedImage(glAlphaColorModel,raster,false,null);
    	g = (Graphics2D)texImage.getGraphics();
    	g.setFont(style);
    	g.setColor(color);
    	g.drawString(input, 0, metrics.getHeight());
    	
        return texImage;
    }
}
