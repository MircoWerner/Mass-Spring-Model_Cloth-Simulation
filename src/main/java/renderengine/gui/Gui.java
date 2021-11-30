package renderengine.gui;

import renderengine.engine.Window;
import renderengine.mesh.TextureMesh2D;
import org.joml.Vector2f;

/**
 * @author Mirco Werner
 */
public abstract class Gui {
    protected final Vector2f upperLeftPosition;
    protected final Vector2f extend;

    protected final Vector2f position = new Vector2f(0.0f, 0.0f);
    protected final Vector2f scale = new Vector2f(1.0f, 1.0f);
    private final TextureMesh2D mesh2D;

    protected boolean hover;
    protected boolean enabled = true;

    public Gui(Window window, Vector2f upperLeftPosition, Vector2f extend, TextureMesh2D mesh2D) {
        this.upperLeftPosition = upperLeftPosition;
        this.extend = extend;
        this.mesh2D = mesh2D;
        onWindowResized(window);
    }

    public void onWindowResized(Window window) {
        float x = 2 * (upperLeftPosition.x + extend.x / 2.0f) / (float) window.getWidth() - 1;
        float y = -(2 * (upperLeftPosition.y + extend.y / 2.0f) / (float) window.getHeight() - 1);
        float xScale = extend.x / (float) window.getWidth();
        float yScale = extend.y / (float) window.getHeight();
        position.set(x, y);
        scale.set(xScale, yScale);
    }

    public Vector2f getPosition() {
        return position;
    }

    public Vector2f getScale() {
        return scale;
    }

    public TextureMesh2D getMesh2D() {
        return mesh2D;
    }

    public void cleanUp() {
        mesh2D.cleanUp();
    }

    public abstract void onAction(Action action, float x, float y);

    public boolean isHover() {
        return hover;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public enum Action {
        PRESS,
        RELEASE,
        HOVER
    }
}
