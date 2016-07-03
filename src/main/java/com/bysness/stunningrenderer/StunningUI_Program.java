package com.bysness.stunningrenderer;

import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GLES2;

/**
 * Created by Anuraag on 4/29/2016.
 */
public class StunningUI_Program {
    private static StunningUI_Program ourInstance = new StunningUI_Program();

    public static StunningUI_Program getInstance() {
        return ourInstance;
    }

    private StunningUI_Program() {
    }

    private String vertexShaderString_NoTexture =
            "#if __VERSION__ >= 130\n" + // GLSL 130+ uses in and out
                    "  #define attribute in\n" + // instead of attribute and varying
                    "  #define varying out\n" +  // used by OpenGL 3 core and later.
                    "#endif\n" +

                    "#ifdef GL_ES \n" +
                    "precision mediump float; \n" + // Precision Qualifiers
                    "precision mediump int; \n" +   // GLSL ES section 4.5.2
                    "#endif \n" +

                    "uniform mat4    uniform_Projection; \n" + // incoming data used by
                    "attribute vec4  attribute_Position; \n" + // the vertex shader
                    "attribute vec4  attribute_Color; \n" +    // uniform and attributes

                    "varying vec4    varying_Color; \n" + // Outgoing varying data
                    // sent to the fragment shader
                    "void main(void) \n" +
                    "{ \n" +
                    "  varying_Color = attribute_Color; \n" +
                    "  gl_Position = uniform_Projection * attribute_Position; \n" +
                    "} ";

//    private String fragmentShaderString_NoTexture =
//            "#if __VERSION__ >= 130\n" +
//                    "  #define varying in\n" +
//                    "  out vec4 mgl_FragColor;\n" +
//                    "  #define texture2D texture\n" +
//                    "  #define gl_FragColor mgl_FragColor\n" +
//                    "#endif\n" +
//
//                    "#ifdef GL_ES \n" +
//                    "precision mediump float; \n" +
//                    "precision mediump int; \n" +
//                    "#endif \n" +
//
//                    "varying   vec4    varying_Color; \n" + //incoming varying data to the
//                    //fragment shader
//                    //sent from the vertex shader
//                    "void main (void) \n" +
//                    "{ \n" +
//                    "  gl_FragColor = varying_Color; \n" +
//                    "} ";

    private String fragmentShaderString_NoTexture =
            "#if __VERSION__ >= 130\n" +
                    "  #define varying in\n" +
                    "  out vec4 mgl_FragColor;\n" +
                    "  #define texture2D texture\n" +
                    "  #define gl_FragColor mgl_FragColor\n" +
                    "#endif\n" +

                    "#ifdef GL_ES \n" +
                    "precision mediump float; \n" +
                    "precision mediump int; \n" +
                    "#endif \n" +

                    "varying   vec4    varying_Color; \n" + //incoming varying data to the
                    "uniform float hasClipping; \n" + //does it require clipping
                    //fragment shader
                    //sent from the vertex shader
                    "void main (void) \n" +
                    "{ \n" +
                    "  if(hasClipping>0.5) \n" +
                    "  { \n" +
                    "       uniform float clipX0; \n" + //clip coord
                "           uniform float clipY0; \n" + //clip coord
                    "       uniform float clipX1; \n" + //clip coord
                    "       uniform float clipY1; \n" + //clip coord
                    "      if((gl_FragCoord.x>clipX0 && gl_FragCoord.x<clipX1) && (gl_FragCoord.y>clipY0 && gl_FragCoord.y<clipY1)) \n" +
                    "      { \n" +
                    "          gl_FragColor = varying_Color;  \n" +
                    "      } \n" +
                    "      else { \n" +
                    "           gl_FragColor.r = varying_Color.r; \n" +
                    "           gl_FragColor.g = varying_Color.g; \n" +
                    "           gl_FragColor.b = varying_Color.b; \n" +
                    "           gl_FragColor.a = 0; \n" +
                    "      } \n" +
                    "  } \n" +
                    "  else { \n" +
                    "      gl_FragColor = varying_Color; \n" +
                    "  } \n" +
                    "} ";

    private int shaderProgram;
    private int vertShader;
    private int fragShader;

