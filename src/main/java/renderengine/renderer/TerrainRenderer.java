package renderengine.renderer;

import org.joml.Vector3f;
import renderengine.camera.ACamera;
import renderengine.engine.Window;
import renderengine.entities.Entity;
import renderengine.entities.Light;
import renderengine.shader.ShaderProgram;
import renderengine.mesh.Terrain;
import renderengine.utils.Transformation;

import java.util.List;

/**
 * @author Mirco Werner
 */
public class TerrainRenderer extends ARenderer {
    public TerrainRenderer() throws Exception {
        super();
    }

    @Override
    protected String getVertexShaderResource() {
        return "shaders/terrain_vertex.glsl";
    }

    @Override
    protected String getFragmentShaderResource() {
        return "shaders/terrain_fragment.glsl";
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
        shaderProgram.createUniform("scale");
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

    public void render(Window window, ACamera camera, Light light, List<Terrain> terrains) {
        shaderProgram.bind();

        shaderProgram.setUniform("viewMatrix", Transformation.getViewMatrix(camera));
        shaderProgram.setUniform("projectionMatrix", Transformation.getProjectionMatrix(FOV, window.getWidth(), window.getHeight(), Z_NEAR, Z_FAR));
        setAdditionalUniforms(shaderProgram);

        for (Terrain terrain : terrains) {
            shaderProgram.setUniform("phongExponent", terrain.getMaterial().getPhongExponent());
            shaderProgram.setUniform("specularStrength", terrain.getMaterial().getSpecularStrength());
            shaderProgram.setUniform("skyColor", new Vector3f(Window.RED, Window.GREEN, Window.BLUE));
            shaderProgram.setUniform("scale", terrain.getScale());
            shaderProgram.setUniform("transformationMatrix", Transformation.getTransformationMatrix(terrain));
            shaderProgram.setUniform("lightPosition", light.getPosition());
            shaderProgram.setUniform("lightColor", light.getColor());

            terrain.prepareRender();
            terrain.render();
            terrain.restoreRender();
        }

        shaderProgram.unbind();
    }
}
