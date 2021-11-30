package renderengine.mesh;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/**
 * @author Mirco Werner
 */
public class Model {
    private final int vertexCount;

    private final int vaoId;
    private final int vboId;
    private final int textureVboId;
    private final int normalsVboId;
    private final int indicesVboId;

    public Model(float[] vertices, float[] texVertices, float[] normals, int[] indices) {
        this.vertexCount = indices.length;

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        // position vbo
        FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length);
        verticesBuffer.put(vertices).flip();
        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        // texture vbo
        textureVboId = glGenBuffers();
        FloatBuffer textureBuffer = BufferUtils.createFloatBuffer(texVertices.length);
        textureBuffer.put(texVertices).flip();
        glBindBuffer(GL_ARRAY_BUFFER, textureVboId);
        glBufferData(GL_ARRAY_BUFFER, textureBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

        // normal vbo
        normalsVboId = glGenBuffers();
        FloatBuffer normalsBuffer = BufferUtils.createFloatBuffer(normals.length);
        normalsBuffer.put(normals).flip();
        glBindBuffer(GL_ARRAY_BUFFER, normalsVboId);
        glBufferData(GL_ARRAY_BUFFER, normalsBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);

        // index vbo
        IntBuffer indicesBuffer = BufferUtils.createIntBuffer(indices.length);
        indicesBuffer.put(indices).flip();
        indicesVboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indicesVboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

        // Unbind the VBO
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        // Unbind the VAO
        glBindVertexArray(0);
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public int getVaoId() {
        return vaoId;
    }

    public int getVboId() {
        return vboId;
    }

    public int getTextureVboId() {
        return textureVboId;
    }

    public int getNormalsVboId() {
        return normalsVboId;
    }

    public int getIndicesVboId() {
        return indicesVboId;
    }
}
