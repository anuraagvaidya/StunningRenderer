package com.bysness.stunningrenderer;

import javafx.scene.transform.Affine;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by Anuraag on 5/3/2016.
 */
public class StunningUI_FontEngine {
    private void highlight(StunningUI_Sprite sprite, Graphics2D graphics2D,StunningUI_TextInfo.LineSpecification line,int selectionStart, int selectionEnd,int offsetY,int highlightHeight){
        if((selectionStart-1>=line.start && selectionStart-1-line.start<line.charWidths.size()) || (selectionEnd-line.start>0 && selectionStart<line.start+line.charWidths.size()))
        {
            graphics2D.setPaint(sprite.style.selectionColor);
            int x=(selectionStart-1-line.start<line.charWidths.size() && selectionStart-1-line.start>0)?line.charWidths.get(selectionStart-1-line.start).intValue():0;
            int y=offsetY;
            int width=selectionEnd-1>0?(selectionEnd-line.start<line.charWidths.size()?
                    line.charWidths.get(selectionEnd-line.start).intValue():
                    line.charWidths.get(line.charWidths.size()-1).intValue()-offsetY)-x:0;
            int height=highlightHeight;
            graphics2D.fillRect(x, y, width, height);
        }
    }
    private void setTextColor(StunningUI_Sprite sprite, Graphics2D graphics2D,StunningUI_TextInfo.LineSpecification line,int selectionStart, int selectionEnd){
        if(selectionStart>=line.start && selectionStart-line.start<line.charWidths.size() && selectionStart!=selectionEnd)
        {
            graphics2D.setColor(sprite.style.selectionTextColor);
        }
        else {
            graphics2D.setColor(sprite.style.textColor);
        }
    }
    void setFontBitmap(StunningUI_Sprite sprite, Font f,String text,int surfaceWidth,int lineHeight, int selectionStart, int selectionEnd){
        sprite.textInfo=new StunningUI_TextInfo();
        FontRenderContext fontContext = new FontRenderContext(null, false, false);
        LineMetrics fontMetrics=f.getLineMetrics(text,fontContext);
        float ascent=fontMetrics.getAscent();
        float descent=fontMetrics.getDescent();

        sprite.textInfo.ascent=ascent;
        sprite.textInfo.descent=descent;
        sprite.textInfo.lineHeight=lineHeight;

        GlyphVector gv=f.createGlyphVector(fontContext,text);
        Rectangle2D rect=gv.getLogicalBounds();
        int rectWidth=(int)Math.ceil(rect.getWidth());
        int rectHeight=(int)Math.ceil(rect.getHeight());
        int linesNeeded=Math.round((float)Math.ceil((float)rectWidth/(float)surfaceWidth))+1;
        int imageWidth=linesNeeded>1?surfaceWidth:rectWidth;
        int newLinesCount=0;
        for(int i=0;i<text.length();i++)
        {
            if(text.charAt(i)=='\n')
            {
                newLinesCount++;
            }
        }
        linesNeeded+=newLinesCount;
        int imageHeight=linesNeeded==1?rectHeight:(rectHeight*linesNeeded)+((linesNeeded-1)*lineHeight);

        imageWidth=imageWidth==0?1:imageWidth;
        imageHeight=imageHeight==0?1:imageHeight;

//        System.out.println("text canvasWidth:" + imageWidth + ", text canvasHeight:" + imageHeight + ", lines:" + linesNeeded + ", rectHeight:" + rectHeight);
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = image.createGraphics();
        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        rh.put(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        rh.put(RenderingHints.KEY_STROKE_CONTROL,RenderingHints.VALUE_STROKE_PURE);
        graphics2D.setRenderingHints(rh);

        boolean wrapAtSpace=true;
        String whiteSpace=" ";

        double currentWidth=0;
        double lastWidth=0;
        int currentIndex=0;
        int linelyCurrentIndex=0;
        int lastStart=0;
        int currentLine=0;
        int lastWhiteSpace=-1;
        int lastWhiteSpaceCut=-2;
        double lastWhiteSpaceWidth=0;
        sprite.textInfo.curWidth=0;
        sprite.textInfo.curHeight=lineHeight;
        StunningUI_TextInfo.LineSpecification firstLine=sprite.textInfo.addLine(0);
        StunningUI_TextInfo.LineSpecification curLine=firstLine;
        StunningUI_TextInfo.LineSpecification prevLine=null;
        while(currentWidth<imageWidth && currentIndex<text.length())
        {
            String str=""+text.charAt(currentIndex);
            if(str.equals(whiteSpace))
            {
                lastWhiteSpace=currentIndex;
            }
            GlyphVector gv1=f.createGlyphVector(fontContext,str);
            Rectangle2D rect1=gv1.getLogicalBounds();
            sprite.textInfo.curWidth+=rect1.getWidth();
            curLine.charWidths.add(sprite.textInfo.curWidth);
            if(currentWidth+rect1.getWidth()>imageWidth || currentIndex==text.length()-1 || str.equals("\n"))
            {
                if(currentWidth>sprite.textInfo.maxWidth)
                {
                    sprite.textInfo.maxWidth=(int)currentWidth;
                }
                linelyCurrentIndex=currentIndex-linelyCurrentIndex;
                if(!wrapAtSpace || lastWhiteSpaceCut==lastWhiteSpace || currentIndex==text.length()-1 || lastWhiteSpace==-1 || str.equals("\n"))
                {
                    currentLine++;

                    highlight(sprite,graphics2D,curLine,selectionStart,selectionEnd,(int)(ascent),(int)(descent)+(currentLine==0?0:lineHeight));
                    graphics2D.translate(0d,(ascent)+ (currentLine==0?0:lineHeight));
//                    System.out.println("translate 2 (-" + 0d + ", " + (ascent)+ (currentLine==0?0:lineHeight) + ")");
                    currentWidth=0;
                    lastWidth=0;
                    lastWhiteSpaceCut=-2;
                    prevLine=curLine;
                    sprite.textInfo.curWidth=0;
                    sprite.textInfo.curHeight=(int)ascent + (currentLine) * ((int)ascent + lineHeight);
                    curLine = sprite.textInfo.addLine(currentIndex+1);
                }
                else
                {
                    currentLine++;
                    highlight(sprite,graphics2D,curLine,selectionStart,selectionEnd,(int)(ascent),(int)(descent)+(currentLine==0?0:lineHeight));
                    graphics2D.translate(-lastWhiteSpaceWidth,(ascent)+ (currentLine==0?0:lineHeight));
//                    System.out.println("translate 1 (-" + lastWhiteSpaceWidth + ", " + (ascent)+ (currentLine==0?0:lineHeight) + ")");
                    currentIndex=lastWhiteSpace;
                    lastWhiteSpaceWidth=0;
                    currentWidth=0;
                    sprite.textInfo.curWidth=0;
                    sprite.textInfo.curHeight=(int)ascent + (currentLine) * ((int)ascent + lineHeight);
                    lastWhiteSpaceCut=lastWhiteSpace;

                    prevLine=curLine;
                    curLine = sprite.textInfo.addLine(currentIndex+1);
                }
                prevLine.remove(currentIndex+1,curLine);
                currentIndex++;


//                System.out.println("Substring " + text.substring(Math.min(lastStart,currentIndex),Math.max(lastStart,currentIndex)));
                GlyphVector gv2=f.createGlyphVector(fontContext,text.substring(Math.min(lastStart,currentIndex),Math.max(lastStart,currentIndex)));
                //Rectangle2D rect2=gv2.getLogicalBounds();
                for (int j = 0; j < gv2.getNumGlyphs(); j++) {
                    Shape glypthShape=gv2.getGlyphOutline(j);
                    setTextColor(sprite,graphics2D,prevLine,selectionStart,selectionEnd);
                    graphics2D.fill(glypthShape);
                }
                lastStart=currentIndex;
            }
            else
            {
                lastWidth=rect1.getWidth();
                currentWidth+=rect1.getWidth();
                currentIndex++;
            }
        }



        sprite.style.backgroundBufferedImage=image;
    }
}
