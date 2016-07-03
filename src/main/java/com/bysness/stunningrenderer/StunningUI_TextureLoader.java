package com.bysness.stunningrenderer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * Created by Anuraag on 5/1/2016.
 */
public class StunningUI_TextureLoader {
    /** The table of textures that have been loaded in this loader */
    private HashMap table = new HashMap();
    /** The GL context used to load textures */
    private GL2ES2 gl;
    /** The colour model including alpha for the GL image */
    private ColorModel glAlphaColorModel;
    /** The colour model for the GL image */
    private ColorModel glColorModel;

    //Anuraag's edits
    private int repeat;

    /**
     * Create a new texture loader based on the game panel
     *
     * @param gl The GL content in which the textures should be loaded
     */
    public StunningUI_TextureLoader(GL2ES2 gl, int repeat) {
        this.gl = gl;
        this.repeat=repeat;

        glAlphaColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
                new int[] {8,8,8,8},
                true,
                false,
                ComponentColorModel.TRANSLUCENT,
                DataBuffer.TYPE_BYTE);

        glColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
                new int[] {8,8,8,0},
                false,
                false,
                ComponentColorModel.OPAQUE,
                DataBuffer.TYPE_BYTE);
    }

    /**
     * Create a new texture ID
     *
     * @return A new texture ID
     */
    private IntBuffer createTextureID()
    {
        IntBuffer tmp=IntBuffer.allocate(1);
        gl.glGenTextures(1, tmp);
        return tmp;
    }

    /**
     * Load a texture
     *
     * @param resourceName The location of the resource to load
     * @return The loaded texture
     * @throws IOException Indicates a failure to access the resource
     */
