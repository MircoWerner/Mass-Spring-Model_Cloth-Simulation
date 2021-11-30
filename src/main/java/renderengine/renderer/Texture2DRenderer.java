package renderengine.renderer;

import renderengine.entities.Entity;
import renderengine.gui.Gui;
import org.lwjgl.opengl.GL11;
import renderengine.shader.ShaderProgram;
import renderengine.utils.Transformation;

import java.util.List;

/**
 * @author Mirco Werner
 */
public class Texture2DRenderer extends ARenderer {
    public Texture2DRenderer() throws Exception {
        super();
    }

    @Override
    protected String getVertexShaderResource() {
        return "shaders/texture2d_vertex.glsl";
    }

    @Override
    protected String getFragmentShaderResource() {
        return "shaders/texture2d_fragment.glsl";
    }

    @Override
    protected void createShaderUniforms(ShaderProgram shaderProgram) throws Exception {
        shaderProgram.createUniform("transformationMatrix");
        shaderProgram.createUniform("flipped");
        shaderProgram.createUniform("texture_sampler");
        shaderProgram.createUniform("hover");
        shaderProgram.createUniform("enabled");
    }

    @Override
    protected void setAdditionalUniforms(ShaderProgram shaderProgram) {
        shaderProgram.setUniform("texture_sampler", 0);
    }

    @Override
    protected void setAdditionalUniformsForEachEntity(ShaderProgram shaderProgram, Entity entity) {

    }

    public void render(List<Gui> guis) {
        shaderProgram.bind();

        setAdditionalUniforms(shaderProgram);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        for (Gui gui : guis) {
            shaderProgram.setUniform("transformationMatrix", Transformation.getTransformationMatrix(gui));
            shaderProgram.setUniform("flipped", gui.getMesh2D().getTexture().isFlipped() ? 1 : 0);
            shaderProgram.setUniform("hover", gui.isHover() ? 1 : 0);
            shaderProgram.setUniform("enabled", gui.isEnabled() ? 1 : 0);
            gui.getMesh2D().render();
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);

        shaderProgram.unbind();
    }
}
