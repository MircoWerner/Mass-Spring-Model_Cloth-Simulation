#version 330 core

layout(location = 0) in vec4 position;
layout(location = 1) in vec2 texCoord;
layout(location = 2) in vec4 normal;

out vec2 passTexCoord;
out vec3 passNormal;
out vec3 toLight;
out vec3 toCamera;
out float visibility;

uniform mat4 transformationMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform vec3 lightPosition;

const float density = 0.005;
const float gradient = 5.0;

void main() {
    vec4 worldPosition = transformationMatrix * position;
    vec4 positionRelativeToCam = viewMatrix * worldPosition;
    gl_Position = projectionMatrix * positionRelativeToCam;
    passTexCoord = texCoord;

    passNormal = mat3(transpose(inverse(transformationMatrix))) * normal.xyz;
    toLight = lightPosition - worldPosition.xyz;
    toCamera = (inverse(viewMatrix) * vec4(0.0, 0.0, 0.0, 1.0)).xyz - worldPosition.xyz;

    float distance = length(positionRelativeToCam.xyz);
    visibility = exp(-pow((distance * density), gradient));
    visibility = clamp(visibility, 0.0, 1.0);
}
