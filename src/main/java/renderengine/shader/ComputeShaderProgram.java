package renderengine.shader;

import org.lwjgl.BufferUtils;
import renderengine.utils.IOUtils;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL43.*;

/**
 * @author Mirco Werner
 */
public class ComputeShaderProgram extends AShaderProgram {
    private int computeShaderId;

    private int workGroupSizeX;
    private int workGroupSizeY;

    public ComputeShaderProgram() throws Exception {
        super();
    }

    public void createComputeShader(String fileName) throws Exception {
        computeShaderId = createShader(IOUtils.readAllLines(fileName), GL_COMPUTE_SHADER);
    }

    public void link() throws Exception {
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw new Exception("Error linking shader code: " + glGetProgramInfoLog(programId, 1024));
        }

        if (computeShaderId != 0) {
            glDetachShader(programId, computeShaderId);
        }

        glValidateProgram(programId);
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
            System.err.println("Warning validating shader code: " + glGetProgramInfoLog(programId, 1024));
        }
    }

    public void initProgram() {
        bind();

        IntBuffer workGroupSize = BufferUtils.createIntBuffer(3);
        glGetProgramiv(programId, GL_COMPUTE_WORK_GROUP_SIZE, workGroupSize);
        workGroupSizeX = workGroupSize.get(0);
        workGroupSizeY = workGroupSize.get(1);

        unbind();
    }

    public void dispatch(int workSizeX, int workSizeY) {
        glDispatchCompute(workSizeX / workGroupSizeX, workSizeY / workGroupSizeY, 1);
    }
}
