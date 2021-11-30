package renderengine.mesh;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;

/**
 * @author Mirco Werner
 */
public class TexturedModel implements IMesh {
    private final Model model;
    private final Texture texture;

    public TexturedModel(Model model, Texture texture) {
        this.model = model;
        this.texture = texture;
    }

    public void prepareRender() {
        glActiveTexture(GL_TEXTURE0);
        texture.bind();

        glBindVertexArray(model.getVaoId());
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);

        if (texture.hasTransparency()) {
            glDisable(GL_CULL_FACE);
            glCullFace(GL_BACK);
        }
    }

    @Override
    public void render() {
        glDrawElements(GL_TRIANGLES, model.getVertexCount(), GL_UNSIGNED_INT, 0);
    }

    public void restoreRender() {
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glBindVertexArray(0);
        texture.unbind();

        if (texture.hasTransparency()) {
            glEnable(GL_CULL_FACE);
            glCullFace(GL_BACK);
        }
    }

    @Override
    public void cleanUp() {
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(model.getVboId());
        glDeleteBuffers(model.getTextureVboId());
        glDeleteBuffers(model.getNormalsVboId());
        glDeleteBuffers(model.getIndicesVboId());

        texture.cleanUp();

        glBindVertexArray(0);
        glDeleteVertexArrays(model.getVaoId());
    }

    public Texture getTexture() {
        return texture;
    }

    @Override
    public Material getMaterial() {
        return texture;
    }

}
