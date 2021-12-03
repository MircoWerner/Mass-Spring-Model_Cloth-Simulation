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
 * @author Mirco Werner
 */
public class HangingPlaneScene implements IScene {
    private final MassSpringCloth massSpringCloth;

    public HangingPlaneScene(ThirdPersonCamera camera) throws Exception {
        int width = 40;
        int height = 40;
        Point[][] points = new Point[width][height];
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                points[w][h] = new Point(0.75f * w - width / 2f, 50, 0.85f * h - height / 2f, 0, 0, 0, 0);
            }
        }
        points[0][0].locked = 1;
        points[0][3 * height / 4].locked = 1;
        points[0][3 * height / 4].y = 52;
        points[width - 1][0].locked = 1;
        points[width - 1][0].y = 55;

        massSpringCloth = new MassSpringCloth(new MassSpringModel(width, height, points), -1, false, 1, 1.5f, new Vector3f(0), 500);

        camera.setCenter(-10, 35, -10);
        camera.setPhi((float) Math.PI / 8f);
        camera.setTheta((float) Math.PI / 4f);
        camera.setR(40);
    }

    @Override
    public void destruct() {
        massSpringCloth.cleanUp();
    }

    @Override
    public void simulate() {
        massSpringCloth.simulate();
    }

    @Override
    public void render(Window window, ACamera camera, Light light) {
        massSpringCloth.render(window, camera, light);
    }
}
