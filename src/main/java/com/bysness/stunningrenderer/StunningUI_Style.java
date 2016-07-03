package com.bysness.stunningrenderer;

import com.google.gson.JsonObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;

import java.awt.*;
import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Created by Anuraag on 5/2/2016.
 * Stores style settings for each sprite
 */
class StunningUI_Style {
    static int POSITION_STATIC=0;
    static int POSITION_RELATIVE=1;
    static int POSITION_ABSOLUTE=2;
    static int POSITION_FIXED=3;

    static int BG_REPEAT_REPEAT_X=0;
    static int BG_REPEAT_REPEAT_Y=1;
    static int BG_REPEAT_REPEAT_BOTH=2;
    static int BG_REPEAT_NO_REPEAT=3;

    Color selectionColor=null;
    Color selectionTextColor=null;
    Color foregroundColor=null;
    Color textColor=Color.black;
    Color backgroundColor;
    String backgroundImage;
    BufferedImage backgroundBufferedImage;
    int backgroundRepeat=BG_REPEAT_REPEAT_BOTH;
    float backgroundImageOpacity=1.0f;
    int position=StunningUI_Style.POSITION_STATIC; //position like in CSS
    int width=0;
    int height=0;
    float actualWidth=0;
    float actualHeight=0;
    float maxWidth=0;
    float maxHeight=0;
    int left=0;
    int top=0;
    int totalLeft=0;
    int totalTop=0;
    float totalActualLeft=0;
    float totalActualTop=0;
    float actualLeft=0;
    float actualTop=0;
    float depth=0;
    int fontSize=20;
    boolean overflowX=false;
    boolean overflowY=true;
    boolean scrollXVisible=false;
    boolean scrollYVisible=true;
    int scrollTop=0;
    float zoom=1f;
    int zoomLeft=0;
    int zoomTop=0;

    Color Color_SWTToAWT(org.eclipse.swt.graphics.Color swtColor){
        return new Color(swtColor.getRed(),swtColor.getGreen(),swtColor.getBlue());
    }
    StunningUI_Style(){
//        Display display = Display.getCurrent();
        selectionColor=Color.BLUE;
        selectionTextColor=Color.WHITE;
//        selectionColor=Color_SWTToAWT(display.getSystemColor(SWT.COLOR_LIST_SELECTION));
//        selectionTextColor=Color_SWTToAWT(display.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
    }

    String fontName="SansSerif";

    JsonObject save(){
        JsonObject json=new JsonObject();
        json.addProperty("width",width);
        json.addProperty("height",height);
        json.addProperty("left",left);
        json.addProperty("top",top);
        json.addProperty("backgroundImage",backgroundImage);
        json.addProperty("backgroundImageOpacity",backgroundImageOpacity);
        json.addProperty("fontName",fontName);
        json.addProperty("fontSize",fontSize);
        json.addProperty("foregroundColor",foregroundColor.getRed() + "," + foregroundColor.getGreen() + "," + foregroundColor.getBlue() + "," + foregroundColor.getAlpha());
        json.addProperty("overflowX",overflowX);
        json.addProperty("overflowY",overflowY);
        json.addProperty("scrollXVisible",scrollXVisible);
        json.addProperty("scrollYVisible",scrollYVisible);
        return json;
    }
    static StunningUI_Style load(JsonObject jsonObject){
        StunningUI_Style style=new StunningUI_Style();

        if (jsonObject.has("width")) {
            style.width=jsonObject.get("width").getAsInt();
        }
        if (jsonObject.has("height")) {
            style.height=jsonObject.get("height").getAsInt();
        }
        if (jsonObject.has("left")) {
            style.left=jsonObject.get("left").getAsInt();
        }
        if (jsonObject.has("top")) {
            style.top=jsonObject.get("top").getAsInt();
        }
        if (jsonObject.has("backgroundImage")) {
            style.backgroundImage=jsonObject.get("backgroundImage").getAsString();
        }
        if (jsonObject.has("backgroundImageOpacity")) {
            style.backgroundImageOpacity=jsonObject.get("backgroundImageOpacity").getAsFloat();
        }
        if (jsonObject.has("fontName")) {
            style.fontName=jsonObject.get("fontName").getAsString();
        }
        if (jsonObject.has("fontSize")) {
            style.fontSize=jsonObject.get("fontSize").getAsInt();
        }
        if (jsonObject.has("foregroundColor")) {
            String[] colorComponents=jsonObject.get("foregroundColor").getAsString().split(",");
            style.foregroundColor=new Color(Integer.parseInt(colorComponents[0]),Integer.parseInt(colorComponents[1]),Integer.parseInt(colorComponents[2]),Integer.parseInt(colorComponents[3]));
        }
        if (jsonObject.has("overflowX")) {
            style.overflowX=jsonObject.get("overflowX").getAsBoolean();
        }
        if (jsonObject.has("overflowY")) {
            style.overflowY=jsonObject.get("overflowY").getAsBoolean();
        }
        if (jsonObject.has("scrollXVisible")) {
            style.scrollXVisible=jsonObject.get("scrollXVisible").getAsBoolean();
        }
        if (jsonObject.has("scrollYVisible")) {
            style.scrollYVisible=jsonObject.get("scrollYVisible").getAsBoolean();
        }
        return style;
    }
}
