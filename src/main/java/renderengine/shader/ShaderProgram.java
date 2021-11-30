package renderengine.shader;

import renderengine.utils.IOUtils;

import static org.lwjgl.opengl.GL20.*;

/**
 * @author Mirco Werner
 */
public class ShaderProgram extends AShaderProgram {
    private int vertexShaderId;
    private int fragmentShaderId;

    public ShaderProgram() throws Exception {
        super();
    }

    public void createVertexShader(String fileName) throws Exception {
        vertexShaderId = createShader(IOUtils.readAllLines(fileName), GL_VERTEX_SHADER);
    }

    public void createFragmentShader(String fileName) throws Exception {
        fragmentShaderId = createShader(IOUtils.readAllLines(fileName), GL_FRAGMENT_SHADER);
    }

    public void link() throws Exception {
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw new Exception("Error linking shader code: " + glGetProgramInfoLog(programId, 1024));
        }

        if (vertexShaderId != 0) {
            glDetachShader(programId, vertexShaderId);
        }
        if (fragmentShaderId != 0) {
            glDetachShader(programId, fragmentShaderId);
        }

        glValidateProgram(programId);
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
            System.err.println("Warning validating shader code: " + glGetProgramInfoLog(programId, 1024));
        }
    }
}
