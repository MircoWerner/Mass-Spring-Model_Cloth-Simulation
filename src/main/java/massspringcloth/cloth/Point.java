package massspringcloth.cloth;

/**
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
