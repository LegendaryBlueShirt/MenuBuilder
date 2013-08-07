package menubuilder;

import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glViewport;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public class Example {
	
	ArrayList<String> reserved;
	
	float angle = 0.0f;
	
    float lightAmbient[] = { 0.5f, 0.5f, 0.5f, 1.0f };
    float lightDiffuse[] = { 1.0f, 1.0f, 1.0f, 1.0f };
    float lightPosition[] = { 0.0f, 0.0f, 150.0f, 1.0f };
	
	public void start() {
		try {
			Display.setDisplayMode(new DisplayMode(640,480));
			Display.setTitle("MenuBuilder Example");
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		configureGL();
		
		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
        ByteBuffer temp = ByteBuffer.allocateDirect(16);
        temp.order(ByteOrder.nativeOrder());
        GL11.glLight(GL11.GL_LIGHT1, GL11.GL_AMBIENT, (FloatBuffer)temp.asFloatBuffer().put(lightAmbient).flip());              // Setup The Ambient Light
        GL11.glLight(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, (FloatBuffer)temp.asFloatBuffer().put(lightDiffuse).flip());              // Setup The Diffuse Light
        GL11.glLight(GL11.GL_LIGHT1, GL11.GL_POSITION,(FloatBuffer)temp.asFloatBuffer().put(lightPosition).flip());         // Position The Light
        GL11.glEnable(GL11.GL_LIGHT1);                          // Enable Light One
		
		reserved = new ArrayList<String>();
		reserved.add("dungeon");
		Menu.getMainMenu(640,480,reserved,"screens");
		
		while (!Display.isCloseRequested()) {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			glLoadIdentity();
			
			pollInput();
			
			render3dCube();
			
			Menu.render();
			
			Display.update();
			Display.sync(60);
		}
		Display.destroy();
	}
	
	public void render3dCube() {
		
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GLU.gluPerspective(45.0f,4/3f,0.1f,100.0f);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		
		GL11.glPushMatrix();
		
		GL11.glEnable(GL11.GL_LIGHTING);
		
		GL11.glColorMaterial ( GL11.GL_FRONT, GL11.GL_AMBIENT_AND_DIFFUSE );
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		
		GL11.glTranslatef(0.0f,0.0f,-80.0f);
		GL11.glRotatef(angle++, 1.0f, 1.0f, 1.0f);
		GL11.glScalef(.7f, .7f, .7f);
		GL11.glColor3f(.4f,.8f,0);
		
		GL11.glBegin(GL11.GL_QUADS);
		
		GL11.glNormal3f( 0.0f, 0.0f, 1.0f);
		GL11.glVertex3f(20f, 20f, 20f);
		GL11.glVertex3f(-20f, 20f, 20f);
		GL11.glVertex3f(-20f, -20f, 20f);
		GL11.glVertex3f(20f, -20f, 20f);
		
		GL11.glNormal3f( 0.0f, 0.0f, -1.0f);
		GL11.glVertex3f(20f, -20f, -20f);
		GL11.glVertex3f(-20f, -20f, -20f);
		GL11.glVertex3f(-20f, 20f, -20f);
		GL11.glVertex3f(20f, 20f, -20f);
		
		GL11.glNormal3f( -1.0f, 0.0f, 0.0f);
		GL11.glVertex3f(-20f, -20f, -20f);
		GL11.glVertex3f(-20f, -20f, 20f);
		GL11.glVertex3f(-20f, 20f, 20f);
		GL11.glVertex3f(-20f, 20f, -20f);
		
		GL11.glNormal3f( 1.0f, 0.0f, 0.0f);
		GL11.glVertex3f(20f, -20f, 20f);
		GL11.glVertex3f(20f, -20f, -20f);
		GL11.glVertex3f(20f, 20f, -20f);
		GL11.glVertex3f(20f, 20f, 20f);
		
		GL11.glNormal3f( 0.0f, 1.0f, 0.0f);
		GL11.glVertex3f(-20f, 20f, 20f);
		GL11.glVertex3f(20f, 20f, 20f);
		GL11.glVertex3f(20f, 20f, -20f);
		GL11.glVertex3f(-20f, 20f, -20f);
		
		GL11.glNormal3f( 0.0f, -1.0f, 0.0f);
		GL11.glVertex3f(20f, -20f, 20f);
		GL11.glVertex3f(-20f, -20f, 20f);
		GL11.glVertex3f(-20f, -20f, -20f);
		GL11.glVertex3f(20f, -20f, -20f);
		GL11.glEnd();
		
		GL11.glDisable(GL11.GL_LIGHTING);
		
		GL11.glPopMatrix();
	}
	
	public void pollInput() {
		while (Keyboard.next()) {
			if(Keyboard.getEventKeyState()){
				String status = Menu.input(Keyboard.getEventKey());
				if(!status.equals("")){
					System.out.println(status);
				}
			}
		}
	}
	
	public void configureGL() {
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
		glViewport (0, 0, 640, 480);
		/*GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GLU.gluPerspective(45.0f,4/3f,0.1f,100.0f);
		//glOrtho(-320,320,-240,240, 200, -200);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);*/
		GL11.glEnable(GL11.GL_BLEND); //Enable alpha transparency on textures
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glClearColor(.4f, .4f, .4f, 0.5f);
		GL11.glClearDepth(1.0f);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); //Enable alpha transparency on textures
		GL11.glTexParameteri( GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri( GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST ); //Remove the blur when scaling up
		GL11.glPixelStorei( GL11.GL_UNPACK_LSB_FIRST, GL11.GL_FALSE ); 
		GL11.glPixelStorei( GL11.GL_UNPACK_ALIGNMENT, 1 );
	}
	
	public static void main(String... args) {
		new Example().start();
	}
}
