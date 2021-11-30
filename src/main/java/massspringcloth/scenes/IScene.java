package massspringcloth.scenes;

import renderengine.camera.ACamera;
import renderengine.engine.Window;
import renderengine.entities.Light;

/**
 * @author Mirco Werner
 */
public interface IScene {
    void destruct();
    void simulate();
    void render(Window window, ACamera camera, Light light);
}
