package renderengine.engine;

import org.joml.Vector2d;
import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.*;

/**
 * @author Mirco Werner
 */
public class MouseInput {
    private final Vector2d previousPos = new Vector2d(-1, -1);
    private final Vector2d currentPos = new Vector2d(0, 0);

    private final Vector2f motionVec = new Vector2f();
    private final Vector2f scrollVec = new Vector2f();

    private boolean inWindow = false;

    private boolean leftButtonPressed = false;
    private boolean rightButtonPressed = false;

    private boolean leftCurrentlyReleased = true;
    private boolean leftCurrentlyPressed = false;
    private boolean onLeftReleased = false;
    private boolean onLeftPressed = false;

    public void init(Window window) {
        glfwSetCursorPosCallback(window.getWindowHandle(), (windowHandle, xPos, yPos) -> {
            currentPos.x = xPos;
            currentPos.y = yPos;
        });
        glfwSetCursorEnterCallback(window.getWindowHandle(), (windowHandle, entered) -> inWindow = entered);
        glfwSetMouseButtonCallback(window.getWindowHandle(), (windowHandle, button, action, mode) -> {
            leftButtonPressed = button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS;
            rightButtonPressed = button == GLFW_MOUSE_BUTTON_2 && action == GLFW_PRESS;

            onLeftPressed = leftCurrentlyReleased && button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS;
            leftCurrentlyReleased = button == GLFW_MOUSE_BUTTON_1 && action == GLFW_RELEASE;

            onLeftReleased = leftCurrentlyPressed && button == GLFW_MOUSE_BUTTON_1 && action == GLFW_RELEASE;
            leftCurrentlyPressed = button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS;
        });
        glfwSetScrollCallback(window.getWindowHandle(), (windowHandle, xOffset, yOffset) -> scrollVec.set(xOffset, yOffset));
    }

    public Vector2f getMotionVec() {
        return motionVec;
    }

    public Vector2f getScrollVec() {
        return scrollVec;
    }

    public void resetScrollVec() {
        scrollVec.zero();
    }

    public void input() {
        motionVec.x = 0;
        motionVec.y = 0;
        if (previousPos.x > 0 && previousPos.y > 0 && inWindow) {
            double deltaX = currentPos.x - previousPos.x;
            double deltaY = currentPos.y - previousPos.y;
            if (deltaX != 0) {
                motionVec.y = (float) deltaX;
            }
            if (deltaY != 0) {
                motionVec.x = (float) deltaY;
            }
        }
        previousPos.x = currentPos.x;
        previousPos.y = currentPos.y;
    }

    public boolean isLeftButtonPressed() {
        return leftButtonPressed;
    }

    public boolean isRightButtonPressed() {
        return rightButtonPressed;
    }

    public Vector2d getCurrentPos() {
        return currentPos;
    }

    public boolean isOnLeftPressed() {
        return onLeftPressed;
    }

    public void setOnLeftPressedHandled() {
        onLeftPressed = false;
    }

    public boolean isOnLeftReleased() {
        return onLeftReleased;
    }

    public void setOnLeftReleasedHandled() {
        onLeftReleased = false;
    }
}