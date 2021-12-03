package massspringcloth.cloth;

import org.joml.Vector3f;
import renderengine.camera.ACamera;
import renderengine.engine.Window;
import renderengine.entities.Entity;
import renderengine.entities.Light;
import renderengine.mesh.Texture;
import org.lwjgl.BufferUtils;
import renderengine.shader.ComputeShaderProgram;
import renderengine.shader.ShaderProgram;
import renderengine.utils.Transformation;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL42.GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT;
import static org.lwjgl.opengl.GL42.glMemoryBarrier;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BARRIER_BIT;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER;

/**
 * @author Mirco Werner
 */
public class MassSpringCloth {
    private ShaderProgram shaderProgram;
    private ComputeShaderProgram computeProgram;

    private int vaoId;
    private int vertexVboId;
    private int textureVboId;
    private int normalsVboId;
    private int tangentsVboId;
    private int indicesVboId;
    private Texture texture;
    private Texture textureNormalMap;

    private int inputBufferId;
    private int outputBufferId;

    private final boolean sphereEnabled;
    private final int normalSign;
    private final float mass;
    private final float viscousDamping;
    private final float springConstant;
    private final int width;
    private final int height;

    private int count;

    private final Entity entity = new Entity();

    public MassSpringCloth(MassSpringModel massSpringModel, int normalSign, boolean sphereEnabled, float mass, float viscousDamping, float springConstant) throws Exception {
        this.normalSign = normalSign;
        this.sphereEnabled = sphereEnabled;
        this.mass = mass;
        this.viscousDamping = viscousDamping;
        this.springConstant = springConstant;
        this.width = massSpringModel.getWidth();
        this.height = massSpringModel.getHeight();
        init(massSpringModel);
    }

    private void init(MassSpringModel massSpringModel) throws Exception {
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader("shaders/cloth_vert.glsl");
        shaderProgram.createFragmentShader("shaders/cloth_frag.glsl");
        shaderProgram.link();
        shaderProgram.createUniform("texture_sampler");
        shaderProgram.createUniform("texture_sampler_normal");
        shaderProgram.createUniform("transformationMatrix");
        shaderProgram.createUniform("projectionMatrix");
        shaderProgram.createUniform("viewMatrix");
        shaderProgram.createUniform("skyColor");
        shaderProgram.createUniform("lightPosition");
        shaderProgram.createUniform("lightColor");
        shaderProgram.unbind();

        createComputeShaderBuffers(massSpringModel);

        // VAO
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        // vertex position vbo
        vertexVboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexVboId);
        glBufferData(GL_ARRAY_BUFFER, massSpringModel.getVertexBufferLengthInBytes(), GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0);

        // texture vbo
        textureVboId = glGenBuffers();
        FloatBuffer textureBuffer = BufferUtils.createFloatBuffer(massSpringModel.getTex().length);
        textureBuffer.put(massSpringModel.getTex()).flip();
        glBindBuffer(GL_ARRAY_BUFFER, textureVboId);
        glBufferData(GL_ARRAY_BUFFER, textureBuffer, GL_STATIC_DRAW);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

