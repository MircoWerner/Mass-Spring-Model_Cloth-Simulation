package massspringcloth;

import renderengine.camera.ThirdPersonCamera;
import renderengine.engine.IRenderLogic;
import renderengine.engine.MouseInput;
import renderengine.engine.Window;
import renderengine.entities.Light;
import renderengine.gui.Gui;
import massspringcloth.simulation.SimulationController;
import renderengine.mesh.Texture;
import org.joml.Vector2f;
import org.joml.Vector3f;
import renderengine.renderer.TerrainRenderer;
import renderengine.mesh.Terrain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * @author Mirco Werner
 */
public class MassSpringClothRenderLogic implements IRenderLogic {
    private ThirdPersonCamera camera;
    private final Vector3f cameraInc;
    private static final float MOUSE_SENSITIVITY = 0.25f;
    private static final float CAMERA_POS_STEP = 3f;

    private Light light;

    private TerrainRenderer terrainRenderer;
    private final List<Terrain> terrains = new ArrayList<>();

    private UserInterface userInterface;
    private SimulationController simulationController;

    public MassSpringClothRenderLogic() {
        cameraInc = new Vector3f();
    }

    @Override
    public void init(Window window) throws Exception {
        terrainRenderer = new TerrainRenderer();
        camera = new ThirdPersonCamera();

        light = new Light(new Vector3f(50, 100, 50), new Vector3f(1f, 1f, 1f));

        {
            Texture texture = Texture.loadTexture(Objects.requireNonNull(MassSpringClothRenderLogic.class.getClassLoader().getResourceAsStream("textures/scifi_panel_basecolor.png")));
            Terrain terrain00 = new Terrain(0, 0, texture);
            Terrain terrain01 = new Terrain(0, -1, texture);
            Terrain terrain10 = new Terrain(-1, 0, texture);
            Terrain terrain11 = new Terrain(-1, -1, texture);

            terrain00.setScale(0.25f);
            terrain01.setScale(0.25f);
            terrain10.setScale(0.25f);
            terrain11.setScale(0.25f);

            terrains.add(terrain00);
            terrains.add(terrain01);
            terrains.add(terrain10);
            terrains.add(terrain11);
        }

        simulationController = new SimulationController(camera);
        userInterface = new UserInterface(window, simulationController);
    }

    @Override
    public void input(Window window, MouseInput mouseInput) {
        int factor = 1;
        if (window.isKeyPressed(GLFW_KEY_LEFT_CONTROL)) {
            factor = 5;
        }
        cameraInc.set(0, 0, 0);
        if (window.isKeyPressed(GLFW_KEY_W)) {
            cameraInc.z = -factor;
        } else if (window.isKeyPressed(GLFW_KEY_S)) {
            cameraInc.z = factor;
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            cameraInc.x = -factor;
        } else if (window.isKeyPressed(GLFW_KEY_D)) {
            cameraInc.x = factor;
        }
        if (window.isKeyPressed(GLFW_KEY_LEFT_SHIFT)) {
            cameraInc.y = -factor;
        } else if (window.isKeyPressed(GLFW_KEY_SPACE)) {
            cameraInc.y = factor;
        }

        float x = (float) (2 * mouseInput.getCurrentPos().x / (float) window.getWidth() - 1);
        float y = (float) -(2 * mouseInput.getCurrentPos().y / (float) window.getHeight() - 1);
        if (mouseInput.isOnLeftPressed()) {
            userInterface.onMousePressed(Gui.Action.PRESS, x, y);
            mouseInput.setOnLeftPressedHandled();
        } else if (mouseInput.isOnLeftReleased()) {
            userInterface.onMousePressed(Gui.Action.RELEASE, x, y);
            mouseInput.setOnLeftReleasedHandled();
        } else {
            userInterface.onMousePressed(Gui.Action.HOVER, x, y);
        }
    }

    @Override
    public void update(Window window, long timeSinceLastFrame, MouseInput mouseInput) {
        float increment = timeSinceLastFrame / 1000f;

        // Update camera position
        camera.moveCenter(cameraInc.x * CAMERA_POS_STEP * increment, cameraInc.y * CAMERA_POS_STEP * increment, cameraInc.z * CAMERA_POS_STEP * increment);

        // Update camera based on mouse
        if (mouseInput.isLeftButtonPressed()) {
            Vector2f rotVec = mouseInput.getMotionVec();
            camera.move(0.0f, rotVec.x * MOUSE_SENSITIVITY * increment, -rotVec.y * MOUSE_SENSITIVITY * increment);
        }
        if (mouseInput.getScrollVec().y != 0) {
            camera.move(-mouseInput.getScrollVec().y, 0.0f, 0.0f);
            mouseInput.resetScrollVec();
        }

        simulationController.simulate();
    }

    @Override
    public void render(Window window) {
        glDisable(GL_CULL_FACE);
        if (window.isKeyPressed(GLFW_KEY_T)) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        }
        simulationController.render(window, camera, light);
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glEnable(GL_CULL_FACE);

        terrainRenderer.render(window, camera, light, terrains);

        userInterface.render();
    }

    @Override
    public void cleanUp() {
        terrainRenderer.cleanUp();
        terrains.forEach(Terrain::cleanUp);
        simulationController.cleanUp();
        userInterface.cleanUp();
    }

    @Override
    public void onWindowResized(Window window) {
        userInterface.onWindowResized(window);
    }
}
