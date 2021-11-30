package renderengine.gui;

import renderengine.engine.Window;
import renderengine.mesh.TextureMesh2D;
import org.joml.Vector2f;

/**
 * @author Mirco Werner
 */
public class Frame extends Gui {
    public Frame(Window window, Vector2f upperLeftPosition, Vector2f extend, TextureMesh2D mesh2D) {
        super(window, upperLeftPosition, extend, mesh2D);
    }

    @Override
    public void onAction(Action action, float x, float y) {

    }
}
