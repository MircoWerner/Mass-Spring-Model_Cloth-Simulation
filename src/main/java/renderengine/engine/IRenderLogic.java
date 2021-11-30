package renderengine.engine;

/**
 * @author Mirco Werner
 */
public interface IRenderLogic {
    void init(Window window) throws Exception;
    void input(Window window, MouseInput mouseInput);
    void update(Window window, long timeSinceLastFrame, MouseInput mouseInput);
    void render(Window window);
    void cleanUp();
    void onWindowResized(Window window);
}
