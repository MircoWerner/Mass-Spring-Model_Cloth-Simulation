package renderengine.mesh;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * @author Mirco Werner
 */
public class TextureMesh2D implements IMesh {
    private final int vertexCount;

    private final int vaoId;
    private final int vboId;

    private Texture texture;

    public TextureMesh2D(float[] vertices, Texture texture) {
        this.vertexCount = vertices.length / 2;

        this.texture = texture;

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        // position vbo
        FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length);
        verticesBuffer.put(vertices).flip();
        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
        // Unbind the VBO
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        // Unbind the VAO
        glBindVertexArray(0);
    }

    @Override
    public void render() {
        glActiveTexture(GL_TEXTURE0);
        texture.bind();

        glBindVertexArray(vaoId);

        glDrawArrays(GL_TRIANGLE_STRIP, 0, vertexCount);

        glBindVertexArray(0);
        texture.unbind();
    }

    @Override
    public void cleanUp() {
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(vboId);

        texture.cleanUp();

        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    @Override
    public Material getMaterial() {
        return texture;
    }

    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }
}
