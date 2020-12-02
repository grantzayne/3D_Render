package renderer;

import java.awt.Color;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import renderer.Scene.Polygon;

import static java.lang.Math.round;

/**
 * The Pipeline class has method stubs for all the major components of the
 * rendering pipeline, for you to fill in.
 * 
 * Some of these methods can get quite long, in which case you should strongly
 * consider moving them out into their own file. You'll need to update the
 * imports in the test suite if you do.
 */
public class Pipeline {
	/**
	 * Returns true if the given polygon is facing away from the camera (and so
	 * should be hidden), and false otherwise.
	 */
	public static boolean isHidden(Polygon poly) {
		Vector3D normal = (poly.getVertices()[1].minus(poly.getVertices()[0])).crossProduct(poly.getVertices()[2].minus(poly.getVertices()[1]));
		if(normal.z >= 0){
			return true;
		}
		return false;
	}

	/**
	 * Computes the colour of a polygon on the screen, once the lights, their
	 * angles relative to the polygon's face, and the reflectance of the polygon
	 * have been accounted for.
	 * 
	 * @param lightDirection
	 *            The Vector3D pointing to the directional light read in from
	 *            the file.
	 * @param lightColor
	 *            The color of that directional light.
	 * @param ambientLight
	 *            The ambient light in the scene, i.e. light that doesn't depend
	 *            on the direction.
	 */
	public static Color getShading(Polygon poly, Vector3D lightDirection, Color lightColor, Color ambientLight) {
		Vector3D normal = (poly.getVertices()[1].minus(poly.getVertices()[0])).crossProduct(poly.getVertices()[2].minus(poly.getVertices()[1]));
		float cosO = normal.cosTheta(lightDirection);

		if(lightDirection.z > 0){
			 cosO = 0;
		}

		float r = (ambientLight.getRed() * poly.getReflectance().getRed() + lightColor.getRed() * poly.getReflectance().getRed() * cosO);
		float g = (ambientLight.getGreen() * poly.getReflectance().getGreen() + lightColor.getGreen() * poly.getReflectance().getGreen() * cosO);
		float b = (ambientLight.getBlue() * poly.getReflectance().getBlue() + lightColor.getBlue() * poly.getReflectance().getBlue() * cosO);

		return new Color((int)r/255,(int)g/255,(int)b/255);
	}

	/**
	 * This method should rotate the polygons and light such that the viewer is
	 * looking down the Z-axis. The idea is that it returns an entirely new
	 * Scene object, filled with new Polygons, that have been rotated.
	 * 
	 * @param scene
	 *            The original Scene.
	 * @param xRot
	 *            An angle describing the viewer's rotation in the YZ-plane (i.e
	 *            around the X-axis).
	 * @param yRot
	 *            An angle describing the viewer's rotation in the XZ-plane (i.e
	 *            around the Y-axis).
	 * @return A new Scene where all the polygons and the light source have been
	 *         rotated accordingly.
	 */
	public static Scene rotateScene(Scene scene, float xRot, float yRot) {
		Transform rotationM = Transform.newXRotation(xRot).compose(Transform.newYRotation(yRot));
		return transform(scene,rotationM);
	}

	/**
	 * This should translate the scene by the appropriate amount.
	 * 
	 * @param scene
	 * @return
	 */
	public static Scene translateScene(Scene scene, float tx, float ty, float tz) {
		Transform translateM = Transform.newTranslation(tx, ty, tz);
		return transform(scene,translateM);
	}

	/**
	 * This should scale the scene.
	 * 
	 * @param scene
	 * @return
	 */
	public static Scene scaleScene(Scene scene, float tx, float ty, float tz) {
		Transform scaleM = Transform.newScale(tx,ty,tz);
		return transform(scene,scaleM);
	}

