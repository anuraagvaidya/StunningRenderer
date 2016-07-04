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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Anuraag on 7/3/2016.
 */
public class StunningWindow{
    Panel design_canvasParent;
    private StunningRenderer renderer;
    private StunningUI_Rectangle container;
    Frame design_frame;
    ConcurrentHashMap<String,StunningUI_Rectangle> tools;
    ConcurrentHashMap<Double,String> toolsIDs;
    private StunningUI_FontEngine fontEngine;
    private Pattern hexColorPattern;
    private final CopyOnWriteArrayList<String> responses;
    String surround(String str)
    {
        return "\"" + str + "\"";
    }
    String keyValueStr(String key,String value)
    {
        return surround(key) + ":" + surround(value);
    }
    String id;

    StunningWindow(String id, final CopyOnWriteArrayList<String> responses) {
        this.id=id;
        this.responses=responses;
        tools=new ConcurrentHashMap<String, StunningUI_Rectangle>();
        toolsIDs=new ConcurrentHashMap<Double, String>();
        fontEngine=new StunningUI_FontEngine();
        String hexColorPatternStr = "#[a-fA-f0-8]+";
        hexColorPattern = Pattern.compile(hexColorPatternStr);
        renderer=new StunningRenderer(new RendererCallback() {
            public void init(GLAutoDrawable drawable) {
                Display.getDefault().syncExec(new Runnable() {
                    public void run() {
                        responses.add("{" + keyValueStr("status","Renderer is ready") + "}");
                        container.style.width=renderer.getCanvas().getSurfaceWidth();
                        container.style.height=renderer.getCanvas().getSurfaceHeight();
                    }
                });
            }
            public void render(GLAutoDrawable drawable, long delta) {
//                System.out.println("Renderering " + delta);
            }
            public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
                container.style.left=0;
                container.style.top=0;
                container.style.width=renderer.getCanvas().getSurfaceWidth();
                container.style.height=renderer.getCanvas().getSurfaceHeight();
            }
            public void dispose(GLAutoDrawable drawable) {
            }
        },"StunningRenderer1",30);