    int compileProgramWithoutTexture(GL2ES2 gl){

        if(gl.isGL3core()){
            //todo: don't do this repeatedly
            System.out.println("GL3 core detected: explicit add #version 130 to shaders");
            vertexShaderString_NoTexture = "#version 130\n"+vertexShaderString_NoTexture;
            fragmentShaderString_NoTexture = "#version 130\n"+fragmentShaderString_NoTexture;
        }

        // Create GPU shader handles
        // OpenGL ES retuns a index id to be stored for future reference.
        vertShader = gl.glCreateShader(GL2ES2.GL_VERTEX_SHADER);
        fragShader = gl.glCreateShader(GL2ES2.GL_FRAGMENT_SHADER);

        //Compile the vertexShader String into a program.
        String[] vlines = new String[] { vertexShaderString_NoTexture };
        int[] vlengths = new int[] { vlines[0].length() };
        gl.glShaderSource(vertShader, vlines.length, vlines, vlengths, 0);
        gl.glCompileShader(vertShader);

        //Check compile status.
        int[] compiled = new int[1];
        gl.glGetShaderiv(vertShader, GL2ES2.GL_COMPILE_STATUS, compiled,0);
        if(compiled[0]!=0){System.out.println("Horray! vertex shader compiled");}
        else {
            int[] logLength = new int[1];
            gl.glGetShaderiv(vertShader, GL2ES2.GL_INFO_LOG_LENGTH, logLength, 0);

            byte[] log = new byte[logLength[0]];
            gl.glGetShaderInfoLog(vertShader, logLength[0], (int[])null, 0, log, 0);

            System.err.println("Error compiling the vertex shader: " + new String(log));
            System.exit(1);
        }

        //Compile the fragmentShader String into a program.
        String[] flines = new String[] { fragmentShaderString_NoTexture };
        int[] flengths = new int[] { flines[0].length() };
        gl.glShaderSource(fragShader, flines.length, flines, flengths, 0);
        gl.glCompileShader(fragShader);

        //Check compile status.
        gl.glGetShaderiv(fragShader, GL2ES2.GL_COMPILE_STATUS, compiled,0);
        if(compiled[0]!=0){System.out.println("Horray! fragment shader compiled");}
        else {
            int[] logLength = new int[1];
            gl.glGetShaderiv(fragShader, GL2ES2.GL_INFO_LOG_LENGTH, logLength, 0);

            byte[] log = new byte[logLength[0]];
            gl.glGetShaderInfoLog(fragShader, logLength[0], (int[])null, 0, log, 0);

            System.err.println("Error compiling the fragment shader: " + new String(log));
            System.exit(1);
        }

        //Each shaderProgram must have
        //one vertex shader and one fragment shader.
        shaderProgram = gl.glCreateProgram();
        gl.glAttachShader(shaderProgram, vertShader);
        gl.glAttachShader(shaderProgram, fragShader);

        //Associate attribute ids with the attribute names inside
        //the vertex shader.
        gl.glBindAttribLocation(shaderProgram, 0, "attribute_Position");
        gl.glBindAttribLocation(shaderProgram, 1, "attribute_Color");

        gl.glLinkProgram(shaderProgram);
        return shaderProgram;
    }


    private String vertexShaderString_WithTexture =
            "#if __VERSION__ >= 130\n" + // GLSL 130+ uses in and out
                    "  #define attribute in\n" + // instead of attribute and varying
                    "  #define varying out\n" +  // used by OpenGL 3 core and later.
                    "#endif\n" +

                    "#ifdef GL_ES \n" +
                    "precision mediump float; \n" + // Precision Qualifiers
                    "precision mediump int; \n" +   // GLSL ES section 4.5.2
                    "#endif \n" +

                    "uniform mat4    uniform_Projection; \n" + // incoming data used by
                    "attribute vec4  attribute_Position; \n" + // the vertex shader
                    "attribute vec4  attribute_Color; \n" +    // uniform and attributes
                    "attribute vec2  attribute_TexCoord; \n" +    // Texture coords

