#version 330

layout(location=0) in vec3 position;
layout(location=1) in vec2 texCoord;
layout(location=2) in vec3 vertexNormal;

out vec2 textureCoords;

uniform mat4 mvpMatrix;

void main(void){

    gl_Position = mvpMatrix * vec4(position, 1.0);
    textureCoords = texCoord;
}