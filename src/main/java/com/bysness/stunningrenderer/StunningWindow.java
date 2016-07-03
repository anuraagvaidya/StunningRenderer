package com.bysness.stunningrenderer;

import com.jogamp.opengl.GLAutoDrawable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import swt.SWTResourceManager;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Anuraag on 7/3/2016.
 */
public class StunningWindow{
    Panel design_canvasParent;
    StunningRenderer renderer;
    Frame design_frame;
    ConcurrentHashMap<String,StunningUI_Rectangle> tools;
    StunningWindow() {
        tools=new ConcurrentHashMap<String, StunningUI_Rectangle>();
        renderer=new StunningRenderer(new RendererCallback() {
            public void init(GLAutoDrawable drawable) {
                Display.getDefault().syncExec(new Runnable() {
                    public void run() {
                        System.out.println("Renderer is ready");
                    }
                });
            }
            public void render(GLAutoDrawable drawable, long delta) {
//                System.out.println("Renderering " + delta);
            }
            public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
            }
            public void dispose(GLAutoDrawable drawable) {
            }
        },"StunningRenderer1",30);

        renderer.unlimited=true;

        design_frame = new Frame("Window Title");
        design_frame.add(renderer.getCanvas());
        design_frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                design_frame.remove(renderer.getCanvas());
                design_frame.dispose();
            }
        });
        design_frame.setSize(640,480);
        design_frame.setVisible(true);


        renderer.start();

    }

    void forceRender()
    {
        renderer.forceRender();
    }
    void addComponent(String id,String parentId){
        StunningUI_Rectangle parent=parentId.equals("")?renderer.mainLayer:tools.get(parentId);
        final StunningUI_Rectangle rect=new StunningUI_Rectangle(parent);
        rect.style.width=300;
        rect.style.height=200;
        rect.style.left=0;
        rect.style.top=0;
        Random rand = new Random();
        float r = rand.nextFloat() / 2f + 0.0f;
        float g = rand.nextFloat() / 2f + 0.0f;
        float b = rand.nextFloat() / 2f + 0.0f;

        rect.style.foregroundColor=new Color(r, g, b, 1f);

        tools.put(id,rect);
        renderer.forceRender();
    }

}
