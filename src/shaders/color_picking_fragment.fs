#version 330

in vec3 mvVertexpos;

out vec4 fragColor;

uniform vec3 color;

void main(){

    vec4 baseColor = vec4(color, 1.0);
    
    fragColor = baseColor;
}