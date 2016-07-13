
#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec3 vertexNormal;

out vec2 outTexCoord;
out vec3 mvVertexNormal;
out vec3 mvVertexPos;
out vec4 FragPosLightSpace;

uniform mat4 modelMatrix;
uniform mat4 viewMatrix;
uniform mat4 projectionMatrix;
uniform mat4 lightSpaceMatrix;

uniform mat4 toShadowMapSpace;

void main()
{
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1.0f);
    
    outTexCoord = texCoord;
    
    mvVertexNormal = normalize(viewMatrix * modelMatrix *vec4(vertexNormal, 0.0)).xyz;
    
    vec4 mvPos = viewMatrix * modelMatrix * vec4(position, 1.0);
    mvVertexPos = mvPos.xyz;
    
    FragPosLightSpace = lightSpaceMatrix * modelMatrix * vec4(position, 1.0);
}                                                                                                                                                                                                                                                                                                                  