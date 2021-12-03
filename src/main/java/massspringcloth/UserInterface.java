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

    public UserInterface(Window window, SimulationController simulationController) throws Exception {
        this.simulationController = simulationController;

        texture2DRenderer = new Texture2DRenderer();

        init(window);
    }

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

    private void pause() {
        simulationController.setSimulationMode(ESimulationMode.NONE);
        playPauseButton.getMesh2D().setTexture(simulationController.getSimulationMode() == ESimulationMode.SIMULATION ? pauseTexture : playTexture);
    }

    private void setHangingWindEnabled(boolean visible) {
        sceneHangingWindButton.setEnabled(visible);
        sceneHangingWindButton.getMesh2D().setTexture(textureWind);
    }

    public void render() {
        texture2DRenderer.render(guis);
    }

    public void onWindowResized(Window window) {
        guis.forEach(gui -> gui.onWindowResized(window));
    }

    public void onMousePressed(Gui.Action action, float x, float y) {
        guis.forEach(gui -> gui.onAction(action, x, y));
    }

    public void cleanUp() {
        texture2DRenderer.cleanUp();
        guis.forEach(Gui::cleanUp);
    }
}
