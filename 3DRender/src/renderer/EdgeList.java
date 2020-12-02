package renderer;

/**
 * EdgeList should store the data for the edge list of a single polygon in your
 * scene. A few method stubs have been provided so that it can be tested, but
 * you'll need to fill in all the details.
 *
 * You'll probably want to add some setters as well as getters or, for example,
 * an addRow(y, xLeft, xRight, zLeft, zRight) method.
 */
public class EdgeList {
	private int startY,endY;
	private float[] leftX, rightX, leftZ, rightZ;

	public EdgeList(int startY, int endY) {
		this.startY = startY;
		this.endY = endY;
		int ty = endY - startY;

		leftX = new float[ty];
		rightX = new float[ty];
		leftZ = new float[ty];
		rightZ = new float[ty];

		for (int i = 0; i < ty; i++) {
			leftX[i] = Float.POSITIVE_INFINITY;
			rightX[i] = Float.NEGATIVE_INFINITY;
			leftZ[i] = Float.POSITIVE_INFINITY;
			rightZ[i] = Float.POSITIVE_INFINITY;
		}
	}

	/**
	 * Created and add row method to add the X and Z values needed
	 * for the edgelist.
	 *
	 * @param y
	 *		Needed for location
	 * @param x
	 * 		Left or right X
	 * @param z
	 * 		Left or right Z
	 */
	public void addRow(int y, float x, float z){
		if(x <= this.leftX[y]){
			this.leftX[y] = x;
			this.leftZ[y] = z;
		}

		if(x >= this.rightX[y]){
			this.rightX[y] = x;
			this.rightZ[y] = z;
		}
	}

	public int getStartY() {
		return startY;
	}

	public int getEndY() {
		return endY;
	}

	public float getLeftX(int y) {
		if(y == leftX.length){
			return leftX[y-1];
		}
		return leftX[y];
	}

	public float getRightX(int y) {
		if(y == rightX.length){
			return rightX[y-1] -1;
		}
		return rightX[y];
	}

	public float getLeftZ(int y) {
		if(y == leftZ.length){
			return leftZ[y-1] +1;
		}
		return leftZ[y];
	}

	public float getRightZ(int y) {
		if(y == rightZ.length){
			return rightZ[y-1];
		}
		return rightZ[y];
	}
}

// code for comp261 assignments