//    public StunningUI_Texture getTexture(String resourceName) {
//        StunningUI_Texture tex = (StunningUI_Texture) table.get(resourceName);
//
//        if (tex != null) {
//            return tex;
//        }
//
//        /*//edit by Anuraag:
//        gl.glPixelStorei(GL2ES2.GL_UNPACK_ALIGNMENT,1);*/
//        try
//        {
//
//            BufferedImage bufferedImage = loadImage(resourceName);
//            tex = getTexture(bufferedImage,
//                    GL2ES2.GL_TEXTURE_2D, // target
//                    GL2ES2.GL_RGBA,     // dst pixel format
//                    GL2ES2.GL_LINEAR, // min filter (unused)
//                    GL2ES2.GL_LINEAR);
//
//            table.put(resourceName,tex);
//
//            return tex;
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//        return null;
//    }

    /**
     * Load a texture into OpenGL from a image reference on
     * disk.
     *
     * @param bufferedImage The bufferedImage data to load from
     * @param target The GL target to load the texture against
     * @param dstPixelFormat The pixel format of the screen
     * @param minFilter The minimising filter
     * @param magFilter The magnification filter
     * @return The loaded texture
     * @throws IOException Indicates a failure to access the resource
     */
    public StunningUI_Texture getTexture(BufferedImage bufferedImage,
                              int target,
                              int dstPixelFormat,
                              int minFilter,
                              int magFilter)
    {
        int srcPixelFormat = 0;

        // create the texture ID for this texture
        IntBuffer textureID = createTextureID();
        StunningUI_Texture texture = new StunningUI_Texture(target,textureID);

        // bind this texture
        gl.glBindTexture(target, textureID.get(0));
        texture.setWidth(bufferedImage.getWidth());
        texture.setHeight(bufferedImage.getHeight());

        if (bufferedImage.getColorModel().hasAlpha()) {
            srcPixelFormat = GL2ES2.GL_RGBA;
        } else {
            srcPixelFormat = GL2ES2.GL_RGB;
        }

        // convert that image into a byte buffer of texture data
        ByteBuffer textureBuffer = convertImageData(bufferedImage,texture);

        if (target == GL2ES2.GL_TEXTURE_2D)
        {
            gl.glTexParameteri(target, GL2ES2.GL_TEXTURE_MIN_FILTER, minFilter);
            gl.glTexParameteri(target, GL2ES2.GL_TEXTURE_MAG_FILTER, magFilter);
            if(this.repeat==StunningUI_Style.BG_REPEAT_NO_REPEAT)
            {
                gl.glTexParameterf(target, GL2ES2.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
                gl.glTexParameterf(target, GL2ES2.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
            }
            else if(this.repeat==StunningUI_Style.BG_REPEAT_REPEAT_X)
            {
                gl.glTexParameterf(target, GL2ES2.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
                gl.glTexParameterf(target, GL2ES2.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
            }
            else if(this.repeat==StunningUI_Style.BG_REPEAT_REPEAT_Y)
            {
                gl.glTexParameterf(target, GL2ES2.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
                gl.glTexParameterf(target, GL2ES2.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
            }
            else if(this.repeat==StunningUI_Style.BG_REPEAT_REPEAT_BOTH)
            {
                gl.glTexParameterf(target, GL2ES2.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
                gl.glTexParameterf(target, GL2ES2.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
            }
        }

        // produce a texture from the byte buffer
        gl.glTexImage2D(target,
                0,
                dstPixelFormat,
                bufferedImage.getWidth(),
                bufferedImage.getHeight(),
                    /*get2Fold(bufferedImage.getWidth()),
                    get2Fold(bufferedImage.getHeight()), edited by Anuraag*/
                0,
                srcPixelFormat,
                GL2ES2.GL_UNSIGNED_BYTE,
                textureBuffer );
        return texture;
    }

    /**
     * Get the closest greater power of 2 to the fold number
     *
     * @param fold The target number
     * @return The power of 2
     */
    private int get2Fold(int fold) {
        int ret = 2;
        while (ret < fold) {
            ret *= 2;
        }
        return ret;
    }

    /**
     * Convert the buffered image to a texture
     *
     * @param bufferedImage The image to convert to a texture
     * @param texture The texture to store the data into
     * @return A buffer containing the data
     */
    private ByteBuffer convertImageData(BufferedImage bufferedImage,StunningUI_Texture texture) {
        ByteBuffer imageBuffer = null;
        WritableRaster raster;
        BufferedImage texImage;

        int texWidth = bufferedImage.getWidth();
        int texHeight = bufferedImage.getHeight();

        // find the closest power of 2 for the canvasWidth and canvasHeight
        // of the produced texture
        /*
        int texWidth = 2;
        int texHeight = 2;
        while (texWidth < bufferedImage.getWidth()) {
            texWidth *= 2;
        }
        while (texHeight < bufferedImage.getHeight()) {
            texHeight *= 2;
        }
        edited by Anuraag
        */

        texture.setTextureHeight(texHeight);
        texture.setTextureWidth(texWidth);

        // create a raster that can be used by OpenGL as a source
        // for a texture
        if (bufferedImage.getColorModel().hasAlpha()) {
            raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,texWidth,texHeight,4,null);
            texImage = new BufferedImage(glAlphaColorModel,raster,false,new Hashtable());
        } else {
            raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,texWidth,texHeight,3,null);
            texImage = new BufferedImage(glColorModel,raster,false,new Hashtable());
        }


        // copy the source image into the produced image
        Graphics g = texImage.getGraphics();
        g.setColor(new Color(0f,0f,0f,0f));
        g.fillRect(0,0,texWidth,texHeight);
        g.drawImage(bufferedImage,0,0,null);


        // build a byte buffer from the temporary image
        // that be used by OpenGL to produce a texture.
        byte[] data = ((DataBufferByte) texImage.getRaster().getDataBuffer()).getData();

        imageBuffer = ByteBuffer.allocateDirect(data.length);
        imageBuffer.order(ByteOrder.nativeOrder());
        imageBuffer.put(data, 0, data.length);
        imageBuffer.flip(); //edit by Anuraag
        //imageBuffer.rewind(); //edit by Anuraag

        return imageBuffer;
    }


}