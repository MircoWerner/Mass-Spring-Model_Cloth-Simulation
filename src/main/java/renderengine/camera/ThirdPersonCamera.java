package renderengine.camera;

import org.joml.Vector3f;

/**
 * @author Mirco Werner
 */
public class ThirdPersonCamera extends ACamera {
    private static final float PI_HALF = (float) (Math.PI / 2.0f);
    private static final float TWO_PI = (float) (2.0f * Math.PI);

    private float r = 15.0f;
    private float phi = 0.0f;
    private float theta = 0.0f;

    private final Vector3f center = new Vector3f(0.0f, 5.0f, 0.0f);

    public ThirdPersonCamera() {
        super();
        updateXYZ();
    }

    public void moveCenter(float offsetX, float offsetY, float offsetZ) {
        if (offsetZ != 0) {
            center.x += (float) Math.sin(Math.toRadians(rotation.y)) * -1.0f * offsetZ;
            center.z += (float) Math.cos(Math.toRadians(rotation.y)) * offsetZ;
        }
        if (offsetX != 0) {
            center.x += (float) Math.sin(Math.toRadians(rotation.y - 90)) * -1.0f * offsetX;
            center.z += (float) Math.cos(Math.toRadians(rotation.y - 90)) * offsetX;
        }
        center.y += offsetY;
        updateXYZ();
    }

    public void move(float offsetR, float offsetPhi, float offsetTheta) {
        r = Math.max(0.0f, r + offsetR);
        phi = Math.min(PI_HALF, Math.max(-PI_HALF, phi + offsetPhi));
        theta = (theta + offsetTheta + TWO_PI) % TWO_PI;
        updateXYZ();
    }

    public void updateXYZ() {
        setPosition((float) (r * Math.cos(phi) * Math.sin(theta)) + center.x,
                (float) (r * Math.sin(phi)) + center.y,
                (float) (r * Math.cos(phi) * Math.cos(theta)) + center.z);
        setRotation((float) Math.toDegrees(phi), -(float) Math.toDegrees(theta), 0);
    }

    public void setCenter(float x, float y, float z) {
        this.center.set(x, y, z);
    }

    public float getR() {
        return r;
    }

    public void setR(float r) {
        this.r = r;
    }

    public float getPhi() {
        return phi;
    }

    public void setPhi(float phi) {
        this.phi = phi;
    }

    public float getTheta() {
        return theta;
    }

    public void setTheta(float theta) {
        this.theta = theta;
    }
}
