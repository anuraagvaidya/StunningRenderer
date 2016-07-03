package com.bysness.stunningrenderer;

import com.jogamp.opengl.GL2ES2;

import java.nio.IntBuffer;

/**
 * Created by Anuraag on 5/1/2016.
 */
public class StunningUI_Texture {
    /** The GL target type */
    private int target;
    /** The GL texture ID */
    IntBuffer textureID;
    /** The canvasHeight of the image */
    private int height;
    /** The canvasWidth of the image */
    private int width;
    /** The canvasWidth of the texture */
    private int texWidth;
    /** The canvasHeight of the texture */
    private int texHeight;
    /** The ratio of the canvasWidth of the image to the texture */
    private float widthRatio;
    /** The ratio of the canvasHeight of the image to the texture */
    private float heightRatio;

    /**
     * Create a new texture
     *
     * @param target The GL target
     * @param textureID The GL texture ID
     */
    public StunningUI_Texture(int target,IntBuffer textureID) {
        this.target = target;
        this.textureID = textureID;
    }

    /**
     * Bind the specified GL context to a texture
     *
     * @param gl The GL context to bind to
     */
    public void bind(GL2ES2 gl) {
        gl.glBindTexture(target, textureID.get(0));
    }

    /**
     * Set the canvasHeight of the image
     *
     * @param height The canvasHeight of the image
     */
    public void setHeight(int height) {
        this.height = height;
        setHeight();
    }

    /**
     * Set the canvasWidth of the image
     *
     * @param width The canvasWidth of the image
     */
    public void setWidth(int width) {
        this.width = width;
        setWidth();
    }

    /**
     * Get the canvasHeight of the original image
     *
     * @return The canvasHeight of the original image
     */
    public int getImageHeight() {
        return height;
    }

    /**
     * Get the canvasWidth of the original image
     *
     * @return The canvasWidth of the original image
     */
    public int getImageWidth() {
        return width;
    }

    /**
     * Get the canvasHeight of the physical texture
     *
     * @return The canvasHeight of physical texture
     */
    public float getHeight() {
        return heightRatio;
    }

    /**
     * Get the canvasWidth of the physical texture
     *
     * @return The canvasWidth of physical texture
     */
    public float getWidth() {
        return widthRatio;
    }

    /**
     * Set the canvasHeight of this texture
     *
     * @param texHeight The canvasHeight of the texture
     */
    public void setTextureHeight(int texHeight) {
        this.texHeight = texHeight;
        setHeight();
    }

    /**
     * Set the canvasWidth of this texture
     *
     * @param texWidth The canvasWidth of the texture
     */
    public void setTextureWidth(int texWidth) {
        this.texWidth = texWidth;
        setWidth();
    }

    /**
     * Set the canvasHeight of the texture. This will update the
     * ratio also.
     */
    private void setHeight() {
        if (texHeight != 0) {
            heightRatio = ((float) height)/texHeight;
        }
    }

    /**
     * Set the canvasWidth of the texture. This will update the
     * ratio also.
     */
    private void setWidth() {
        if (texWidth != 0) {
            widthRatio = ((float) width)/texWidth;
        }
    }
}