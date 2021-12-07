package massspringcloth.scenes;

import massspringcloth.cloth.MassSpringCloth;
import massspringcloth.cloth.MassSpringModel;
import massspringcloth.cloth.Point;
import org.joml.Vector3f;
import renderengine.camera.ACamera;
import renderengine.camera.ThirdPersonCamera;
import renderengine.engine.Window;
import renderengine.entities.Entity;
import renderengine.entities.Light;
import renderengine.mesh.OBJLoader;
import renderengine.mesh.Texture;
import renderengine.mesh.TexturedModel;
import renderengine.renderer.ModelRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines a scene where cloth is falling on a sphere.
 *
 * @author Mirco Werner
 */
public class SphereScene implements IScene {
    private final MassSpringCloth massSpringCloth;

    private final ModelRenderer modelRenderer;
    private final Map<TexturedModel, List<Entity>> modeledEntities = new HashMap<>();

    /**
     * Creates the initial state of the cloth and the sphere entity.
     *
     * @param camera camera of the scene
     * @throws Exception if the scene creation fails
     */
    public SphereScene(ThirdPersonCamera camera) throws Exception {
        int width = 30;
        int height = 30;
        Point[][] points = new Point[width][height];
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                // create cloth in the xz-plane
                points[w][h] = new Point(w - width / 2f, 50, h - height / 2f, 0, 0, 0, 0);
            }
        }

        massSpringCloth = new MassSpringCloth(new MassSpringModel(width, height, points), -1, true, 1, 0.8f, new Vector3f(0), 100);

        modelRenderer = new ModelRenderer();

        TexturedModel sphere = new TexturedModel(OBJLoader.loadMesh("models/sphere.obj"),
                Texture.loadTexture("textures/white.png"));
        Entity entity = new Entity();
        entity.setPosition(0, 30f, 0);
        entity.setScale(19.5f);
        modeledEntities.put(sphere, new ArrayList<>() {{
            add(entity);
        }});

        camera.setCenter(0, 30, 0);
        camera.setPhi((float) Math.PI / 8f);
        camera.setTheta((float) Math.PI / 8f);
        camera.setR(40);
    }

    @Override
    public void destruct() {
        massSpringCloth.cleanUp();
        modelRenderer.cleanUp();
        modeledEntities.keySet().forEach(TexturedModel::cleanUp);
    }

    @Override
    public void simulate() {
        massSpringCloth.simulate(10);
    }

    @Override
    public void render(Window window, ACamera camera, Light light) {
        massSpringCloth.render(window, camera, light);
        modelRenderer.render(window, camera, light, modeledEntities);
    }
}