        // normal vbo
        normalsVboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, normalsVboId);
        glBufferData(GL_ARRAY_BUFFER, massSpringModel.getVertexBufferLengthInBytes(), GL_STATIC_DRAW); // vertex buffer length same as normal buffer length
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 4, GL_FLOAT, false, 0, 0);

        // tangent vbo
        tangentsVboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, tangentsVboId);
        glBufferData(GL_ARRAY_BUFFER, massSpringModel.getVertexBufferLengthInBytes(), GL_STATIC_DRAW); // vertex buffer length same as tangent buffer length
        glEnableVertexAttribArray(3);
        glVertexAttribPointer(3, 4, GL_FLOAT, false, 0, 0);

        // index vbo
        indicesVboId = glGenBuffers();
        IntBuffer indicesBuffer = BufferUtils.createIntBuffer(massSpringModel.getIndices().length);
        indicesBuffer.put(massSpringModel.getIndices()).flip();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indicesVboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

        // Unbind
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        count = massSpringModel.getIndices().length;

        texture = Texture.loadTexture("textures/cloth.png");
        textureNormalMap = Texture.loadTexture("textures/cloth_normal.png");

        computeProgram = new ComputeShaderProgram();
        computeProgram.createComputeShader("shaders/cloth_compute.glsl");
        computeProgram.link();
        computeProgram.initProgram();
        computeProgram.createUniform("time");
        computeProgram.createUniform("normalSign");
        computeProgram.createUniform("sphereEnabled");
        computeProgram.createUniform("width");
        computeProgram.createUniform("height");
        computeProgram.createUniform("mass");
        computeProgram.createUniform("viscousDamping");
        computeProgram.createUniform("springConstant");
        computeProgram.createUniform("relaxation");
        computeProgram.unbind();

        useComputeShaderProgram(0, 0);
        int store = outputBufferId;
        outputBufferId = inputBufferId;
        inputBufferId = store;
        useComputeShaderProgram(0, 1);
        store = outputBufferId;
        outputBufferId = inputBufferId;
        inputBufferId = store;
    }

    private void createComputeShaderBuffers(MassSpringModel model) {
        // input buffer
        {
            FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(model.getPoints().length);
            verticesBuffer.put(model.getPoints()).flip();
            inputBufferId = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, inputBufferId);
            glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(0);
            glEnableVertexAttribArray(1);
            glEnableVertexAttribArray(2);
            glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0);
            glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, 0);
            glVertexAttribPointer(2, 4, GL_FLOAT, false, 0, 0);
        }
        // output buffer
        {
            outputBufferId = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, outputBufferId);
            glBufferData(GL_ARRAY_BUFFER, model.getPointsBufferLengthInBytes(), GL_STATIC_DRAW); // 4 times 4 byte floats per vertex
            glEnableVertexAttribArray(0);
            glEnableVertexAttribArray(1);
            glEnableVertexAttribArray(2);
            glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0);
            glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, 0);
            glVertexAttribPointer(2, 4, GL_FLOAT, false, 0, 0);
        }
    }

    private static final float FOV = (float) Math.toRadians(60.0f);
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000.0f;

    public void render(Window window, ACamera camera, Light light) {
        shaderProgram.bind();

        shaderProgram.setUniform("texture_sampler", 0);
        shaderProgram.setUniform("texture_sampler_normal", 1);
        shaderProgram.setUniform("viewMatrix", Transformation.getViewMatrix(camera));
        shaderProgram.setUniform("projectionMatrix", Transformation.getProjectionMatrix(FOV, window.getWidth(), window.getHeight(), Z_NEAR, Z_FAR));
        shaderProgram.setUniform("transformationMatrix", Transformation.getTransformationMatrix(entity));
        shaderProgram.setUniform("skyColor", new Vector3f(Window.RED, Window.GREEN, Window.BLUE));
        shaderProgram.setUniform("lightPosition", light.getPosition());
        shaderProgram.setUniform("lightColor", light.getColor());

        glActiveTexture(GL_TEXTURE0);
        texture.bind();
        glActiveTexture(GL_TEXTURE1);
        textureNormalMap.bind();

        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glEnableVertexAttribArray(3);

        glDrawElements(GL_TRIANGLES, count, GL_UNSIGNED_INT, 0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glDisableVertexAttribArray(3);
        glBindVertexArray(0);
        texture.unbind();
        textureNormalMap.unbind();

        shaderProgram.unbind();
    }

    public void simulate() {
        float timeStep = 0.01f;
        for (var i = 0; i < 10; i++) {
            useComputeShaderProgram(timeStep, 0);
            // switch input and output buffer for next iteration
            int store = outputBufferId;
            outputBufferId = inputBufferId;
            inputBufferId = store;

            useComputeShaderProgram(timeStep, 1);
            // switch input and output buffer for next iteration
            store = outputBufferId;
            outputBufferId = inputBufferId;
            inputBufferId = store;
        }
    }

    private void useComputeShaderProgram(float time, int relaxation) {
        computeProgram.bind();

        // set uniforms
        computeProgram.setUniform("time", time);
        computeProgram.setUniform("normalSign", normalSign);
        computeProgram.setUniform("sphereEnabled", sphereEnabled ? 1 : 0);
        computeProgram.setUniform("width", width);
        computeProgram.setUniform("height", height);
        computeProgram.setUniform("mass", mass);
        computeProgram.setUniform("viscousDamping", viscousDamping);
        computeProgram.setUniform("springConstant", springConstant);
        computeProgram.setUniform("relaxation", relaxation);

        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, inputBufferId);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, outputBufferId);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 2, vertexVboId);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 3, normalsVboId);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 4, tangentsVboId);

        computeProgram.dispatch(width, height);

        glMemoryBarrier(GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT); // wait until data is written to the vbos
        glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT); // wait until data is written to the output buffer

        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, 0);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, 0);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 2, 0);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 3, 0);
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 4, 0);

        computeProgram.unbind();
    }

    public void cleanUp() {
        glDeleteBuffers(vertexVboId);
        glDeleteBuffers(textureVboId);
        glDeleteBuffers(normalsVboId);
        glDeleteBuffers(indicesVboId);
        glDeleteVertexArrays(vaoId);
        texture.cleanUp();
        shaderProgram.cleanUp();
        computeProgram.cleanUp();
    }
}
