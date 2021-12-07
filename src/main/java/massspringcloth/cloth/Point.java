package massspringcloth.cloth;

/**
 * Stores information about a point.
 *
 * @author Mirco Werner
 */
public class Point {
    public float x;
    public float y;
    public float z;
    public float v_x;
    public float v_y;
    public float v_z;
    public float locked;

    /**
     * Creates a point.
     *
     * @param x coordinate
     * @param y coordinate
     * @param z coordinate
     * @param v_x x velocity
     * @param v_y y velocity
     * @param v_z z velocity
     * @param locked 0 if unlocked, any other number otherwise
     */
    public Point(float x, float y, float z, float v_x, float v_y, float v_z, float locked) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.v_x = v_x;
        this.v_y = v_y;
        this.v_z = v_z;
        this.locked = locked;
    }
}
