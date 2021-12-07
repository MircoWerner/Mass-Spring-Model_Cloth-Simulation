package massspringcloth.scenes;

import renderengine.camera.ACamera;
import renderengine.engine.Window;
import renderengine.entities.Light;

/**
 * Interface for simulation, rendering and destruction of a scene. Should be implemented for every scene.
 *
 * @author Mirco Werner
 */
public interface IScene {
    /**
     * Destructs the scene. Frees all resources.
     */
    void destruct();

    /**
     * Executes a simulation of the scene, i.e. execution of the compute shader multiple times.
     */
    void simulate();

    /**
     * Renders the cloth (and probably other entities defined in the scene).
     *
     * @param window window of the application
     * @param camera camera of the scene
     * @param light  light in the scene
     */
    void render(Window window, ACamera camera, Light light);
}
