package com.bysness.stunningrenderer;

import javax.swing.text.Style;
import java.awt.*;
import java.util.Random;

/**
 * Created by Anuraag on 5/7/2016.
 */
class RandomString {

    private static final char[] symbols;

    static {
        StringBuilder tmp = new StringBuilder();
        for (char ch = '0'; ch <= '9'; ++ch)
            tmp.append(ch);
        for (char ch = 'a'; ch <= 'z'; ++ch)
            tmp.append(ch);
        symbols = tmp.toString().toCharArray();
    }

    private final Random random = new Random();

    private final char[] buf;

    public RandomString(int length) {
        if (length < 1)
            throw new IllegalArgumentException("length < 1: " + length);
        buf = new char[length];
    }

    public String nextString() {
        for (int idx = 0; idx < buf.length; ++idx)
            buf[idx] = symbols[random.nextInt(symbols.length)];
        return new String(buf);
    }
}
public class StunningUI_Generator {
    RandomString randomString;
    StunningUI_Generator(){
        randomString=new RandomString(10);
    }

    String createObject(StringBuilder builder,String classname,String parentVariable){
        String variable="r"+randomString.nextString();
        builder.append(classname).append(" ").append(variable).append(" = new ").append(classname).append("(").append(parentVariable==null?"null":parentVariable).append(");\n");
        return variable;
    }

    void addStyle(StringBuilder builder,String variable,String key,String value){
        builder.append(variable).append(".style.").append(key).append(" = \"").append(value).append("\";\n");
    }

    void addStyle(StringBuilder builder,String variable,String key,int value){
        builder.append(variable).append(".style.").append(key).append(" = ").append(value).append(";\n");
    }
    void addStyle(StringBuilder builder,String variable,String key,double value){
        builder.append(variable).append(".style.").append(key).append(" = ").append(value).append("d;\n");
    }
    void addStyle(StringBuilder builder,String variable,String key,float value){
        builder.append(variable).append(".style.").append(key).append(" = ").append(value).append("f;\n");
    }
    void addStyle(StringBuilder builder,String variable,String key,boolean value){
        builder.append(variable).append(".style.").append(key).append(" = ").append(value).append(";\n");
    }
    void addStyle(StringBuilder builder,String variable,String key,Color value){
        builder.append(variable)
                .append(".style.")
                .append(key)
                .append(" = new Color(")
                .append(value.getRed())
                .append(",")
                .append(value.getGreen())
                .append(",")
                .append(value.getBlue())
                .append(",")
                .append(value.getAlpha())
                .append(");\n");
    }
    String serialize(StunningUI_Sprite sprite,StringBuilder stringBuilder,String parentVariable){
        String variable=createObject(stringBuilder,sprite.getClass().getSimpleName(),parentVariable);
        serializeStyle(sprite.style,variable,stringBuilder);
        stringBuilder.append("\n\n\n");

        for(int i=0;i<sprite.children.size();i++)
        {
            sprite.children.get(i).generateJava(this,stringBuilder,variable);
        }
        return variable;
    }
    void serializeStyle(StunningUI_Style style,String variable,StringBuilder stringBuilder)
    {
        addStyle(stringBuilder,variable,"canvasWidth",style.width);
        addStyle(stringBuilder,variable,"canvasHeight",style.height);
        addStyle(stringBuilder,variable,"top",style.top);
        addStyle(stringBuilder,variable,"left",style.left);
        addStyle(stringBuilder,variable,"foregroundColor",style.foregroundColor);
        addStyle(stringBuilder,variable,"backgroundImage",style.backgroundImage);
        addStyle(stringBuilder,variable,"backgroundRepeat",style.backgroundRepeat);
        addStyle(stringBuilder,variable,"backgroundImageOpacity",style.backgroundImageOpacity);
        addStyle(stringBuilder,variable,"overflowX",style.overflowX);
        addStyle(stringBuilder,variable,"overflowY",style.overflowY);
        addStyle(stringBuilder,variable,"scrollXVisible",style.scrollXVisible);
        addStyle(stringBuilder,variable,"scrollYVisible",style.scrollYVisible);
        addStyle(stringBuilder,variable,"depth",style.depth);
        addStyle(stringBuilder,variable,"fontName",style.fontName);
        addStyle(stringBuilder,variable,"fontSize",style.fontSize);
        addStyle(stringBuilder,variable,"scrollTop",style.scrollTop);
    }
}
