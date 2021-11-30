package renderengine.renderer;

import org.joml.Vector3f;
import renderengine.camera.ACamera;
import renderengine.engine.Window;
import renderengine.entities.Entity;
import renderengine.entities.Light;
import renderengine.mesh.TexturedModel;
import renderengine.shader.ShaderProgram;
import renderengine.utils.Transformation;

import java.util.List;
import java.util.Map;

/**
 * @author Mirco Werner
 */
public class ModelRenderer extends ARenderer {
    public ModelRenderer() throws Exception {
        super();
    }

    @Override
    protected String getVertexShaderResource() {
        return "shaders/model_vertex.glsl";
    }

    @Override
    protected String getFragmentShaderResource() {
        return "shaders/model_fragment.glsl";
    }

    @Override
    protected void createShaderUniforms(ShaderProgram shaderProgram) throws Exception {
        shaderProgram.createUniform("transformationMatrix");
        shaderProgram.createUniform("projectionMatrix");
        shaderProgram.createUniform("viewMatrix");
        shaderProgram.createUniform("texture_sampler");
        shaderProgram.createUniform("phongExponent");
        shaderProgram.createUniform("specularStrength");
        shaderProgram.createUniform("skyColor");
        shaderProgram.createUniform("useFakeLighting");
        shaderProgram.createUniform("lightPosition");
        shaderProgram.createUniform("lightColor");
    }

    @Override
    protected void setAdditionalUniforms(ShaderProgram shaderProgram) {
        shaderProgram.setUniform("texture_sampler", 0);
    }

    @Override
    protected void setAdditionalUniformsForEachEntity(ShaderProgram shaderProgram, Entity entity) {

    }

    public void render(Window window, ACamera camera, Light light, Map<TexturedModel, List<Entity>> entities) {
        shaderProgram.bind();

        shaderProgram.setUniform("viewMatrix", Transformation.getViewMatrix(camera));
        shaderProgram.setUniform("projectionMatrix", Transformation.getProjectionMatrix(FOV, window.getWidth(), window.getHeight(), Z_NEAR, Z_FAR));
        setAdditionalUniforms(shaderProgram);

        for (TexturedModel texturedModel : entities.keySet()) {
            shaderProgram.setUniform("phongExponent", texturedModel.getMaterial().getPhongExponent());
            shaderProgram.setUniform("specularStrength", texturedModel.getMaterial().getSpecularStrength());
            shaderProgram.setUniform("skyColor", new Vector3f(Window.RED, Window.GREEN, Window.BLUE));
            shaderProgram.setUniform("useFakeLighting", texturedModel.getTexture().useFakeLighting() ? 1 : 0);
            shaderProgram.setUniform("lightPosition", light.getPosition());
            shaderProgram.setUniform("lightColor", light.getColor());

            texturedModel.prepareRender();
            for (Entity entity : entities.get(texturedModel)) {
                shaderProgram.setUniform("transformationMatrix", Transformation.getTransformationMatrix(entity));
                texturedModel.render();
            }
            texturedModel.restoreRender();
        }

        shaderProgram.unbind();
    }
}