        container=new StunningUI_Rectangle(renderer.mainLayer);

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
    ArrayList<StunningUI_Sprite> findSpriteUnderCoords(int x, int y){
        return renderer.collisionMap.findComponents(x,y);
    }
    void raiseMouseEvent(String eventName,MouseEvent e)
    {
        synchronized (responses)
        {
            ArrayList<StunningUI_Sprite> sprites=findSpriteUnderCoords(e.getX(),e.getY());
            if(sprites!=null)
            {
                for(int i=0;i<sprites.size();i++)
                {
                    if(toolsIDs.containsKey(sprites.get(i).id))
                    {
                        responses.add("{" + keyValueStr("event",eventName) + "," + keyValueStr("windowId",StunningWindow.this.id) + "," + keyValueStr("toolId",toolsIDs.get(sprites.get(i).id)) + "," + keyValueStr("x",e.getX() + "") + "," + keyValueStr("y",e.getY() + "")  + "," + keyValueStr("clickCount",e.getClickCount() + "") + "}");
                    }
                }
            }
        }
    }
    void raiseKeyEvent(String eventName,KeyEvent e)
    {
        synchronized (responses)
        {
            responses.add("{" + keyValueStr("event",eventName) + "," + keyValueStr("windowId",StunningWindow.this.id) + "," + keyValueStr("keyCode",e.getKeyCode() + "") + "," + keyValueStr("alt",e.isAltDown() + "") + "," + keyValueStr("shift",e.isShiftDown() + "") + "," + keyValueStr("ctrl",e.isControlDown() + "") + "}");
        }
    }
    void attachEvent(String toolId, String event)
    {
        StunningUI_Rectangle rect=tools.get(toolId);
        renderer.collisionMap.updateComponent(rect);

        if(event.equals("click"))
        {
            renderer.getCanvas().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    super.mouseClicked(e);
                    raiseMouseEvent("click",e);
                }
            });
        }
        else if(event.equals("mouseenter"))
        {
            renderer.getCanvas().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e)
                {
                    super.mouseEntered(e);
                    raiseMouseEvent("mouseenter",e);
                }
            });
        }
        else if(event.equals("mouseexit"))
        {
            renderer.getCanvas().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e)
                {
                    super.mouseExited(e);
                    raiseMouseEvent("mouseexit",e);
                }
            });
        }
        else if(event.equals("mousedown"))
        {
            renderer.getCanvas().addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    super.mousePressed(e);
                    raiseMouseEvent("mousedown",e);
                }
            });
        }
        else if(event.equals("mouseup"))
        {
            renderer.getCanvas().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    super.mouseReleased(e);
                    raiseMouseEvent("mouseup",e);
                }
            });
        }
        else if(event.equals("drag"))
        {
            renderer.getCanvas().addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    super.mouseDragged(e);
                    raiseMouseEvent("drag",e);
                }
            });
        }
        else if(event.equals("move"))
        {
            renderer.getCanvas().addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    super.mouseMoved(e);
                    raiseMouseEvent("move",e);
                }
            });
        }
        else if(event.equals("keypress"))
        {
            renderer.getCanvas().addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    super.keyTyped(e);
                    raiseKeyEvent("keypress",e);
                }
            });
        }
        else if(event.equals("keydown"))
        {
            renderer.getCanvas().addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    super.keyPressed(e);
                    raiseKeyEvent("keydown",e);
                }
            });
        }
        else if(event.equals("keyup"))
        {
            renderer.getCanvas().addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    super.keyReleased(e);
                    raiseKeyEvent("keyup",e);
                }
            });
        }
    }
    void setToolStyle(String id, String paramAndValuesStr)
    {
        boolean hasText=false,textChanged=false;
        StunningUI_Rectangle rect=tools.get(id);
        String[] paramAndValues=paramAndValuesStr.split("&");
        for(int j=0;j<paramAndValues.length;j++) {
            String[] paramAndValue = paramAndValues[j].split("=");
            String param = paramAndValue[0];
            String value = paramAndValue[1];
            value = value.replaceAll("\\$", " ");
            if(param.equals("width"))
            {
                rect.style.width=Integer.parseInt(value);
            }
            else if(param.equals("height"))
            {
                rect.style.height=Integer.parseInt(value);
            }
            else if(param.equals("left"))
            {
                rect.style.left=Integer.parseInt(value);
            }
            else if(param.equals("top"))
            {
                rect.style.top=Integer.parseInt(value);
            }
            else if(param.equals("foregroundColor"))
            {
                Matcher m=hexColorPattern.matcher(value);
                if(m.find())
                {
                    rect.style.foregroundColor=Color.decode(value);
                }
                else
                {
                    //Must be a comma separated rgba
                    String[] rgba=value.split(",");
                    int r=rgba.length>0?Integer.parseInt(rgba[0]):0;
                    int g=rgba.length>1?Integer.parseInt(rgba[1]):0;
                    int b=rgba.length>2?Integer.parseInt(rgba[2]):0;
                    int a=rgba.length>3?Integer.parseInt(rgba[3]):255;
                    rect.style.foregroundColor=new Color(r,g,b,a);
                }
            }
            else if(param.equals("text"))
            {
                if(!value.equals(rect.text))
                {
                    textChanged=true;
                }
                rect.text=value;
                hasText=true;
            }
            else if(param.equals("fontStyle"))
            {
                if(value.equals("plain")){ rect.style.fontStyle=Font.PLAIN; }
                if(value.equals("bold")){ rect.style.fontStyle=Font.BOLD; }
                if(value.equals("italic")){ rect.style.fontStyle=Font.ITALIC; }
                if(value.equals("boldItalic")){ rect.style.fontStyle=Font.BOLD | Font.ITALIC; }
            }
            else if(param.equals("fontName"))
            {
                rect.style.fontName=value;
            }
            else if(param.equals("fontSize"))
            {
                rect.style.fontSize=Integer.parseInt(value);
            }
            else if(param.equals("textColor"))
            {
                Matcher m=hexColorPattern.matcher(value);
                if(m.find())
                {
                    rect.style.textColor=Color.decode(value);
                }
                else
                {
                    //Must be a comma separated rgba
                    String[] rgba=value.split(",");
                    int r=rgba.length>0?Integer.parseInt(rgba[0]):0;
                    int g=rgba.length>1?Integer.parseInt(rgba[1]):0;
                    int b=rgba.length>2?Integer.parseInt(rgba[2]):0;
                    int a=rgba.length>3?Integer.parseInt(rgba[3]):255;
                    rect.style.textColor=new Color(r,g,b,a);
                }
            }
            else if(param.equals("backgroundImageOpacity"))
            {
                rect.style.backgroundImageOpacity=Float.parseFloat(value);
            }
            else if(param.equals("zoom"))
            {
                rect.style.zoom=Float.parseFloat(param);
            }
            else if(param.equals("zoomLeft"))
            {
                rect.style.zoomLeft=Integer.parseInt(param);
            }
            else if(param.equals("zoomTop"))
            {
                rect.style.zoomTop=Integer.parseInt(param);
            }
        }

        if(hasText)
        {

            rect.style.backgroundRepeat=StunningUI_Style.BG_REPEAT_NO_REPEAT;
            Font font=new Font(rect.style.fontName,rect.style.fontStyle,rect.style.fontSize);

            fontEngine.setFontBitmap(
                    rect,
                    font,
                    rect.text,
                    500,
                    10,
                    0,
                    0
            );
            rect.style.width=rect.style.backgroundBufferedImage.getWidth();
            rect.style.height=rect.style.backgroundBufferedImage.getHeight();

        }
        if(textChanged)
        {
            rect.forceInit();
        }
        renderer.forceRender();
    }
    void addRectangle(String id,String parentId,String paramAndValuesStr){
        StunningUI_Rectangle parent=parentId.equals("")?container:tools.get(parentId);
        final StunningUI_Rectangle rect=new StunningUI_Rectangle(parent);
        tools.put(id,rect);
        toolsIDs.put(rect.id,id);
        setToolStyle(id,paramAndValuesStr);
    }

}
