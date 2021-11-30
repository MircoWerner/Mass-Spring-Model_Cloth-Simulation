#version 330 core

in vec2 passTexCoord;
in vec3 passNormal;
in vec3 toLight;
in vec3 toCamera;
in float visibility;

out vec4 fragColor;

uniform sampler2D texture_sampler;
uniform vec3 skyColor;
uniform vec3 lightColor;

const float ambientStrength = 0.2;
const float diffuseStrength = 1.0;
const float specularStrength = 0.0;
const float phongExponent = 0.0;

void main() {
    vec4 textureColor = texture(texture_sampler, passTexCoord);

    // phong
    vec3 unitNormal = normalize(passNormal);
    vec3 unitToCamera = normalize(toCamera);
    vec3 unitToLight = normalize(toLight);
    vec3 reflectedLightDirection = reflect(-unitToLight, unitNormal);

    float totalAmbient = ambientStrength;
    float totalDiffuse = diffuseStrength * max(dot(unitNormal, unitToLight), 0.0);
    float totalSpecular = specularStrength * pow(max(dot(reflectedLightDirection, unitToCamera), 0.0), phongExponent);

    fragColor = (totalAmbient + totalDiffuse) * vec4(lightColor, 1.0) * textureColor + totalSpecular * vec4(lightColor, 1.0);

    fragColor = mix(vec4(skyColor, 1.0), fragColor, visibility);
}