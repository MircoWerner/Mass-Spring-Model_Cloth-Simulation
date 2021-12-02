#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec3 normal;

out vec2 passTexCoord;
out vec3 passNormal;
out vec3 toLight;
out vec3 toCamera;
out float visibility;

uniform mat4 transformationMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform vec3 lightPosition;
uniform int useFakeLighting;

const float density = 0.005;
const float gradient = 5.0;

void main()
{
    vec4 worldPosition = transformationMatrix * vec4(position, 1.0);
    vec4 positionRelativeToCam = viewMatrix * worldPosition;
    gl_Position = projectionMatrix * positionRelativeToCam;
    passTexCoord = texCoord;

    vec3 actualNormal = normal;
    if (useFakeLighting == 1) {
        actualNormal = vec3(0.0, 1.0, 0.0);
    }

    passNormal = (transpose(inverse(viewMatrix * transformationMatrix)) * vec4(actualNormal, 0.0)).xyz;
    toLight = (viewMatrix * vec4(lightPosition, 1.0)).xyz - positionRelativeToCam.xyz;
    toCamera = -positionRelativeToCam.xyz;

    float distance = length(positionRelativeToCam.xyz);
    visibility = exp(-pow((distance * density), gradient));
    visibility = clamp(visibility, 0.0, 1.0);
}