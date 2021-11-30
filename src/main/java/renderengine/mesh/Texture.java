package renderengine.mesh;

import org.lwjgl.BufferUtils;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.GL_TEXTURE_LOD_BIAS;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;

/**
 * @author Mirco Werner
 */
public class Texture extends Material {
    private final int id;

    private boolean hasTransparency = false;
    private boolean useFakeLighting = false;

    private final int width;
    private final int height;

    private boolean flipped = false;

    public Texture(int id, int width, int height) {
        this.id = id;
        this.width = width;
        this.height = height;
    }

    public Texture(int id, int width, int height, boolean flipped) {
        this(id, width, height);
        this.flipped = flipped;
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public static Texture loadTexture(InputStream imageStream) throws Exception {
        int width;
        int height;
        ByteBuffer buf;

        IntBuffer w = BufferUtils.createIntBuffer(1);
        IntBuffer h = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);

        byte[] imageData = imageStream.readAllBytes();
        ByteBuffer imageBuffer = BufferUtils.createByteBuffer(imageData.length);
        imageBuffer.put(imageData);
        imageBuffer.flip();

        buf = stbi_load_from_memory(imageBuffer, w, h, channels, 4);
        if (buf == null) {
            throw new Exception("Image not loaded: " + stbi_failure_reason());
        }


        width = w.get();
        height = h.get();


        int textureId = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, textureId);

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);

        glGenerateMipmap(GL_TEXTURE_2D);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS, -0.4f);

        imageStream.close();
        stbi_image_free(buf);

        return new Texture(textureId, width, height);
    }

    public static Texture loadTexture(String resourceName) throws Exception {
        return loadTexture(Objects.requireNonNull(Texture.class.getClassLoader().getResourceAsStream(resourceName)));
    }

    public void cleanUp() {
        glDeleteTextures(id);
    }

    public void setHasTransparency(boolean hasTransparency) {
        this.hasTransparency = hasTransparency;
    }

    public boolean hasTransparency() {
        return hasTransparency;
    }

    public void setUseFakeLighting(boolean useFakeLighting) {
        this.useFakeLighting = useFakeLighting;
    }

    public boolean useFakeLighting() {
        return useFakeLighting;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setFlipped(boolean flipped) {
        this.flipped = flipped;
    }

    public boolean isFlipped() {
        return flipped;
    }
}