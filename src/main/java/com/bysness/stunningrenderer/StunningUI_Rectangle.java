package com.bysness.stunningrenderer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jogamp.common.nio.Buffers;
import com.jogamp.nativewindow.util.Rectangle;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GLAutoDrawable;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by Anuraag on 4/29/2016.
 */
public class StunningUI_Rectangle extends StunningUI_Sprite {
    //General variables
    //openGL Variables
    int[] vboHandles;
    private int vboVertices, vboColors, vboIndices, vboCoords, vboStencil;
    StunningUI_Utilities utilities;

    StunningUI_Rectangle(StunningUI_Sprite parent){
        super(parent);
        this.utilities=StunningUI_Utilities.getInstance();
    }

    private double t0 = System.currentTimeMillis();
    private double theta;
    private double s;
    private double c = 0;
    StunningUI_TextureLoader textureLoader;
    StunningUI_Texture texture=null;

    @Override
    void init(final GLAutoDrawable drawable, int ModelViewProjectionMatrix_location,int shaderProgram, StunningRenderer glishRenderer) {
        super.init(drawable, ModelViewProjectionMatrix_location,shaderProgram,glishRenderer);

        final GL2ES2 gl = drawable.getGL().getGL2ES2();

        /*vboHandles = new int[2];
        gl.glGenBuffers(2, vboHandles, 0);
        vboColors = vboHandles[0];
        vboVertices = vboHandles[1];*/
        vboHandles = new int[5];
        gl.glGenBuffers(5, vboHandles, 0);
        vboColors = vboHandles[0];
        vboVertices = vboHandles[1];
        vboIndices = vboHandles[2];
        vboCoords = vboHandles[3];
        vboStencil = vboHandles[4];

        texture=null;

        if(style.backgroundImage!=null || style.backgroundBufferedImage!=null)
        {
            //System.out.println("GENERATING TEXTURE");

            textureLoader=new StunningUI_TextureLoader(gl,style.backgroundRepeat);
            if(style.backgroundBufferedImage!=null)
            {
                texture=textureLoader.getTexture(style.backgroundBufferedImage,GL2ES2.GL_TEXTURE_2D, // target
                        GL2ES2.GL_RGBA,     // dst pixel format
                        GL2ES2.GL_LINEAR, // min filter (unused)
                        GL2ES2.GL_LINEAR);
            }
            else
            {
                try
                {

                    style.backgroundBufferedImage= utilities.loadImage(style.backgroundImage);
                    texture=textureLoader.getTexture(style.backgroundBufferedImage,GL2ES2.GL_TEXTURE_2D, // target
                            GL2ES2.GL_RGBA,     // dst pixel format
                            GL2ES2.GL_LINEAR, // min filter (unused)
                            GL2ES2.GL_LINEAR);
                }
                catch (Exception e)
                {
//                    System.out.println("Unable to load background image");
                    style.backgroundBufferedImage=null;
                    style.backgroundImage=null;
                }
            }


        }
        /*if(style.backgroundImage!=null || style.backgroundBufferedImage!=null)
        {
            gl.glDisableVertexAttribArray(2); // Allow release of textcoords memory
            if(texture!=null)
            {
                gl.glDeleteTextures(1, texture.textureID);
                texture=null;
            }
            textureLoader=null;
        }*/
    }

