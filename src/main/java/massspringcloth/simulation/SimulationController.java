package massspringcloth.simulation;

import massspringcloth.scenes.ESceneType;
import massspringcloth.scenes.HangingScene;
import massspringcloth.scenes.IScene;
import renderengine.camera.ACamera;
import renderengine.camera.ThirdPersonCamera;
import renderengine.engine.Window;
import renderengine.entities.Light;

/**
 * This class stores information about the currently selected scene and provides methods to switch, simulate and render scenes.
 *
 * @author Mirco Werner
 */
public class SimulationController {
    private ESimulationMode simulationMode;
    private IScene scene;
    private ESceneType sceneType;

    private final ThirdPersonCamera camera;

    /**
     * Creates the simulation controller and loads the HANGING scene.
     *
     * @param camera camera of the renderer, used to set the camera position when a new scene is loaded
     * @throws Exception if the scene creation fails
     */
    public SimulationController(ThirdPersonCamera camera) throws Exception {
        this.camera = camera;
        scene = new HangingScene(camera);
        sceneType = ESceneType.HANGING;
    }

    /**
     * Loads a new scene.
     * If the given scene is already loaded, the scene will be reloaded and restarted.
     * The old is cleaned up.
     *
     * @param sceneType new scene
     * @throws Exception if the scene creation fails
     */
    public void switchScene(ESceneType sceneType) throws Exception {
        if (scene != null) {
            scene.destruct();
        }
        scene = ESceneType.createScene(sceneType, camera);
        this.sceneType = sceneType;
    }

    /**
     * Executes the simulation if the simulation is running.
     */
    public void simulate() {
        if (simulationMode == ESimulationMode.SIMULATION) {
            scene.simulate();
        }
    }

    /**
     * Renders the cloth.
     *
     * @param window window of the application
     * @param light  light in the scene
     */
    public void render(Window window, Light light) {
        scene.render(window, camera, light);
    }

    public ESimulationMode getSimulationMode() {
        return simulationMode;
    }

    public void setSimulationMode(ESimulationMode simulationMode) {
        this.simulationMode = simulationMode;
    }

    /**
     * Starts or pauses the simulation mode.
     */
    public void switchSimulationMode() {
        simulationMode = simulationMode == ESimulationMode.SIMULATION ? ESimulationMode.NONE : ESimulationMode.SIMULATION;
    }

    /**
     * The loaded scene will be cleaned up.
     */
    public void cleanUp() {
        if (scene != null) {
            scene.destruct();
        }
    }

    /**
     * Enables or disables wind in the HANGING scene.
     */
    public void toggleWindEnabled() {
        if (sceneType == ESceneType.HANGING) {
            ((HangingScene) scene).toggleWindEnabled();
        }
    }

    /**
     * Returns if wind is enabled in the HANGING scene.
     *
     * @return true if the wind is enabled AND the HANGING scene is loaded, false otherwise
     */
    public boolean isWindEnabled() {
        if (sceneType == ESceneType.HANGING) {
            return ((HangingScene) scene).isWindEnabled();
        }
        return false;
    }
}
