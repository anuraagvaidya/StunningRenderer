package com.bysness.stunningrenderer;
import com.google.gson.JsonObject;
import com.jogamp.opengl.GLAutoDrawable;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Anuraag on 4/29/2016.
 */

interface SpriteEventInterface {
    void mouseClicked(MouseEvent e);
    void mousePressed(MouseEvent e);
    void mouseReleased(MouseEvent e);
    void mouseEntered(MouseEvent e);
    void mouseExited(MouseEvent e);
    void keyPressed(KeyEvent e);
    void keyDown(KeyEvent e);
    void keyUp(KeyEvent e);
}
class SpriteEvent implements SpriteEventInterface {
    SpriteEvent(){

    }

    public void mouseClicked(MouseEvent e) {

    }

    public void mousePressed(MouseEvent e) {

    }

    public void mouseReleased(MouseEvent e) {

    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }

    public void keyPressed(KeyEvent e) {

    }

    public void keyDown(KeyEvent e) {

    }

    public void keyUp(KeyEvent e) {

    }
}

class StunningUI_Sprite {
    static int STATE_VISIBLE=0;
    static int STATE_INVISIBLE=1;

    boolean changed=false;
    int state=STATE_VISIBLE;
    int type=0;
    String tag="";
    String text="";
    protected volatile boolean initialized=false;
    StunningUI_Sprite parent=null;
    ArrayList<StunningUI_Sprite> children;
    HashMap<String,Object> extras;
    StunningUI_Style style;
    double id;
    boolean listensToMouse=false;
    boolean listensToKeyboard=false;
    boolean listensToTouch=false;
    StunningUI_TextInfo textInfo;
    ArrayList<Integer> tiles;
    ArrayList<SpriteEvent> events=null;

    public StunningUI_Sprite(StunningUI_Sprite parent) {
        children=new ArrayList<StunningUI_Sprite>();
        id=System.nanoTime();
        tiles=new ArrayList<Integer>();
        style=new StunningUI_Style();
        if(parent!=null)
        {
            this.parent=parent;
            this.style.zoom=this.parent.style.zoom;
            this.style.zoomLeft=this.parent.style.zoomLeft;
            this.style.zoomTop=this.parent.style.zoomTop;
            this.parent.addChild(this);
        }
    }

    void addSpriteEventListener(SpriteEvent event){
        if(events==null)
        {
            events=new ArrayList<SpriteEvent>();
        }
        events.add(event);
    }
    void removeSpriteEventListener(SpriteEvent event){
        if(events!=null)
        {
            events.remove(event);
        }
    }

    void hide(){
        state=STATE_INVISIBLE;
    }

    void show(){
        state=STATE_VISIBLE;
    }

    void toggle(){
        state=state==STATE_INVISIBLE?STATE_VISIBLE:STATE_INVISIBLE;
    }

    void initExtras(){
        extras=new HashMap<String,Object>();
    }

    void addChild(StunningUI_Sprite sprite){
        children.add(sprite);
    }

    void forceInit(){
        initialized=false;
    }
    void init(final GLAutoDrawable drawable, int ModelViewProjectionMatrix_location,int shaderProgram, StunningRenderer glishRenderer){
        initialized=true;
    }
    void render(final GLAutoDrawable drawable, int ModelViewProjectionMatrix_location,int shaderProgram, StunningRenderer glishRenderer, long delta){
        if(!initialized)
        {
            this.init(drawable,ModelViewProjectionMatrix_location,shaderProgram,glishRenderer);
        }
    }
    int getChildIndex()
    {
        if(parent==null)
        {
            return 0;
        }
        else {
            for(int i=0;i<parent.children.size();i++)
            {
                if(parent.children.get(i).id==this.id)
                {
                    return i;
                }
            }
            return 0;
        }
    }
    int calculateZIndex(){
        if(parent==null)
        {
            return 1;
        }
        else {
            return parent.calculateZIndex() + getChildIndex() + 1;
        }
    }
    void renderChildren(final GLAutoDrawable drawable, int ModelViewProjectionMatrix_location,int shaderProgram, StunningRenderer glishRenderer, long delta){
        if(children!=null && state!=STATE_INVISIBLE)
        {
            for(int i=0;i<children.size();i++)
            {
                children.get(i).render(drawable,ModelViewProjectionMatrix_location,shaderProgram,glishRenderer,delta);
            }
        }
    }
    void dispose(final GLAutoDrawable drawable){

    }
    void reshape(final GLAutoDrawable drawable, final int x, final int y, final int width, final int height){

    }
    void reshapeChildren(final GLAutoDrawable drawable, final int x, final int y, final int width, final int height){
        if(children!=null && state!=STATE_INVISIBLE)
        {
            for(int i=0;i<children.size();i++)
            {
                children.get(i).reshape(drawable,x,y,width,height);
            }
        }
    }

    JsonObject save(){
        return null;
    }

    static StunningUI_Sprite load(JsonObject jsonObject, StunningUI_Sprite defaultParent){
        return null;
    }

    String generateJava(StunningUI_Generator generator,StringBuilder stringBuilder, String variable)
    {
        //Generate Java code for this one
        return "";
    }
    void generateJavaScript(StunningUI_Generator generator,StringBuilder stringBuilder)
    {

    }
}
