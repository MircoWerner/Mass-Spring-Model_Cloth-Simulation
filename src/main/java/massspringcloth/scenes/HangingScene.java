package massspringcloth.scenes;

import org.joml.Vector3f;
import renderengine.camera.ACamera;
import renderengine.camera.ThirdPersonCamera;
import renderengine.engine.Window;
import renderengine.entities.Light;
import massspringcloth.cloth.MassSpringCloth;
import massspringcloth.cloth.MassSpringModel;
import massspringcloth.cloth.Point;
import org.joml.SimplexNoise;

/**
 * @author Mirco Werner
 */
public class HangingScene implements IScene {
    private final MassSpringCloth massSpringCloth;
    private final Vector3f velocityFluid = new Vector3f(0f, 0f, 0f);
    private boolean windEnabled = false;
    private float counter = 0;

    public HangingScene(ThirdPersonCamera camera) throws Exception {
        int width = 20;
        int height = 20;
        Point[][] points = new Point[width][height];
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                points[w][h] = new Point(w - width / 2f, 50 + h - height / 2f, 0.1f * SimplexNoise.noise(w, h), 0, 0, 0, h == height - 1 && (w == 0 || w == width - 1) ? 1 : 0);
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
        if (windEnabled) {
            counter += 0.005f * Math.random();
            velocityFluid.z = 20 * Math.abs((float) Math.sin(counter));
        }
        massSpringCloth.simulate(10);
    }

    @Override
    public void render(Window window, ACamera camera, Light light) {
        massSpringCloth.render(window, camera, light);
    }

    public void toggleWindEnabled() {
        windEnabled = !windEnabled;
        velocityFluid.set(0);
        counter = 0;
    }

    public boolean isWindEnabled() {
        return windEnabled;
    }
}
