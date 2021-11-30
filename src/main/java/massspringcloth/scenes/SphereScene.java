package massspringcloth.scenes;

import renderengine.camera.ACamera;
import renderengine.camera.ThirdPersonCamera;
import renderengine.engine.Window;
import renderengine.entities.Entity;
import renderengine.entities.Light;
import massspringcloth.cloth.MassSpringCloth;
import massspringcloth.cloth.MassSpringModel;
import massspringcloth.cloth.Point;
import renderengine.mesh.OBJLoader;
import renderengine.mesh.Texture;
import renderengine.mesh.TexturedModel;
import org.joml.SimplexNoise;
import renderengine.renderer.ModelRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mirco Werner
 */
public class SphereScene implements IScene {
    private final MassSpringCloth massSpringCloth;

    private final ModelRenderer modelRenderer;
    private final Map<TexturedModel, List<Entity>> modeledEntities = new HashMap<>();

    public SphereScene(ThirdPersonCamera camera) throws Exception {
        int width = 30;
        int height = 30;
        Point[][] points = new Point[width][height];
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                points[w][h] = new Point(w - width / 2f, 50 + 0.1f * SimplexNoise.noise(w, h), h - height / 2f, 0, -25, 0, 0);
            }
        }

        massSpringCloth = new MassSpringCloth(new MassSpringModel(width, height, points), -1, true, 1, 0.8f, 500);

        modelRenderer = new ModelRenderer();

        TexturedModel sphere = new TexturedModel(OBJLoader.loadMesh("models/sphere.obj"),
                Texture.loadTexture("textures/white.png"));
        Entity entity = new Entity();
        entity.setPosition(0, 29.9f, 0);
        entity.setScale(20);
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
        massSpringCloth.simulate();
    }

    @Override
    public void render(Window window, ACamera camera, Light light) {
        massSpringCloth.render(window, camera, light);
        modelRenderer.render(window, camera, light, modeledEntities);
    }
}