                    "varying vec4    varying_Color; \n" + // Outgoing varying data
                    "varying vec4    varying_Position; \n" + // Outgoing varying position
                    "varying vec2    varying_TexCoord; \n" + // Outgoing varying data sent to the fragment shader
                    "void main(void) \n" +
                    "{ \n" +
                    "  varying_Color = attribute_Color; \n" +
                    "  gl_Position = uniform_Projection * attribute_Position; \n" +
                    "  varying_Position = gl_Position; \n" +
                    "  varying_TexCoord = attribute_TexCoord; \n" + //interpolate for the frag shader
                    "} ";

    private String fragmentShaderString_WithTexture =
            "#if __VERSION__ >= 130\n" +
                    "  #define varying in\n" +
                    "  out vec4 mgl_FragColor;\n" +
                    //"  #define texture2D texture\n" +
                    "  #define gl_FragColor mgl_FragColor\n" +
                    "#endif\n" +

                    "#ifdef GL_ES \n" +
                    "precision mediump float; \n" +
                    "precision mediump int; \n" +
                    "#endif \n" +

                    "uniform float hasClipping; \n" + //does it require clipping
                    "uniform float clipX0; \n" + //clip coord
                    "uniform float clipY0; \n" + //clip coord
                    "uniform float clipX1; \n" + //clip coord
                    "uniform float clipY1; \n" + //clip coord

                    "uniform float screenWidth; \n" + //screen canvasWidth
                    "uniform float screenHeight; \n" + //screen canvasHeight


                    "uniform sampler2D texture; \n" + //sampler 2D texture
                    "uniform float hasTexture; \n" + //does it have a texture?
                    "uniform float opacity; \n" + //opacity of the rectangle
                    "varying vec4 varying_Color; \n" + //incoming varying data to the fragment shader
                    "varying vec4 varying_Position; \n" + //position coming from vertex shader
                    "in   vec2    varying_TexCoord; \n" + //texture coordinates from vertex shader
                    //sent from the vertex shader
                    "void main (void) \n" +
                    "{ \n" +
                    "  if(hasTexture>0.5) \n" +
                    "  { \n" +
                    "      gl_FragColor.r = texture2D(texture, varying_TexCoord).r; \n" +
                    "      gl_FragColor.g = texture2D(texture, varying_TexCoord).g; \n" +
                    "      gl_FragColor.b = texture2D(texture, varying_TexCoord).b; \n" +
                    "      gl_FragColor.a = opacity * texture2D(texture, varying_TexCoord).a; \n" +
                    "  } \n" +
                    "  else \n" +
                    "  { \n" +
                    "      gl_FragColor = varying_Color; \n" +
                    "  } \n" +
                    "  if(hasClipping>0.5) \n" +
                    "  { \n" +
                    "      float curPosX=(gl_FragCoord.x/screenWidth) * 2.0 - 1.0; \n" +
                    "      float curPosY=(gl_FragCoord.y/screenHeight) * 2.0 - 1.0; \n" +
                    "      //Note that in openGL, Y is flipped \n" +
                    "      if(curPosX>=clipX0 && curPosX<=clipX1 && curPosY<=clipY0 && curPosY>=clipY1) \n" +
                    "      { \n" +
                    "          gl_FragColor = gl_FragColor;  \n" +
                    "      } \n" +
                    "      else { \n" +
                    "          gl_FragColor.a=0.0; \n" +
                    "      } \n" +
                    "  } \n" +
                    "  else { \n" +
                    "      gl_FragColor = gl_FragColor; \n" +
                    "  } \n" +
                    "} ";

