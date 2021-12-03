package massspringcloth.simulation;

import massspringcloth.scenes.ESceneType;
import massspringcloth.scenes.HangingScene;
import massspringcloth.scenes.IScene;
import renderengine.camera.ACamera;
import renderengine.camera.ThirdPersonCamera;
import renderengine.engine.Window;
import renderengine.entities.Light;

/**
 * @author Mirco Werner
 */
public class SimulationController {
    private ESimulationMode simulationMode;
    private IScene scene;
    private ESceneType sceneType;

    private final ThirdPersonCamera camera;

    public SimulationController(ThirdPersonCamera camera) throws Exception {
        this.camera = camera;
        scene = new HangingScene(camera);
        sceneType = ESceneType.HANGING;
    }

    public void switchScene(ESceneType sceneType) throws Exception {
        if (scene != null) {
            scene.destruct();
        }
        scene = ESceneType.createScene(sceneType, camera);
        this.sceneType = sceneType;
    }

    public void simulate() {
        if (simulationMode == ESimulationMode.SIMULATION) {
            scene.simulate();
        }
    }

    public void render(Window window, ACamera camera, Light light) {
        scene.render(window, camera, light);
    }

    public ESimulationMode getSimulationMode() {
        return simulationMode;
    }

    public void setSimulationMode(ESimulationMode simulationMode) {
        this.simulationMode = simulationMode;
    }

    public void switchSimulationMode() {
        simulationMode = simulationMode == ESimulationMode.SIMULATION ? ESimulationMode.NONE : ESimulationMode.SIMULATION;
    }

    public void cleanUp() {
        if (scene != null) {
            scene.destruct();
        }
    }

    public void toggleWindEnabled() {
        if (sceneType == ESceneType.HANGING) {
            ((HangingScene) scene).toggleWindEnabled();
        }
    }

    public boolean isWindEnabled() {
        if (sceneType == ESceneType.HANGING) {
            return ((HangingScene) scene).isWindEnabled();
        }
        return false;
    }
}
