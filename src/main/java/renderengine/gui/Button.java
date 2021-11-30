package renderengine.gui;

import renderengine.engine.Window;
import renderengine.mesh.TextureMesh2D;
import org.joml.Vector2f;

/**
 * @author Mirco Werner
 */
public abstract class Button extends Gui {
    public Button(Window window, Vector2f upperLeftPosition, Vector2f extend, TextureMesh2D mesh2D) {
        super(window, upperLeftPosition, extend, mesh2D);
    }

    @Override
    public void onAction(Action action, float x, float y) {
        if (!enabled) {
            return;
        }
        if (x >= position.x - scale.x && x <= position.x + scale.x &&
                y >= position.y - scale.y && y <= position.y + scale.y) {
            if (action == Action.PRESS) {
                onClick();
            }
            hover = true;
        } else {
            hover = false;
        }
    }

    public abstract void onClick();
}
