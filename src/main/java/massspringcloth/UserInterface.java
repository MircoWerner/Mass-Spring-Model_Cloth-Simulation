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

    private Texture playTexture;
    private Texture pauseTexture;

    public UserInterface(Window window, SimulationController simulationController) throws Exception {
        this.simulationController = simulationController;

        texture2DRenderer = new Texture2DRenderer();

        init(window);
    }

    public void init(Window window) throws Exception {
        playTexture = Texture.loadTexture("textures/gui/play.png");
        pauseTexture = Texture.loadTexture("textures/gui/pause.png");

        playPauseButton = new Button(window, new Vector2f(1, 1), new Vector2f(32, 32),
                new TextureMesh2D(new float[]{-1, 1, -1, -1, 1, 1, 1, -1},
                        playTexture)) {
            @Override
            public void onClick() {
                simulationController.switchSimulationMode();
                this.getMesh2D().setTexture(simulationController.getSimulationMode() == ESimulationMode.SIMULATION ? pauseTexture : playTexture);
            }
        };
        guis.add(playPauseButton);

        Button sceneHangingButton = new Button(window, new Vector2f(1, 34), new Vector2f(32, 32),
                new TextureMesh2D(new float[]{-1, 1, -1, -1, 1, 1, 1, -1},
                        Texture.loadTexture("textures/gui/scene_hanging.png"))) {
            @Override
            public void onClick() {
                pause();
                try {
                    simulationController.switchScene(ESceneType.HANGING);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        guis.add(sceneHangingButton);

        Button sceneSphereButton = new Button(window, new Vector2f(35, 34), new Vector2f(32, 32),
                new TextureMesh2D(new float[]{-1, 1, -1, -1, 1, 1, 1, -1},
                        Texture.loadTexture("textures/gui/scene_sphere.png"))) {
            @Override
            public void onClick() {
                pause();
                try {
                    simulationController.switchScene(ESceneType.SPHERE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        guis.add(sceneSphereButton);

        Button sceneFlagButton = new Button(window, new Vector2f(69, 34), new Vector2f(32, 32),
                new TextureMesh2D(new float[]{-1, 1, -1, -1, 1, 1, 1, -1},
                        Texture.loadTexture("textures/gui/scene_flag.png"))) {
            @Override
            public void onClick() {
                pause();
                try {
                    simulationController.switchScene(ESceneType.FLAG);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        guis.add(sceneFlagButton);
        sceneFlagButton.setEnabled(false);
    }

    private void pause() {
        simulationController.setSimulationMode(ESimulationMode.NONE);
        playPauseButton.getMesh2D().setTexture(simulationController.getSimulationMode() == ESimulationMode.SIMULATION ? pauseTexture : playTexture);
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
