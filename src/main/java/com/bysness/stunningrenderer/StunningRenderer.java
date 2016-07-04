package com.bysness.stunningrenderer;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Anuraag on 4/29/2016.
 *
 */
interface RendererCallback {
    void init(GLAutoDrawable drawable);
    void render(GLAutoDrawable drawable,long delta);
    void reshape(GLAutoDrawable drawable, int x, int y, int width, int height);
    void dispose(GLAutoDrawable drawable);
}

interface AfterNextRender {
    void render(GLAutoDrawable drawable,long delta);
}

class StunningRenderer implements GLEventListener {
    //----------------------------------------------------
    //variables related to OpenGL
    private GLProfile glp;
    private GLCapabilities caps;
    private GLCanvas canvas;
    private FPSAnimator animator;
    StunningUI_Program glishUIProgram;
    StunningUI_Utilities utilities;

    private static String spriteClass="type";
    static String spriteClassRectangle="rect";

    StunningUI_CollisionMap collisionMap;
    HashMap<String,Font> loadedFonts;
    StunningUI_FontEngine fontEngine;
    StunningUI_Rectangle mainLayer;

    RendererCallback rendererCallback;
    ArrayList<AfterNextRender> afterNextRenderArrayList;

    //Current Session
    float zoom=1f;

    private int ModelViewProjectionMatrix_location;
    private int renderCalled;
    private int framerate=30;
    private String rendererID;

    boolean log=false;
    boolean unlimited=false;

    StunningRenderer(RendererCallback renderAgent, String rendererID, int framerate) {
        glp = GLProfile.getDefault();
        caps = new GLCapabilities(glp);
        caps.setStencilBits(8);

        canvas = new GLCanvas(caps);

        canvas.addGLEventListener(this);
        addListeners();
        glishUIProgram=StunningUI_Program.getInstance();

        utilities=StunningUI_Utilities.getInstance();

        mainLayer=new StunningUI_Rectangle(null);
        mainLayer.style.foregroundColor=new Color(255,255,255,255);
        System.setProperty("sun.awt.noerasebackground", "true");

        collisionMap=new StunningUI_CollisionMap();
        loadedFonts=new HashMap<String, Font>();
        fontEngine=new StunningUI_FontEngine();
        rendererCallback=renderAgent;
        afterNextRenderArrayList=new ArrayList<AfterNextRender>();

        renderCalled=0;
        this.rendererID=rendererID;
        this.framerate=framerate;
    }

    void addAfterNextRender(AfterNextRender afterNextRender){
        afterNextRenderArrayList.add(afterNextRender);
    }

