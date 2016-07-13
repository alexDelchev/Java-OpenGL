#version 330

in vec2 texctureCoords;


uniform sampler2D modelTexture;

void main(){
    
    gl_FragDepth = gl_FragCoord.z;

}