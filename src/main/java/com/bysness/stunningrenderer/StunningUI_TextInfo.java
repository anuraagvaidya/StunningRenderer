package com.bysness.stunningrenderer;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Anuraag on 6/3/2016.
 */
public class StunningUI_TextInfo {
    class LineSpecification{
        int start=0;
        int end=0;
        ArrayList<Double> charWidths;
        String text;
        void remove(int index,LineSpecification nextLine){
//            for(int i=(index-start);i<charWidths.size();i++)
//            {
//                nextLine.charWidths.add(charWidths.get(i));
//            }
            for(int i=(index-start);i<charWidths.size();i++)
            {
                charWidths.remove(i);
                i--;
            }
        }
    }
    ArrayList<LineSpecification> lines;
    double curWidth=0;
    double curHeight=0;
    double maxWidth=0;
    double minHeight=0;
    float ascent=1;
    float descent=1;
    int lineHeight=1;
    Font font;
    StunningUI_TextInfo(){
        lines=new ArrayList<LineSpecification>();
    }
    LineSpecification addLine(int start){
        LineSpecification spec=new LineSpecification();
        spec.start=start;
//        spec.end=end;
//        spec.text=text;
        spec.charWidths=new ArrayList<Double>();
        lines.add(spec);
        return spec;
    }
    void deleteLine(int index){
        lines.remove(index);
    }
    String getText(){
        String text="";
        for (LineSpecification s : lines)
        {
            text += s.text + "\r\n"; //todo: OS based separator
        }
        return text;
    }
}