    int compileProgramWithTexture(GL2ES2 gl){
        /* The initialization below will use the OpenGL ES 2 API directly
         * to setup the two shader programs that will be run on the GPU.
         *
         * Its recommended to use the jogamp/opengl/util/glsl/ classes
         * import com.jogamp.opengl.util.glsl.ShaderCode;
         * import com.jogamp.opengl.util.glsl.ShaderProgram;
         * import com.jogamp.opengl.util.glsl.ShaderState;
         * to simplify shader customization, compile and loading.
         *
         * You may also want to look at the JOGL RedSquareES2 demo
         * http://jogamp.org/git/?p=jogl.git;a=blob;f=src/test/com/jogamp/opengl/test/junit/jogl/demos/es2/RedSquareES2.java;hb=HEAD#l78
         * to see how the shader customization, compile and loading is done
         * using the recommended JogAmp GLSL utility classes.
         */

        // Make the shader strings compatible with OpenGL 3 core if needed
        // GL2ES2 also includes the intersection of GL3 core
        // The default implicit GLSL version 1.1 is now depricated in GL3 core
        // GLSL 1.3 is the minimum version that now has to be explicitly set.
        // This allows the shaders to compile using the latest
        // desktop OpenGL 3 and 4 drivers.
        if(gl.isGL3core()){
            //todo: don't do this repeatedly
            System.out.println("GL3 core detected: explicit add #version 130 to shaders");
            vertexShaderString_WithTexture = "#version 130\n"+vertexShaderString_WithTexture;
            fragmentShaderString_WithTexture = "#version 130\n"+fragmentShaderString_WithTexture;
        }

        // Create GPU shader handles
        // OpenGL ES retuns a index id to be stored for future reference.
        vertShader = gl.glCreateShader(GL2ES2.GL_VERTEX_SHADER);
        fragShader = gl.glCreateShader(GL2ES2.GL_FRAGMENT_SHADER);

        //Compile the vertexShader String into a program.
        String[] vlines = new String[] { vertexShaderString_WithTexture };
        int[] vlengths = new int[] { vlines[0].length() };
        gl.glShaderSource(vertShader, vlines.length, vlines, vlengths, 0);
        gl.glCompileShader(vertShader);

        //Check compile status.
        int[] compiled = new int[1];
        gl.glGetShaderiv(vertShader, GL2ES2.GL_COMPILE_STATUS, compiled,0);
        if(compiled[0]!=0){System.out.println("Horray! vertex shader compiled");}
        else {
            int[] logLength = new int[1];
            gl.glGetShaderiv(vertShader, GL2ES2.GL_INFO_LOG_LENGTH, logLength, 0);

            byte[] log = new byte[logLength[0]];
            gl.glGetShaderInfoLog(vertShader, logLength[0], (int[])null, 0, log, 0);

            System.err.println("Error compiling the vertex shader: " + new String(log));
            System.exit(1);
        }

        //Compile the fragmentShader String into a program.
        String[] flines = new String[] { fragmentShaderString_WithTexture };
        int[] flengths = new int[] { flines[0].length() };
        gl.glShaderSource(fragShader, flines.length, flines, flengths, 0);
        gl.glCompileShader(fragShader);

        //Check compile status.
        gl.glGetShaderiv(fragShader, GL2ES2.GL_COMPILE_STATUS, compiled,0);
        if(compiled[0]!=0){System.out.println("Horray! fragment shader compiled");}
        else {
            int[] logLength = new int[1];
            gl.glGetShaderiv(fragShader, GL2ES2.GL_INFO_LOG_LENGTH, logLength, 0);

            byte[] log = new byte[logLength[0]];
            gl.glGetShaderInfoLog(fragShader, logLength[0], (int[])null, 0, log, 0);

            System.err.println("Error compiling the fragment shader: " + new String(log));
            System.exit(1);
        }

        //Each shaderProgram must have
        //one vertex shader and one fragment shader.
        shaderProgram = gl.glCreateProgram();
        gl.glAttachShader(shaderProgram, vertShader);
        gl.glAttachShader(shaderProgram, fragShader);

        //Associate attribute ids with the attribute names inside
        //the vertex shader.
        gl.glBindAttribLocation(shaderProgram, 0, "attribute_Position");
        gl.glBindAttribLocation(shaderProgram, 1, "attribute_Color");
        gl.glBindAttribLocation(shaderProgram, 2, "attribute_TexCoord");

        gl.glLinkProgram(shaderProgram);
        return shaderProgram;
    }

    void disposeProgram(GL2ES2 gl)
    {
        gl.glUseProgram(0);
        gl.glDetachShader(shaderProgram, vertShader);
        gl.glDeleteShader(vertShader);
        gl.glDetachShader(shaderProgram, fragShader);
        gl.glDeleteShader(fragShader);
        gl.glDeleteProgram(shaderProgram);
    }
}
