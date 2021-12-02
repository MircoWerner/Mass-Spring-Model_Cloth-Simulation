#version 330 core

layout(location = 0) in vec4 position;
layout(location = 1) in vec2 texCoord;
layout(location = 2) in vec4 normal;
layout(location = 3) in vec4 tangent;

out vec2 passTexCoord;
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

    vec3 norm = normalize((transpose(inverse(viewMatrix * transformationMatrix)) * normal).xyz); // transform from object space to eye space
    vec3 tang = normalize((viewMatrix * transformationMatrix * tangent).xyz); // transform from object space to eye space
    tang = normalize(tang - dot(tang, norm) * norm); // re-orthogonalize tang
    vec3 bitang = normalize(cross(norm, tang));
    mat3 toTangentSpace = mat3(
        tang.x, bitang.x, norm.x,
        tang.y, bitang.y, norm.y,
        tang.z, bitang.z, norm.z
    ); // transforms from eye space to tanget space

    toLight = toTangentSpace * ((viewMatrix * vec4(lightPosition, 1.0)).xyz - positionRelativeToCam.xyz); // calculate direction in eye space first, then transform to tanget space
    toCamera = toTangentSpace * (-positionRelativeToCam.xyz);

    float distance = length(positionRelativeToCam.xyz);
    visibility = exp(-pow((distance * density), gradient));
    visibility = clamp(visibility, 0.0, 1.0);
}
