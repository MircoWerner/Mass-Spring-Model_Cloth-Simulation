package renderengine.mesh;

/**
 * @author Mirco Werner
 */
public class Material {
    private float phongExponent = 1;
    private float specularStrength = 0;

    public Material() {

    }

    public Material(float phongExponent, float specularStrength) {
        this.phongExponent = phongExponent;
        this.specularStrength = specularStrength;
    }

    public float getPhongExponent() {
        return phongExponent;
    }

    public void setPhongExponent(float phongExponent) {
        this.phongExponent = phongExponent;
    }

    public float getSpecularStrength() {
        return specularStrength;
    }

    public void setSpecularStrength(float specularStrength) {
        this.specularStrength = specularStrength;
    }
}
