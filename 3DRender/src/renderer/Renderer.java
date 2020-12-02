package renderer;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Renderer extends GUI {

	//FIELDS
	private Vector3D lightSource;
	private Scene scene;
	private float scale = 1.0f;
	private Vector3D translation = new Vector3D(0f,0f,0f);
	private ArrayList<Scene.Polygon> polygons = new ArrayList<>();

	/**
	 * BufferedReader reads through a file and gets the rgb colors first then the x y z vectors after.
	 * Checks if it is the last line and gets the lightsource values then breaks out of for loop
	 *
	 * @param file chosen to read
	 */
	@Override
	protected void onLoad(File file) {
		try {
			String line;
			BufferedReader reader = new BufferedReader(new FileReader(file));
			reader.readLine(); //Skip first line

			while ((line = reader.readLine()) != null) {
				reader.mark(100);
				if(reader.readLine() == null){
					//Sets lightsource vector from last line
					String[] lightDirectionValues = line.split(",");
					lightSource = new Vector3D(Float.parseFloat(lightDirectionValues[0]), Float.parseFloat(lightDirectionValues[1]), Float.parseFloat(lightDirectionValues[2]));
					//System.out.println("Light Source: " + Float.parseFloat(lightDirectionValues[0])+ " " + Float.parseFloat(lightDirectionValues[1])+ " " + Float.parseFloat(lightDirectionValues[2]));
					break;
				} else {
					reader.reset();
				}

				String[] values = line.split(","); //Split values of line into array

				Color c = new Color(Integer.parseInt(values[0]),Integer.parseInt(values[1]),Integer.parseInt(values[2]));
				//System.out.println(Float.parseFloat(values[0]) + " " + Float.parseFloat(values[1]) + " " +  Float.parseFloat(values[2])); //Prints r g b

				Vector3D v1 = new Vector3D(Float.parseFloat(values[3]), Float.parseFloat(values[4]), Float.parseFloat(values[5]));
				//System.out.println(Float.parseFloat(values[3]) + " " + Float.parseFloat(values[4]) + " " +  Float.parseFloat(values[5])); //Prints v1 values

				Vector3D v2 = new Vector3D(Float.parseFloat(values[6]), Float.parseFloat(values[7]), Float.parseFloat(values[8]));
				//System.out.println(Float.parseFloat(values[6]) + " " + Float.parseFloat(values[7]) + " " +  Float.parseFloat(values[8])); //Prints v2 values

				Vector3D v3 = new Vector3D(Float.parseFloat(values[9]), Float.parseFloat(values[10]), Float.parseFloat(values[11]));
				//System.out.println(Float.parseFloat(values[9]) + " " + Float.parseFloat(values[10]) + " " +  Float.parseFloat(values[11])); //Prints v3 values

				polygons.add(new Scene.Polygon(v1,v2,v3,c));
				//System.out.println(polygons);
			}

			reader.close();

			scene = new Scene(polygons,lightSource);
		} catch (FileNotFoundException exception) {
			System.out.println("File not found");
		} catch (IOException exception) {
			System.out.println(exception);
		}
	}

	@Override
	protected void onKeyPress(KeyEvent ev) {
		// WASD and arrow keys can be used interchangeably
		if(ev.getKeyCode() == KeyEvent.VK_A){
			scene = Pipeline.rotateScene(scene, 0,(float) (-0.1*Math.PI));

		}else if(ev.getKeyCode() == KeyEvent.VK_D){
			scene = Pipeline.rotateScene(scene, 0,(float) (0.1*Math.PI));

		}else if(ev.getKeyCode() == KeyEvent.VK_W){
			scene = Pipeline.rotateScene(scene, (float) (0.1*Math.PI), 0);

		}else if(ev.getKeyCode() == KeyEvent.VK_S){
			scene = Pipeline.rotateScene(scene, (float) (-0.1*Math.PI), 0);
		}

		/*
		 * This method should be used to rotate the user's viewpoint.
		 */
	}

	@Override
	protected BufferedImage render() {
		if(scene == null){
			return null;
		}

		scene = Pipeline.scaleScene(scene,scale,scale,scale);
		scene = Pipeline.translateScene(scene,translation.x,translation.y,translation.z);
		Color[][] zbuffer = new Color[CANVAS_WIDTH][CANVAS_HEIGHT];
		float[][] zdepth = new float[CANVAS_WIDTH][CANVAS_HEIGHT];

		EdgeList edges;

		for (int x = 0; x < CANVAS_WIDTH; x++) {
			for (int y = 0; y < CANVAS_HEIGHT; y++) {
				zbuffer[x][y] = Color.black;
				zdepth[x][y] = Float.POSITIVE_INFINITY;
			}
		}

		for(Scene.Polygon p : polygons){
			if(!Pipeline.isHidden(p)){
				Color c = Pipeline.getShading(p,scene.getLight(),new Color(100,100,100),new Color(getAmbientLight()[0],getAmbientLight()[1],getAmbientLight()[2]));
				edges = Pipeline.computeEdgeList(p);
				Pipeline.computeZBuffer(zbuffer,zdepth,edges,c);
				//System.out.println(p);
			}
		}
		/*
		 * This method should put together the pieces of your renderer, as
		 * described in the lecture. This will involve calling each of the
		 * static method stubs in the Pipeline class, which you also need to
		 * fill in.
		 */
		return convertBitmapToImage(zbuffer);
	}

	/**
	 * Converts a 2D array of Colors to a BufferedImage. Assumes that bitmap is
	 * indexed by column then row and has imageHeight rows and imageWidth
	 * columns. Note that image.setRGB requires x (col) and y (row) are given in
	 * that order.
	 */
	private BufferedImage convertBitmapToImage(Color[][] bitmap) {
		BufferedImage image = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < CANVAS_WIDTH; x++) {
			for (int y = 0; y < CANVAS_HEIGHT; y++) {
				image.setRGB(x, y, bitmap[x][y].getRGB());
			}
		}
		return image;
	}

	public static void main(String[] args) {
		new Renderer();
	}
}

// code for comp261 assignments
