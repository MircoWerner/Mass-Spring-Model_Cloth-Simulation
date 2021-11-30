package renderengine.utils;

import renderengine.camera.ACamera;
import renderengine.entities.Entity;
import renderengine.gui.Gui;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import renderengine.mesh.Terrain;

/**
 * @author Mirco Werner
 */
public final class Transformation {
    private Transformation() throws IllegalAccessException {
        throw new IllegalAccessException("Utility class constructor.");
    }

    public static Matrix4f getProjectionMatrix(float fov, float width, float height, float zNear, float zFar) {
        float aspectRatio = width / height;
        Matrix4f projectionMatrix = new Matrix4f();
        projectionMatrix.identity();
        projectionMatrix.perspective(fov, aspectRatio, zNear, zFar);
        return projectionMatrix;
    }

    public static Matrix4f getViewMatrix(ACamera camera) {
        Vector3f cameraPos = camera.getPosition();
        Vector3f rotation = camera.getRotation();

        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.identity();
        viewMatrix.rotate((float) Math.toRadians(rotation.x), new Vector3f(1, 0, 0))
                .rotate((float) Math.toRadians(rotation.y), new Vector3f(0, 1, 0))
                .rotate((float) Math.toRadians(rotation.z), new Vector3f(0, 0, 1));
        viewMatrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        return viewMatrix;
    }

    public static Matrix4f getTransformationMatrix(Entity entity) {
        Vector3f rotation = entity.getRotation();
        Matrix4f transformationMatrix = new Matrix4f();
        transformationMatrix.identity().translate(entity.getPosition()).
                rotateX((float) Math.toRadians(-rotation.x)).
                rotateY((float) Math.toRadians(-rotation.y)).
                rotateZ((float) Math.toRadians(-rotation.z)).
                scale(entity.getScale());
        return transformationMatrix;
    }

    public static Matrix4f getTransformationMatrix(Gui gui) {
        Matrix4f transformationMatrix = new Matrix4f();
        transformationMatrix.identity().translate(gui.getPosition().x, gui.getPosition().y, 0).
                scale(gui.getScale().x, gui.getScale().y, 0);
        return transformationMatrix;
    }

    public static Matrix4f getTransformationMatrix(Terrain terrain) {
        Matrix4f transformationMatrix = new Matrix4f();
        transformationMatrix.identity().translate(new Vector3f(terrain.getX(), 0, terrain.getZ()));
        return transformationMatrix;
    }
}