    void addListeners(){
        canvas.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                ArrayList<StunningUI_Sprite> spritesUnderMouse=collisionMap.findComponents(e.getX(),e.getY());
                StunningUI_Sprite sp=utilities.getTopMostSprite(spritesUnderMouse);

                int notches = e.getWheelRotation();
                String message="";
                if(sp!=null)
                {
                    int maxTopAndHeight=0;
                    int maxTop=0;
                    int leastTop=Integer.MAX_VALUE;
                    for(int i=0;i<sp.parent.children.size();i++)
                    {
                        if(sp.parent.children.get(i).style.top>maxTop)
                        {
                            maxTop=sp.parent.children.get(i).style.top;
                            maxTopAndHeight=maxTop+sp.parent.children.get(i).style.height;
                        }
                        if(sp.parent.children.get(i).style.top<leastTop)
                        {
                            leastTop=sp.parent.children.get(i).style.top;
                        }
                    }
                    if (notches < 0) {
                        message = "Mouse wheel moved UP "
                                + -notches + " notch(es)" + "\n";
//                        System.out.println("leastTop:" + leastTop);

                        if(
                                sp.parent.style.overflowY &&
                                        sp.parent.style.scrollYVisible &&
                                        sp.parent.style.scrollTop<=0 &&
                                        (sp.parent.style.scrollTop+(e.getScrollAmount()*2))<=0
                                )
                        {

                            for(int i=0;i<sp.parent.children.size();i++)
                            {
                                sp.parent.style.scrollTop+=e.getScrollAmount()*2;
                            }
                        }
                    }
                    else if(notches > 0)
                    {
                        message = "Mouse wheel moved Down "
                            + notches + " notch(es)" + "\n";
//                        System.out.println("maxTopAndHeight:" + maxTopAndHeight + ", parentHeight:" + sp.parent.style.height);
                        if(
                                sp.parent.style.overflowY &&
                                sp.parent.style.scrollYVisible &&
                                sp.parent.style.scrollTop+maxTopAndHeight>=sp.parent.style.height &&
                                (sp.parent.style.scrollTop-(e.getScrollAmount()*2))+maxTopAndHeight>=sp.parent.style.height
                        )
                        {
                            for(int i=0;i<sp.parent.children.size();i++)
                            {
                                sp.parent.style.scrollTop-=e.getScrollAmount()*2;
                            }
                        }
                    }
                    forceRender();
                }
                message += "    Scroll type: WHEEL_UNIT_SCROLL" + "\n";
                message += "    Scroll amount: " + e.getScrollAmount()
                        + " unit increments per notch" + "\n";
                message += "    Units to scroll: " + e.getUnitsToScroll()
                        + " unit increments" + "\n";
//                System.out.println("Scroll event:" + message);

            }
        });
    }

    GLCanvas getCanvas() {
        return canvas;
    }

    void start()
    {
        animator = new FPSAnimator(canvas, framerate);
        animator.start();
    }
    void reset()
    {
        animator.stop();
    }
    long lastTime = System.currentTimeMillis();
    int shaderProgram=0;
    public void init(GLAutoDrawable drawable) {
        GL2ES2 gl = drawable.getGL().getGL2ES2();

        /*System.out.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
        System.out.println("INIT GL IS: " + gl.getClass().getName());
        System.out.println("GL_VENDOR: " + gl.glGetString(GL.GL_VENDOR));
        System.out.println("GL_RENDERER: " + gl.glGetString(GL.GL_RENDERER));
        System.out.println("GL_VERSION: " + gl.glGetString(GL.GL_VERSION));*/

        //Get a id number to the uniform_Projection matrix
        //so that we can update it.
        //shaderProgram=glishUIProgram.compileProgramWithoutTexture(gl);
        shaderProgram=glishUIProgram.compileProgramWithTexture(gl);
        ModelViewProjectionMatrix_location = gl.glGetUniformLocation(shaderProgram, "uniform_Projection");

        /* GL2ES2 also includes the intersection of GL3 core
         * GL3 core and later mandates that a "Vector Buffer Object" must
         * be created and bound before calls such as gl.glDrawArrays is used.
         * The VBO lines in this demo makes the code forward compatible with
         * OpenGL 3 and ES 3 core and later where a default
         * vector buffer object is deprecated.
         *
         * Generate two VBO pointers / handles
         * VBO is data buffers stored inside the graphics card memory.
         */
        mainLayer.style.width=drawable.getSurfaceWidth();
        mainLayer.style.height=drawable.getSurfaceHeight();
        collisionMap.create(drawable);
        rendererCallback.init(drawable);

        mainLayer.init(drawable,ModelViewProjectionMatrix_location,shaderProgram,this);

    }

    public void printLog(String str){
        if(log)
        {
            System.out.println(str);
        }
    }
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        /*System.out.println("Window resized to canvasWidth=" + z + " canvasHeight=" + h);
        canvasWidth = z;
        canvasHeight = h;

        // Get gl
        GL2ES2 gl = drawable.getGL().getGL2ES2();

        // Optional: Set viewport
        // Render to a square at the center of the window.
        gl.glViewport((canvasWidth-canvasHeight)/2,0,canvasHeight,canvasHeight);*/

        // Get gl
        GL2ES2 gl = drawable.getGL().getGL2ES2();
        gl.glViewport(x,y,width,height);
        mainLayer.style.width=width;
        mainLayer.style.height=height;
        rendererCallback.reshape(drawable,x,y,width,height);
        collisionMap.recreate(drawable);
        printLog("Resized viewport: w" + canvas.getSurfaceWidth() + ", h:" + canvas.getSurfaceHeight());
        forceRender(-1);
    }

    public void dispose(GLAutoDrawable drawable) {
        // put your cleanup code here
        //printLog("dispose");

        printLog("cleanup, remember to release shaders");
        GL2ES2 gl = drawable.getGL().getGL2ES2();
        rendererCallback.dispose(drawable);
        glishUIProgram.disposeProgram(gl);
        mainLayer.dispose(drawable);
        collisionMap.dispose();
    }

    public void forceRender(){
        //Todo: make thread-safe
        renderCalled=0;
//        printLog(Thread.currentThread().getName() + " old1 renderCalled=" + renderCalled);
    }
    public void forceRender(int step){
        //Todo: make thread-safe
        renderCalled=step;
//        printLog(Thread.currentThread().getName() + " old2 renderCalled=" + renderCalled);
    }

    public void display(GLAutoDrawable drawable) {
        // put your drawing code here
        //printLog("display");
        /*update();
        render(glAutoDrawable);*/
        long delta=System.currentTimeMillis()-lastTime;
        //-----------------------------------------
        // Update variables used in animation
//        printLog(rendererID + " " + Thread.currentThread().getName() + " render - " + renderCalled);
        if(renderCalled<2 || unlimited)
        {
            rendererCallback.render(drawable,delta);
            // Get gl
            GL2ES2 gl = drawable.getGL().getGL2ES2();

            gl.glClearStencil(0x0);
            // Clear screen
            gl.glClearColor(1f, 1f, 1f, 1f);  // Purple
            gl.glClear(GL2ES2.GL_STENCIL_BUFFER_BIT |
                    GL2ES2.GL_COLOR_BUFFER_BIT   |
                    GL2ES2.GL_DEPTH_BUFFER_BIT   );


            // Use the shaderProgram that got linked during the init part.
            gl.glUseProgram(shaderProgram);
            mainLayer.render(drawable, ModelViewProjectionMatrix_location, shaderProgram,this,delta);
            renderCalled++;
            printLog(rendererID + " " + Thread.currentThread().getName() + " render called " + renderCalled);

            lastTime = System.currentTimeMillis();

            int afterNextRenderArrayListSize=afterNextRenderArrayList.size();
            for(int i=0;i<afterNextRenderArrayListSize;i++)
            {
                afterNextRenderArrayList.get(i).render(drawable,delta);
                afterNextRenderArrayList.remove(i);
            }
        }
        else
        {
//            printLog(rendererID + " " + Thread.currentThread().getName() + " renderCallback is 2 or greater");
        }
    }
}
