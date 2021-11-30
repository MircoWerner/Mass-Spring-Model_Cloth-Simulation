package renderengine.engine;

import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * @author Mirco Werner
 */
public class RenderEngine {
    private final Window window;
    private final MouseInput mouseInput;
    private final Timer timer;

    private final IRenderLogic renderLogic;

    public RenderEngine(String windowTitle, int width, int height, IRenderLogic renderLogic) {
        window = new Window(windowTitle, width, height);
        this.renderLogic = renderLogic;
        mouseInput = new MouseInput();
        timer = new Timer();
    }

    public void run() {
        try {
            init();
            renderLoop();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cleanUp();
        }
    }

    protected void init() throws Exception {
        window.init(renderLogic);
        timer.init();
        mouseInput.init(window);
        renderLogic.init(window);
    }

    protected void renderLoop() {
        while (!window.windowShouldClose()) {
            input();
            update(timer.getElapsedTime());
            render();
        }
    }

    protected void cleanUp() {
        renderLogic.cleanUp();
        window.cleanUp();
        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }

    protected void input() {
        mouseInput.input();
        renderLogic.input(window, mouseInput);
    }

    protected void update(long interval) {
        renderLogic.update(window, interval, mouseInput);
    }

    protected void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        renderLogic.render(window);
        window.update();
    }
}