	/**
	 * Created this method to do the bulk of transformations for rotating
	 * translation and scale. Should do the composite transformation matrix
	 * Right to Left.
	 *
	 * @param scene
	 * @param matrix
	 * @return
	 */
	public static Scene transform(Scene scene, Transform matrix){
		Vector3D lightpos = matrix.multiply(scene.getLight());

		List<Polygon> transformedPolys = new ArrayList<>();
		for (Polygon p : scene.getPolygons()){
			Vector3D[] transformedV = new Vector3D[3];
			for(int i = 0; i < transformedV.length; i++){
				transformedV[i] = matrix.multiply(p.getVertices()[i]);
			}
			Polygon transformedPoly = new Polygon(transformedV[0],transformedV[1],transformedV[2],p.getReflectance());
			transformedPolys.add(transformedPoly);
		}
		return new Scene(transformedPolys,lightpos);
	}

	/**
	 * Computes the edgelist of a single provided polygon, as per the lecture
	 * slides.
	 */
	public static EdgeList computeEdgeList(Polygon poly) {
		Vector3D v1 = poly.getVertices()[0];
		Vector3D v2 = poly.getVertices()[1];
		Vector3D v3 = poly.getVertices()[2];
		int startY = (int) Math.min(Math.min(v1.y,v2.y),v3.y);
		int endY = (int) Math.max(Math.max(v1.y,v2.y),v3.y);

		EdgeList e = new EdgeList(startY,endY);

		for(int i = 0; i < poly.getVertices().length; i++){
			int j = i + 1;
			j = j == 3 ? 0 : j;
			Vector3D up;
			Vector3D down;

			if(poly.getVertices()[i].y == poly.getVertices()[j].y){
				continue;
			} else if(poly.getVertices()[i].y < poly.getVertices()[j].y){
				up = poly.getVertices()[i];
				down = poly.getVertices()[j];
			} else{
				up = poly.getVertices()[j];
				down = poly.getVertices()[i];
			}

			float mx = (down.x - up.x)/(down.y - up.y);
			float mz = (down.z - up.z)/(down.y - up.y);
			float x = up.x;
			float z = up.z;
			int starty = (int)up.y;
			int endy = (int) down.y;

			for( ; starty < endy; starty++, x += mx, z += mz){
				e.addRow(starty - startY,x,z);
			}
		}
		return e;
	}

	/**
	 * Fills a zbuffer with the contents of a single edge list according to the
	 * lecture slides.
	 * 
	 * The idea here is to make zbuffer and zdepth arrays in your main loop, and
	 * pass them into the method to be modified.
	 * 
	 * @param zbuffer
	 *            A double array of colours representing the Color at each pixel
	 *            so far.
	 * @param zdepth
	 *            A double array of floats storing the z-value of each pixel
	 *            that has been coloured in so far.
	 * @param polyEdgeList
	 *            The edgelist of the polygon to add into the zbuffer.
	 * @param polyColor
	 *            The colour of the polygon to add into the zbuffer.
	 */
	public static void computeZBuffer(Color[][] zbuffer, float[][] zdepth, EdgeList polyEdgeList, Color polyColor) {
		int startY = polyEdgeList.getStartY();
		int endY = polyEdgeList.getEndY();
		int height = endY - startY;

		int y = 0;
		while (y < height) {

			// do not render pixels that are out-of-boundary
			if (y + startY < 0 || y + startY >= zbuffer[0].length) {
				y++;
				continue;
			}

			int x = (int) polyEdgeList.getLeftX(y);
			int rightX = (int) polyEdgeList.getRightX(y);
			float z = polyEdgeList.getLeftZ(y);
			float rightZ = polyEdgeList.getRightZ(y);
			float mz = (rightZ - z) / (rightX - x);

			while (x < rightX) {
				// do not render pixels that are out-of-boundary
				if (x < 0 || x >= zbuffer.length) {
					z += mz;
					x++;
					continue;
				}

				if (z < zdepth[x][y + startY]) {
					zdepth[x][y + startY] = z;
					zbuffer[x][y + startY] = polyColor;
				}
				z += mz;
				x++;
			}
			y++;
		}
	}
}

// code for comp261 assignments
