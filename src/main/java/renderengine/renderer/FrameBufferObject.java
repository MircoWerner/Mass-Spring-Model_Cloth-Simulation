package renderengine.renderer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImageWrite.stbi_write_png;

/**
 * @author Mirco Werner
 */
public class FrameBufferObject {
    private final int width;
    private final int height;

    private final int frameBufferId;
    private final int textureAttachment;
    private final int depthBufferId;
    //private final int depthTextureAttachment;

    public FrameBufferObject(int width, int height) {
        this.width = width;
        this.height = height;

        frameBufferId = createFrameBuffer();
        textureAttachment = createTextureAttachment(width, height);
        depthBufferId = createDepthBuffer(width, height);
        //depthTextureAttachment = createDepthTextureAttachment(width, height);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    private int createFrameBuffer() {
        int frameBuffer = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer);
        GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
        return frameBuffer;
    }

    private int createTextureAttachment(int width, int height) {
        int texture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, texture, 0);
        return texture;
    }

    private int createDepthBuffer(int width, int height) {
        int depthBuffer = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthBuffer);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL11.GL_DEPTH_COMPONENT, width, height);
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, depthBuffer);
        return depthBuffer;
    }

//    private int createDepthTextureAttachment(int width, int height) {
//        int texture = GL11.glGenTextures();
//        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
//        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT32, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer) null);
//        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
//        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
//        GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, texture, 0);
//        return texture;
//    }

    public void bind() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBufferId);
        GL11.glViewport(0, 0, width, height);
    }

    public void unbind(int windowWidth, int windowHeight) {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL11.glViewport(0, 0, windowWidth, windowHeight);
    }

    public void cleanUp() {
        GL30.glDeleteFramebuffers(frameBufferId);
        GL11.glDeleteTextures(textureAttachment);
        GL30.glDeleteRenderbuffers(depthBufferId);
        //GL11.glDeleteTextures(depthTextureAttachment);
    }

    public int getTexture() {
        return textureAttachment;
    }

    //public int getDepthTexture() {
    //    return depthTextureAttachment;
    //}

    public void renderToPng(String filePath, int windowWidth, int windowHeight) {
        bind();
        ByteBuffer imageBuffer = BufferUtils.createByteBuffer(3 * width * height);
        glPixelStorei(GL_PACK_ALIGNMENT, 1);
        glReadPixels(0, 0, width, height, GL_RGB, GL_UNSIGNED_BYTE, imageBuffer);
        stbi_write_png(filePath, width, height, 3, imageBuffer, width * 3);
        unbind(windowWidth, windowHeight);
    }
}
