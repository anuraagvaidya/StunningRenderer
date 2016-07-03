package com.bysness.stunningrenderer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * Created by Anuraag on 4/29/2016.
 */
public class StunningUI_Utilities {
    private static StunningUI_Utilities ourInstance = new StunningUI_Utilities();

    public static StunningUI_Utilities getInstance() {
        return ourInstance;
    }

    private StunningUI_Utilities() {
    }

    /* Introducing projection matrix helper functions
     *
     * OpenGL ES 2 vertex projection transformations gets applied inside the
     * vertex shader, all you have to do are to calculate and supply a projection matrix.
     *
     * Its recomended to use the com/jogamp/opengl/util/PMVMatrix.java
     * import com.jogamp.opengl.util.PMVMatrix;
     * To simplify all your projection model view matrix creation needs.
     *
     * These helpers here are based on PMVMatrix code and common linear
     * algebra for matrix multiplication, translate and rotations.
     */
    void glMultMatrixf(FloatBuffer a, FloatBuffer b, FloatBuffer d) {
        final int aP = a.position();
        final int bP = b.position();
        final int dP = d.position();
        for (int i = 0; i < 4; i++) {
            final float ai0=a.get(aP+i+0*4),  ai1=a.get(aP+i+1*4),  ai2=a.get(aP+i+2*4),  ai3=a.get(aP+i+3*4);
            d.put(dP+i+0*4 , ai0 * b.get(bP+0+0*4) + ai1 * b.get(bP+1+0*4) + ai2 * b.get(bP+2+0*4) + ai3 * b.get(bP+3+0*4) );
            d.put(dP+i+1*4 , ai0 * b.get(bP+0+1*4) + ai1 * b.get(bP+1+1*4) + ai2 * b.get(bP+2+1*4) + ai3 * b.get(bP+3+1*4) );
            d.put(dP+i+2*4 , ai0 * b.get(bP+0+2*4) + ai1 * b.get(bP+1+2*4) + ai2 * b.get(bP+2+2*4) + ai3 * b.get(bP+3+2*4) );
            d.put(dP+i+3*4 , ai0 * b.get(bP+0+3*4) + ai1 * b.get(bP+1+3*4) + ai2 * b.get(bP+2+3*4) + ai3 * b.get(bP+3+3*4) );
        }
    }

    float[] multiply(float[] a,float[] b){
        float[] tmp = new float[16];
        glMultMatrixf(FloatBuffer.wrap(a),FloatBuffer.wrap(b), FloatBuffer.wrap(tmp));
        return tmp;
    }

    float[] translate(float[] m,float x,float y,float z){
        float[] t = { 1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                x, y, z, 1.0f };
        return multiply(m, t);
    }

    float[] rotate(float[] m,float a,float x,float y,float z){
        float s, c;
        s = (float)Math.sin(Math.toRadians(a));
        c = (float)Math.cos(Math.toRadians(a));
        float[] r = {
                x * x * (1.0f - c) + c,     y * x * (1.0f - c) + z * s, x * z * (1.0f - c) - y * s, 0.0f,
                x * y * (1.0f - c) - z * s, y * y * (1.0f - c) + c,     y * z * (1.0f - c) + x * s, 0.0f,
                x * z * (1.0f - c) + y * s, y * z * (1.0f - c) - x * s, z * z * (1.0f - c) + c,     0.0f,
                0.0f, 0.0f, 0.0f, 1.0f };
        return multiply(m, r);
    }


    byte[][][] getPixels(BufferedImage image,int width,int height) {
        byte[][][] result = new byte[width][height][4];
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                //todo: do bitwise operators
                Color c = new Color(image.getRGB(x, y), true);
                result[x][y][0] = (byte)c.getRed();
                result[x][y][1] = (byte)c.getGreen();
                result[x][y][2] = (byte)c.getBlue();
                result[x][y][3] = (byte)c.getAlpha();
            }
        }
        return result;
    }


    public StunningUI_Sprite getTopMostSprite(ArrayList<StunningUI_Sprite> sprites)
    {
        if(sprites!=null)
        {
            if(sprites.size()>0)
            {
                int lastTopSpriteZIndex=0;
                int lastTopSprite=0;
                for(int i=0;i<sprites.size();i++)
                {
                    System.out.println("text: " + sprites.get(i).text + ", hasExtra:" + (sprites.get(i).extras!=null) + ", cI:" + sprites.get(i).calculateZIndex());
                    int spriteZIndex=sprites.get(i).calculateZIndex();
                    if(spriteZIndex>lastTopSprite)
                    {
                        lastTopSprite=spriteZIndex;
                        lastTopSpriteZIndex=i;
                    }
                }
                return sprites.get(lastTopSpriteZIndex);
            }
            else {
                return null;
            }
        }
        else
        {
            return null;
        }
    }

    float getActualLeftX(float left)
    {
        return left-1f;
    }
    float getActualWidthToX(float width)
    {
        return width;
    }
    float getActualTopToY(float top)
    {
        return top+1f;
    }
    float getActualHeightToY(float height)
    {
        return -1f*height;
    }

    int randomInt(int Min,int Max){
        return Min + (int)(Math.random() * ((Max - Min) + 1));
    }

    /**
     * Load a given resource as a buffered image
     *
     * @param ref The location of the resource to load
     * @return The loaded buffered image
     * @throws IOException Indicates a failure to find a resource
     */
    BufferedImage loadImage(String ref) throws IOException
    {
        URL url = StunningUI_TextureLoader.class.getClassLoader().getResource(ref);
        BufferedImage bufferedImage;
        try {
            if(url!=null)
            {
                bufferedImage = ImageIO.read(new BufferedInputStream(getClass().getClassLoader().getResourceAsStream(ref)));
            }
            else {
                bufferedImage = ImageIO.read(new File(ref));
            }

        }
        catch (IOException e) {
            throw e;
        }
        return bufferedImage;
    }

    void saveImage(String filename,BufferedImage bufferedImage){
        File outputfile = new File(filename);
        try
        {
            ImageIO.write(bufferedImage, "jpg", outputfile);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
