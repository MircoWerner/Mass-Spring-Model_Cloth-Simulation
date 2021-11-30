package renderengine.mesh;

/**
 * @author Mirco Werner
 */
public interface IMesh {
    void render();
    void cleanUp();

    Material getMaterial();
}
