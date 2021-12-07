package massspringcloth;

import renderengine.engine.Window;
import renderengine.gui.Button;
import renderengine.gui.Gui;
import massspringcloth.scenes.ESceneType;
import massspringcloth.simulation.ESimulationMode;
import massspringcloth.simulation.SimulationController;
import renderengine.mesh.Texture;
import renderengine.mesh.TextureMesh2D;
import org.joml.Vector2f;
import renderengine.renderer.Texture2DRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines the elements of the GUI and their behavior.
 * This class also provides methods to render the GUI.
 *
 * @author Mirco Werner
 */
public class UserInterface {
    private final SimulationController simulationController;

    private final Texture2DRenderer texture2DRenderer;
    private final List<Gui> guis = new ArrayList<>();

    private Button playPauseButton;
    private Button sceneHangingWindButton;

    private Texture playTexture;
    private Texture pauseTexture;
    private Texture textureWind;
    private Texture textureWindActive;

    /**
     * Creates and initializes the GUI.
     *
     * @param window               window of the application
     * @param simulationController the simulation controller to retrieve information about the currently loaded scene to display in the GUI
     * @throws Exception if texture loading fails
     */
    public UserInterface(Window window, SimulationController simulationController) throws Exception {
        this.simulationController = simulationController;

        texture2DRenderer = new Texture2DRenderer();

        init(window);
    }

    /**
     * Creates the buttons of the GUI and their behavior to switch scenes and start/pause the simulation.
     * When clicking on a scene button, the scene will be loaded or reloaded if already selected.
     *
     * @param window window of the application
     * @throws Exception if texture loading fails
     */
    public void init(Window window) throws Exception {
        playTexture = Texture.loadTexture("textures/gui/play.png");
        pauseTexture = Texture.loadTexture("textures/gui/pause.png");

        playPauseButton = new Button(window, new Vector2f(1, 1), new Vector2f(48, 48),
                new TextureMesh2D(new float[]{-1, 1, -1, -1, 1, 1, 1, -1},
                        playTexture)) {
            @Override
            public void onClick() {
                simulationController.switchSimulationMode();
                this.getMesh2D().setTexture(simulationController.getSimulationMode() == ESimulationMode.SIMULATION ? pauseTexture : playTexture);
            }
        };
        guis.add(playPauseButton);

        Button sceneHangingButton = new Button(window, new Vector2f(1, 50), new Vector2f(48, 48),
                new TextureMesh2D(new float[]{-1, 1, -1, -1, 1, 1, 1, -1},
                        Texture.loadTexture("textures/gui/scene_hanging.png"))) {
            @Override
            public void onClick() {
                pause();
                try {
                    simulationController.switchScene(ESceneType.HANGING);
                    setHangingWindEnabled(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        guis.add(sceneHangingButton);

        textureWind = Texture.loadTexture("textures/gui/wind.png");
        textureWindActive = Texture.loadTexture("textures/gui/wind_active.png");
        sceneHangingWindButton = new Button(window, new Vector2f(9, 99), new Vector2f(32, 32),
                new TextureMesh2D(new float[]{-1, 1, -1, -1, 1, 1, 1, -1},
                        textureWind)) {
            @Override
            public void onClick() {
                simulationController.toggleWindEnabled();
                if (simulationController.isWindEnabled()) {
                    this.getMesh2D().setTexture(textureWindActive);
                } else {
                    this.getMesh2D().setTexture(textureWind);
                }
            }
        };
        guis.add(sceneHangingWindButton);

        Button sceneHangingPlaneButton = new Button(window, new Vector2f(50, 50), new Vector2f(48, 48),
                new TextureMesh2D(new float[]{-1, 1, -1, -1, 1, 1, 1, -1},
                        Texture.loadTexture("textures/gui/scene_hanging_plane.png"))) {
            @Override
            public void onClick() {
                pause();
                try {
                    simulationController.switchScene(ESceneType.HANGING_PLANE);
                    setHangingWindEnabled(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        guis.add(sceneHangingPlaneButton);

        Button sceneSphereButton = new Button(window, new Vector2f(99, 50), new Vector2f(48, 48),
                new TextureMesh2D(new float[]{-1, 1, -1, -1, 1, 1, 1, -1},
                        Texture.loadTexture("textures/gui/scene_sphere.png"))) {
            @Override
            public void onClick() {
                pause();
                try {
                    simulationController.switchScene(ESceneType.SPHERE);
                    setHangingWindEnabled(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        guis.add(sceneSphereButton);

        Button sceneFlagButton = new Button(window, new Vector2f(148, 50), new Vector2f(48, 48),
                new TextureMesh2D(new float[]{-1, 1, -1, -1, 1, 1, 1, -1},
                        Texture.loadTexture("textures/gui/scene_flag.png"))) {
            @Override
            public void onClick() {
                pause();
                try {
                    simulationController.switchScene(ESceneType.FLAG);
                    setHangingWindEnabled(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        guis.add(sceneFlagButton);
    }

    /**
     * Pause the simulation.
     */
    private void pause() {
        simulationController.setSimulationMode(ESimulationMode.NONE);
        playPauseButton.getMesh2D().setTexture(simulationController.getSimulationMode() == ESimulationMode.SIMULATION ? pauseTexture : playTexture);
    }

    /**
     * Enables or disables the wind button for the first scene.
     *
     * @param visible true if the wind button should be enabled, false otherwise
     */
    private void setHangingWindEnabled(boolean visible) {
        sceneHangingWindButton.setEnabled(visible);
        sceneHangingWindButton.getMesh2D().setTexture(textureWind);
    }

    /**
     * Renders the GUI.
     * Depth testing will be disabled. The GUI should be rendered on top of everything else. Therefore, make sure to render the GUI at last.
     */
    public void render() {
        texture2DRenderer.render(guis);
    }

    /**
     * Call this method if the window is resized.
     * All GUI components will be resized to keep their defined size in pixels.
     *
     * @param window window of the application
     */
    public void onWindowResized(Window window) {
        guis.forEach(gui -> gui.onWindowResized(window));
    }

    /**
     * Call this method if the window is clicked (or hovered) with the mouse.
     * The method determines if a GUI element is hit and the defined method of the GUI element is executed.
     *
     * @param action mouse action
     * @param x pixel coordinate
     * @param y pixel coordinate
     */
    public void onMousePressed(Gui.Action action, float x, float y) {
        guis.forEach(gui -> gui.onAction(action, x, y));
    }

    /**
     * Clean up method for the renderer and the GUI components.
     */
    public void cleanUp() {
        texture2DRenderer.cleanUp();
        guis.forEach(Gui::cleanUp);
    }
}