    @Override
    void render(final GLAutoDrawable drawable, int ModelViewProjectionMatrix_location,int shaderProgram, StunningRenderer glishRenderer, long delta) {
        super.render(drawable,ModelViewProjectionMatrix_location,shaderProgram,glishRenderer,delta);
        if(state==STATE_INVISIBLE)
        {
            return;
        }
        if(!super.initialized)
        {
            init(drawable,ModelViewProjectionMatrix_location,shaderProgram,glishRenderer);
        }
        final GL2ES2 gl = drawable.getGL().getGL2ES2();
        //

        double t1 = System.currentTimeMillis();
        theta += (t1-t0)*0.01f;
        t0 = t1;
        s = Math.sin(theta);

        float surfaceWidth=(float)drawable.getSurfaceWidth();
        float surfaceHeight=(float)drawable.getSurfaceHeight();
        float WtoHRatio=surfaceWidth/surfaceHeight;


        float currentZoom=style.zoom;
        int currentZoomLeft=-style.zoomLeft;
        int currentZoomTop=-style.zoomTop;

        if(this.parent==null) {
            style.actualLeft = 2f * (((float)(style.left+currentZoomLeft)*currentZoom) / surfaceWidth);
            style.actualTop = -2f * (((float)(style.top+currentZoomTop)*currentZoom) / surfaceHeight);
            style.totalLeft=(int)((float)(style.left+currentZoomLeft)*currentZoom);
            style.totalTop=(int)((float)(style.top+currentZoomTop)*currentZoom);
            style.totalActualLeft=style.actualLeft;
            style.totalActualTop=style.actualTop;
            //System.out.println(text+","+"parent.totalLeft:" + 0 + ", style.left:" + style.left + ", actualLeft:"+style.actualLeft);
        }
        else
        {
            if(parent.style.zoom!=1f)
            {
                style.zoom=parent.style.zoom;
                style.zoomLeft=parent.style.zoomLeft;
                style.zoomTop=parent.style.zoomTop;

                currentZoom=style.zoom;
                currentZoomLeft=-style.zoomLeft;
                currentZoomTop=-style.zoomTop;
            }
            style.actualLeft = 2f * (((float)parent.style.totalLeft + ((float)(style.left+currentZoomLeft)*currentZoom)) / surfaceWidth);
            style.actualTop = -2f * (((float)parent.style.totalTop + ((float)(style.top+currentZoomTop)*currentZoom) + ((float)parent.style.scrollTop*currentZoom)) / surfaceHeight);
            style.totalLeft=parent.style.totalLeft + (int)((float)(style.left+currentZoomLeft)*currentZoom);
            style.totalTop=parent.style.totalTop + (int)((float)(style.top+currentZoomTop)*currentZoom);

            style.totalActualLeft=style.actualLeft;
            style.totalActualTop=style.actualTop;

            glishRenderer.printLog("Current zoom: " + currentZoom + ", tag:" + tag);
        }

        float normWidth=2*((float)style.width / surfaceWidth)*currentZoom;
        float normHeight=2*((float)style.height / surfaceHeight)*currentZoom;
        style.actualWidth=normWidth;
        style.actualHeight=normHeight;
        float[] model_view_projection;

        float[] identity_matrix = {
                1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f,
        };

        model_view_projection =  utilities.translate(identity_matrix,style.actualLeft,style.actualTop,style.depth);
        //model_view_projection =  utilities.rotate(model_view_projection,(float)30f*(float)s,1.0f,0.0f,1.0f);
        identity_matrix=null;

        // Send the final projection matrix to the vertex shader by
        // using the uniform location id obtained during the init part.
        //gl.glUniformMatrix4fv(ModelViewProjectionMatrix_location, 1, false, model_view_projection, 0);
        gl.glUniformMatrix4fv(ModelViewProjectionMatrix_location, 1, false, model_view_projection, 0);

        drawable.getSurfaceHeight();
        int numBytes =0;


        int id_hasClipping=gl.glGetUniformLocation(shaderProgram,"hasClipping");
        int id_clipX0=gl.glGetUniformLocation(shaderProgram,"clipX0");
        int id_clipY0=gl.glGetUniformLocation(shaderProgram,"clipY0");
        int id_clipX1=gl.glGetUniformLocation(shaderProgram,"clipX1");
        int id_clipY1=gl.glGetUniformLocation(shaderProgram,"clipY1");

        int id_screenWidth=gl.glGetUniformLocation(shaderProgram,"screenWidth");
        int id_screenHeight=gl.glGetUniformLocation(shaderProgram,"screenHeight");

        if(parent!=null)
        {
            gl.glUniform1f(id_screenWidth, surfaceWidth);
            gl.glUniform1f(id_screenHeight, surfaceHeight);

            gl.glUniform1f(id_hasClipping, 1.0f);
            if(text.length()>0)
            {
                /*System.out.println(
                    "text:" + text +
                    ",pT:" + parent.style.totalActualTop +
                    ",NormPT:" + (parent.style.totalActualTop+1f) +
                    ",y0:" + ((parent.style.totalActualTop+1f)) +
                    ",y1:" + (-1f*((parent.style.totalActualTop-1f) + parent.style.actualHeight))
                );*/
            }
            gl.glUniform1f(id_clipX0, utilities.getActualLeftX(parent.style.totalActualLeft));
            gl.glUniform1f(id_clipY0, utilities.getActualTopToY(parent.style.totalActualTop));
            gl.glUniform1f(id_clipX1, utilities.getActualLeftX(parent.style.totalActualLeft)+utilities.getActualWidthToX(parent.style.actualWidth));
            gl.glUniform1f(id_clipY1, utilities.getActualTopToY(parent.style.totalActualTop)+utilities.getActualHeightToY(parent.style.actualHeight));

        }
        else {
            gl.glUniform1f(id_hasClipping, 0.0f);
        }


        float[] vertices = {
                -1f,1f,0.0f, //left
                -1f,1f-normHeight,0.0f, //bottom left
                -1f+normWidth,1f,0.0f, //top right
                -1f+normWidth,1f-normHeight,0.0f, //bottom right
        };

        // Observe that the vertex data passed to glVertexAttribPointer must stay valid
        // through the OpenGL rendering lifecycle.
        // Therefore it is mandatory to allocate a NIO Direct buffer that stays pinned in memory
        // and thus can not get moved by the java garbage collector.
        // Also we need to keep a reference to the NIO Direct buffer around up untill
        // we call glDisableVertexAttribArray first then will it be safe to garbage collect the memory.
        // I will here use the com.jogamp.common.nio.Buffers to quicly wrap the array in a Direct NIO buffer.
        FloatBuffer fbVertices = Buffers.newDirectFloatBuffer(vertices);

        // Select the VBO, GPU memory data, to use for vertices
        gl.glBindBuffer(GL2ES2.GL_ARRAY_BUFFER, vboVertices);

        // transfer data to VBO, this perform the copy of data from CPU -> GPU memory
        numBytes = vertices.length * 4; //4 because RGBA
        gl.glBufferData(GL2ES2.GL_ARRAY_BUFFER, numBytes, fbVertices, GL2ES2.GL_STATIC_DRAW);
        fbVertices = null; // It is OK to release CPU vertices memory after transfer to GPU
        vertices=null;

        // Associate Vertex attribute 0 with the last bound VBO
        gl.glVertexAttribPointer(0 /* the vertex attribute */, 3,
                GL2ES2.GL_FLOAT, true /* normalized? */, 0 /* stride */,
                0 /* The bound VBO data offset */);

        // VBO
        // gl.glBindBuffer(GL2ES2.GL_ARRAY_BUFFER, 0); // You can unbind the VBO after it have been associated using glVertexAttribPointer

        gl.glEnableVertexAttribArray(0);

        gl.glEnable (GL2ES2.GL_BLEND);
        gl.glBlendFunc (GL2ES2.GL_SRC_ALPHA, GL2ES2.GL_ONE_MINUS_SRC_ALPHA);


        float[] colors;
        if(style.foregroundColor!=null)
        {
            float color_red=style.foregroundColor.getRed()/255f;
            float color_green=style.foregroundColor.getGreen()/255f;
            float color_blue=style.foregroundColor.getBlue()/255f;
            float color_alpha=style.foregroundColor.getAlpha()/255f;
            colors = new float[]{
                    color_red, color_green, color_blue, color_alpha,
                    color_red, color_green, color_blue, color_alpha,
                    color_red, color_green, color_blue, color_alpha,

                    color_red, color_green, color_blue, color_alpha
            };
        }
        else {
            //todo: gradients
            float color_red=0/255f;
            float color_green=0/255f;
            float color_blue=0/255f;
            float color_alpha=0/255f;
            colors = new float[]{
                    color_red, color_green, color_blue, color_alpha,
                    color_red, color_green, color_blue, color_alpha,
                    color_red, color_green, color_blue, color_alpha,

                    color_red, color_green, color_blue, color_alpha
            };
        }

        FloatBuffer fbColors = Buffers.newDirectFloatBuffer(colors);

        // Select the VBO, GPU memory data, to use for colors
        gl.glBindBuffer(GL2ES2.GL_ARRAY_BUFFER, vboColors);
        numBytes = colors.length * 4;
        gl.glBufferData(GL2ES2.GL_ARRAY_BUFFER, numBytes, fbColors, GL2ES2.GL_STATIC_DRAW);
        fbColors = null; // It is OK to release CPU color memory after transfer to GPU
        colors=null;

        gl.glVertexAttribPointer(1 /* the vertex attribute */, 4 /* four positions used for each vertex */,
                GL2ES2.GL_FLOAT, true /* normalized? */, 0 /* stride */,
                0 /* The bound VBO data offset */);

        gl.glEnableVertexAttribArray(1);



        int[] indices = {0,1,2, 1,2,3};
        IntBuffer fbIndices = Buffers.newDirectIntBuffer(indices);
        gl.glBindBuffer(GL2ES2.GL_ELEMENT_ARRAY_BUFFER, vboIndices);
        numBytes = indices.length * 4; //32bit int?
        gl.glBufferData(GL2ES2.GL_ELEMENT_ARRAY_BUFFER, numBytes, fbIndices, GL2ES2.GL_STATIC_DRAW);
        fbIndices=null;
        indices=null;

        if((style.backgroundImage!=null || style.backgroundBufferedImage!=null) && texture!=null)
        {
            gl.glEnable(GL2ES2.GL_TEXTURE);
            gl.glBindTexture(GL2ES2.GL_TEXTURE_2D,texture.textureID.get(0));
            float[] textcoords = {
                    0,0, //left
                    0,1f, //bottom left
                    1f,0, //top right
                    1f,1f, //bottom right
            };

            FloatBuffer fbTextCoords = Buffers.newDirectFloatBuffer(textcoords);

            // Select the VBO, GPU memory data, to use for colors
            gl.glBindBuffer(GL2ES2.GL_ARRAY_BUFFER, vboCoords);
            numBytes = textcoords.length * 4;
            gl.glBufferData(GL2ES2.GL_ARRAY_BUFFER, numBytes, fbTextCoords, GL2ES2.GL_STATIC_DRAW);
            fbTextCoords=null;
            textcoords=null;
            gl.glVertexAttribPointer(2 /* the vertex attribute */, 2 /* x and y */,
                    GL2ES2.GL_FLOAT, true /* normalized? */, 0 /* stride */,
                    0 /* The bound VBO data offset */);
            gl.glEnableVertexAttribArray(2);


            int texId=gl.glGetUniformLocation(shaderProgram,"texture");
            gl.glActiveTexture(GL2ES2.GL_TEXTURE0);
            gl.glUniform1i(texId,0);

            int id_hasTexture=gl.glGetUniformLocation(shaderProgram,"hasTexture");
            gl.glUniform1f(id_hasTexture, 1.0f);
        }
        else {
            int id_hasTexture=gl.glGetUniformLocation(shaderProgram,"hasTexture");
            gl.glUniform1f(id_hasTexture, 0.0f);
        }

        int id_opacity=gl.glGetUniformLocation(shaderProgram,"opacity");
        gl.glUniform1f(id_opacity, style.backgroundImageOpacity);

        //gl.glDrawArrays(GL2ES2.GL_TRIANGLES, 0, 6); //Draw the vertices as triangle
        gl.glDrawElements(GL2ES2.GL_TRIANGLES, 6, GL2ES2.GL_UNSIGNED_INT,0); //Draw the vertices as triangle

        gl.glDisableVertexAttribArray(0); // Allow release of vertex position memory
        gl.glDisableVertexAttribArray(1); // Allow release of vertex color memory
        gl.glDisableVertexAttribArray(2); //release texture memory
        //gl.glDisableVertexAttribArray(3); //release stencil buffer memory


        gl.glDeleteBuffers(5, vboHandles, 0); // Release VBO, color and vertices, buffer GPU memory.

        if(style.top<0)
        {
            gl.glDisable(GL2ES2.GL_STENCIL_TEST);
        }

        //rendering children
        renderChildren(drawable,ModelViewProjectionMatrix_location,shaderProgram,glishRenderer,delta);
    }


