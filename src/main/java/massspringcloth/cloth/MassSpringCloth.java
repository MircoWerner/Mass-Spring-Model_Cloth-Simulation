package massspringcloth.cloth;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import renderengine.camera.ACamera;
import renderengine.engine.Window;
import renderengine.entities.Entity;
import renderengine.entities.Light;
import renderengine.mesh.Texture;
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
 * This class allows to execute the simulation (the compute shader) and to render the cloth.
 * Therefore, the compute, vertex and fragment shaders and the required buffers are created.
 * The shaders are defined in the resources/shaders package.
 *
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
    private final Vector3f velocityFluid;
    private final float springConstant;
    private final int width;
    private final int height;

    private int count;

    private final Entity entity = new Entity();

    /**
     * Creates and initializes the cloth object with the given parameters.
     *
     * @param massSpringModel model containing the initial positions etc.
     * @param normalSign      normal orientation \in {-1,1}
     * @param sphereEnabled   true if sphere collisions are enabled, false otherwise
     * @param mass            mass of one point
     * @param viscousDamping  damping constant >= 0, higher damping constant causes more friction
     * @param velocityFluid   velocity of a viscous fluid like wind or water (used for viscous interaction force)
     * @param springConstant  spring constant >= 0, higher spring constant makes cloth more stiff
     * @throws Exception if the creation of the object fails
     */
    public MassSpringCloth(MassSpringModel massSpringModel, int normalSign, boolean sphereEnabled, float mass, float viscousDamping, Vector3f velocityFluid, float springConstant) throws Exception {
        this.normalSign = normalSign;
        this.sphereEnabled = sphereEnabled;
        this.mass = mass;
        this.viscousDamping = viscousDamping;
        this.velocityFluid = velocityFluid;
        this.springConstant = springConstant;
        this.width = massSpringModel.getWidth();
        this.height = massSpringModel.getHeight();
        init(massSpringModel);
    }

    /**
     * Creates the compute, vertex and fragment shader.
     *
     * @param massSpringModel model containing the initial positions etc.
     * @throws Exception if the creation of the object fails
     */
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

        // unbind
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
        computeProgram.createUniform("velocityFluid");
        computeProgram.createUniform("springConstant");
        computeProgram.createUniform("state");
        computeProgram.unbind();

        useComputeShaderProgram(0, -1); // execute one time that the vbos contain valid data that can be rendered
        // switch input and output buffer for next iteration
        int store = outputBufferId;
        outputBufferId = inputBufferId;
        inputBufferId = store;
    }

    /**
     * Creates the input and output buffer of the compute shader.
     *
     * @param model model containing the initial positions etc.
     */
    private void createComputeShaderBuffers(MassSpringModel model) {
        // input buffer
        {
            FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(model.getPoints().length); // put initial data in the input buffer
            verticesBuffer.put(model.getPoints()).flip();
            inputBufferId = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, inputBufferId);
            glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, inputBufferId);
            glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(0);
            glEnableVertexAttribArray(1);
            glEnableVertexAttribArray(2);
            glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0);
            glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, 0);
            glVertexAttribPointer(2, 4, GL_FLOAT, false, 0, 0);
            glDisableVertexAttribArray(0);
            glDisableVertexAttribArray(1);
            glDisableVertexAttribArray(2);
            glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, 0);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
        }
        // output buffer
        {
            outputBufferId = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, outputBufferId);
            glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, outputBufferId);
            glBufferData(GL_ARRAY_BUFFER, model.getPointsBufferLengthInBytes(), GL_STATIC_DRAW); // 4 times 4 byte floats per vertex
            glEnableVertexAttribArray(0);
            glEnableVertexAttribArray(1);
            glEnableVertexAttribArray(2);
            glVertexAttribPointer(0, 4, GL_FLOAT, false, 0, 0);
            glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, 0);
            glVertexAttribPointer(2, 4, GL_FLOAT, false, 0, 0);
            glDisableVertexAttribArray(0);
            glDisableVertexAttribArray(1);
            glDisableVertexAttribArray(2);
            glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, 0);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
        }
    }

    private static final float FOV = (float) Math.toRadians(60.0f);
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000.0f;

    /**
     * Renders the cloth.
     *
     * @param window window of the application
     * @param camera camera of the scene
     * @param light  light in the scene
     */
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

        glActiveTexture(GL_TEXTURE0); // cloth texture
        texture.bind();
        glActiveTexture(GL_TEXTURE1); // normal map
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

    /**
     * Executes the simulation (the compute shader).
     *
     * @param iterations how often the simulation is executed
     */
    public void simulate(int iterations) {
        float timeStep = 0.01f; // maybe make this depend on the timeSinceLastFrame
        for (int i = 0; i < iterations; i++) {
            useComputeShaderProgram(timeStep, 0); // apply forces
            // switch input and output buffer for next iteration
            int store = outputBufferId;
            outputBufferId = inputBufferId;
            inputBufferId = store;

            useComputeShaderProgram(timeStep, 1); // relaxation, adjust too long joints, make the model more stable
            // switch input and output buffer for next iteration
            store = outputBufferId;
            outputBufferId = inputBufferId;
            inputBufferId = store;
        }
    }

    /**
     * Executes the compute shader.
     *
     * @param time  simulation time step
     * @param state 0 => apply forces, 1 => relaxation of the joints, any other number => only write output buffers (and calculate normals, tangents etc.)
     */
    private void useComputeShaderProgram(float time, int state) {
        computeProgram.bind();

        // set uniforms, see compute shader src for uniform documentation
        computeProgram.setUniform("time", time);
        computeProgram.setUniform("normalSign", normalSign);
        computeProgram.setUniform("sphereEnabled", sphereEnabled ? 1 : 0);
        computeProgram.setUniform("width", width);
        computeProgram.setUniform("height", height);
        computeProgram.setUniform("mass", mass);
        computeProgram.setUniform("viscousDamping", viscousDamping);
        computeProgram.setUniform("velocityFluid", velocityFluid);
        computeProgram.setUniform("springConstant", springConstant);
        computeProgram.setUniform("state", state);

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

    /**
     * Frees all memory allocated for the buffers.
     */
    public void cleanUp() {
        glDeleteBuffers(inputBufferId);
        glDeleteBuffers(outputBufferId);
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
