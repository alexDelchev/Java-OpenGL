#version 330 core

const int MAX_WEIGHTS = 4;
const int MAX_JOINTS = 150;

layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec3 vertexNormal;
layout (location=3) in vec4 jointWeights;
layout (location=4) in ivec4 jointIndices;

uniform mat4 jointsMatrix[MAX_JOINTS];
uniform mat4 lightSpaceMatrix;
uniform mat4 model;

void main(){
    
    vec4 initPos = vec4(0.0, 0.0, 0.0, 1.0);
    float totalWeight = 0;
    int count = 0;
    for(int i=0; i < MAX_WEIGHTS; i++){
        
        float weight = jointWeights[i];
        if(weight > 0.0){
            count++;
            totalWeight += weight;
            int jointIndex = jointIndices[i];
            initPos += jointsMatrix[jointIndex] * vec4(position, 1.0) * weight;
        }
    }
    
    if(totalWeight != 1.0f){
        float normWeight = 1.0f/totalWeight;
        
        initPos *= normWeight;
    }
    
    if(count == 0){
        initPos = vec4(position, 1.0);
    }
    
    gl_Position = lightSpaceMatrix * model * initPos;
}