    @Override
    String generateJava(StunningUI_Generator generator,StringBuilder stringBuilder, String parentVar) {
        return generator.serialize(this,stringBuilder,parentVar);
    }

    @Override
    JsonObject save() {
        JsonObject obj=new JsonObject();
        obj.addProperty("tag",tag);
        obj.addProperty("type",StunningRenderer.spriteClassRectangle);
        obj.addProperty("text",text);
        obj.addProperty("state",state);
        obj.addProperty("id",id);
        JsonArray childrenObjects=new JsonArray();
        for(int i=0;i<children.size();i++)
        {
            childrenObjects.add(children.get(i).save());
        }
        obj.add("children",childrenObjects);
        obj.add("style",this.style.save());
        return obj;
    }

    static StunningUI_Sprite load(JsonObject jsonObject,StunningUI_Sprite defaultParent){
        StunningUI_Rectangle rect=new StunningUI_Rectangle(defaultParent);
        rect.style=StunningUI_Style.load(jsonObject.get("style").getAsJsonObject());
        if(jsonObject.has("tag"))
        {
            rect.tag=jsonObject.get("tag").getAsString();
        }
        if(jsonObject.has("text"))
        {
            rect.text=jsonObject.get("text").getAsString();
        }
        if(jsonObject.has("state"))
        {
            rect.state=jsonObject.get("state").getAsInt();
        }
        if(jsonObject.has("id"))
        {
            rect.id=jsonObject.get("id").getAsDouble();
        }
        if(jsonObject.has("children"))
        {
            JsonArray childrenArr=jsonObject.get("children").getAsJsonArray();
            for(int i=0;i<childrenArr.size();i++)
            {
                JsonObject childJson=childrenArr.get(i).getAsJsonObject();
                if(childJson.get("type").getAsString().equals(StunningRenderer.spriteClassRectangle))
                {
                    rect.addChild(StunningUI_Rectangle.load(jsonObject,rect));
                }
                else
                {
                    rect.addChild(StunningUI_Sprite.load(jsonObject,rect));
                }
            }
        }
        return rect;
    }

    private void markAsChanged(){
        changed=true;
    }

}
