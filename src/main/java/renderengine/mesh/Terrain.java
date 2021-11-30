package renderengine.mesh;

import renderengine.mesh.IMesh;
import renderengine.mesh.Material;
import renderengine.mesh.Model;
import renderengine.mesh.Texture;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;

/**
 * @author Mirco Werner, https://www.youtube.com/user/ThinMatrix (Tutorial series "OpenGL 3D Game Tutorial")
 */
public class Terrain implements IMesh {
    private static final float SIZE = 256;
    private static final int VERTEX_COUNT = 128;

    private final float x;
    private final float z;
    private final Model model;
    private final Texture texture;

    private float scale = 1f;

    public Terrain(int gridX, int gridZ, Texture texture) {
        this.x = gridX * SIZE;
        this.z = gridZ * SIZE;
        this.model = generateTerrain();
        this.texture = texture;
    }

    private Model generateTerrain() {
        int count = VERTEX_COUNT * VERTEX_COUNT;
        float[] vertices = new float[count * 3];
        float[] normals = new float[count * 3];
        float[] textureCoords = new float[count * 2];
        int[] indices = new int[6 * (VERTEX_COUNT - 1) * (VERTEX_COUNT - 1)];
        int vertexPointer = 0;
        for (int i = 0; i < VERTEX_COUNT; i++) {
            for (int j = 0; j < VERTEX_COUNT; j++) {
                vertices[vertexPointer * 3] = j / ((float) VERTEX_COUNT - 1) * SIZE;
                vertices[vertexPointer * 3 + 1] = 0;
                vertices[vertexPointer * 3 + 2] = i / ((float) VERTEX_COUNT - 1) * SIZE;
                normals[vertexPointer * 3] = 0;
                normals[vertexPointer * 3 + 1] = 1;
                normals[vertexPointer * 3 + 2] = 0;
                textureCoords[vertexPointer * 2] = (float) j / ((float) VERTEX_COUNT - 1);
                textureCoords[vertexPointer * 2 + 1] = (float) i / ((float) VERTEX_COUNT - 1);
                vertexPointer++;
            }
        }
        int pointer = 0;
        for (int gz = 0; gz < VERTEX_COUNT - 1; gz++) {
            for (int gx = 0; gx < VERTEX_COUNT - 1; gx++) {
                int topLeft = (gz * VERTEX_COUNT) + gx;
                int topRight = topLeft + 1;
                int bottomLeft = ((gz + 1) * VERTEX_COUNT) + gx;
                int bottomRight = bottomLeft + 1;
                indices[pointer++] = topLeft;
                indices[pointer++] = bottomLeft;
                indices[pointer++] = topRight;
                indices[pointer++] = topRight;
                indices[pointer++] = bottomLeft;
                indices[pointer++] = bottomRight;
            }
        }
        return new Model(vertices, textureCoords, normals, indices);
    }

    public void prepareRender() {
        glActiveTexture(GL_TEXTURE0);
        texture.bind();

        glBindVertexArray(model.getVaoId());
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
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
    }

    @Override
    public void cleanUp() {
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(model.getVboId());
        glDeleteBuffers(model.getTextureVboId());
        glDeleteBuffers(model.getNormalsVboId());
        glDeleteBuffers(model.getIndicesVboId());

        glBindVertexArray(0);
        glDeleteVertexArrays(model.getVaoId());

        texture.cleanUp();
    }

    public float getX() {
        return x;
    }

    public float getZ() {
        return z;
    }

    @Override
    public Material getMaterial() {
        return texture;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }
}
