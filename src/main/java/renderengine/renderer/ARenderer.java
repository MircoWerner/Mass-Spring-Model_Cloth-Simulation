package renderengine.renderer;

import renderengine.camera.ACamera;
import renderengine.entities.Entity;
import renderengine.engine.Window;
import org.joml.Matrix4f;
import renderengine.shader.ShaderProgram;
import renderengine.utils.Transformation;

import java.util.List;

/**
 * @author Mirco Werner
 */
public abstract class ARenderer {
    protected static final float FOV = (float) Math.toRadians(60.0f);
    protected static final float Z_NEAR = 0.01f;
    protected static final float Z_FAR = 1000.0f;

    protected final ShaderProgram shaderProgram;

    public ARenderer() throws Exception {
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(getVertexShaderResource());
        shaderProgram.createFragmentShader(getFragmentShaderResource());
        shaderProgram.link();

        createShaderUniforms(shaderProgram);
    }

    protected abstract String getVertexShaderResource();

    protected abstract String getFragmentShaderResource();

    protected abstract void createShaderUniforms(ShaderProgram shaderProgram) throws Exception;

    public void render(Window window, ACamera camera, List<Entity> entities) {
        shaderProgram.bind();

        Matrix4f viewMatrix = Transformation.getViewMatrix(camera);
        shaderProgram.setUniform("viewMatrix", viewMatrix);
        Matrix4f projectionMatrix = Transformation.getProjectionMatrix(FOV, window.getWidth(), window.getHeight(), Z_NEAR, Z_FAR);
        shaderProgram.setUniform("projectionMatrix", projectionMatrix);
        setAdditionalUniforms(shaderProgram);

        for (Entity entity : entities) {
            Matrix4f transformationMatrix = Transformation.getTransformationMatrix(entity);
            shaderProgram.setUniform("transformationMatrix", transformationMatrix);
            setAdditionalUniformsForEachEntity(shaderProgram, entity);
            entity.getMesh().render();
        }

        shaderProgram.unbind();
    }

    protected abstract void setAdditionalUniforms(ShaderProgram shaderProgram);

    protected abstract void setAdditionalUniformsForEachEntity(ShaderProgram shaderProgram, Entity entity);

    public void cleanUp() {
        shaderProgram.cleanUp();
    }
}
