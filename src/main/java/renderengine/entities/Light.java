package renderengine.entities;

import org.joml.Vector3f;

/**
 * @author Mirco Werner
 */
public class Light {
    private Vector3f position;
    private Vector3f color;
    private Vector3f attenuation = new Vector3f(1f, 0f, 0f);

    public Light(Vector3f position, Vector3f color) {
        this.position = position;
        this.color = color;
    }

    public Light(Vector3f position, Vector3f color, Vector3f attenuation) {
        this(position, color);
        this.attenuation = attenuation;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Vector3f getColor() {
        return color;
    }

    public void setColor(Vector3f color) {
        this.color = color;
    }

    public Vector3f getAttenuation() {
        return attenuation;
    }
}
