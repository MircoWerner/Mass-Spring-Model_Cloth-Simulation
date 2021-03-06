package massspringcloth.scenes;

import massspringcloth.cloth.MassSpringCloth;
import massspringcloth.cloth.MassSpringModel;
import massspringcloth.cloth.Point;
import org.joml.SimplexNoise;
import org.joml.Vector3f;
import renderengine.camera.ACamera;
import renderengine.camera.ThirdPersonCamera;
import renderengine.engine.Window;
import renderengine.entities.Light;

/**
 * Defines a scene where a flag (cloth) is affected by wind like a flag.
 *
 * @author Mirco Werner
 */
public class FlagScene implements IScene {
    private final MassSpringCloth massSpringCloth;
    private final Vector3f velocityFluid = new Vector3f(-70f, 0f, 0f); // wind
    private float counter = 0;

    /**
     * Creates the initial state of the cloth.
     *
     * @param camera camera of the scene
     * @throws Exception if the scene creation fails
     */
    public FlagScene(ThirdPersonCamera camera) throws Exception {
        int width = 30;
        int height = 20;
        Point[][] points = new Point[width][height];
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                // create cloth in xy-plane, lock at two points on the left side
                points[w][h] = new Point(w - width / 2f, 50 + h - height / 2f, 0.1f * SimplexNoise.noise(w, h), 0, 0, 0, (h == 0 || h == height - 1) && w == 0 ? 1 : 0);
            }
        }

        massSpringCloth = new MassSpringCloth(new MassSpringModel(width, height, points), 1, false, 1, 1.5f, velocityFluid, 500);

        camera.setCenter(0, 50, 0);
        camera.setPhi((float) Math.PI / 8f);
        camera.setTheta((float) Math.PI / 4f);
        camera.setR(25);
    }

    @Override
    public void destruct() {
        massSpringCloth.cleanUp();
    }

    @Override
    public void simulate() {
        counter += 0.005f;
        velocityFluid.z = 50 * Math.abs((float) Math.sin(counter)); // change wind in z direction by time
        massSpringCloth.simulate(2);
    }

    @Override
    public void render(Window window, ACamera camera, Light light) {
        massSpringCloth.render(window, camera, light);
    }
}
