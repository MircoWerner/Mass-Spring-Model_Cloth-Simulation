#version 330

in vec2 passTexCoord;
in vec3 passNormal;
in vec3 toLight;
in vec3 toCamera;
in float visibility;

out vec4 fragColor;

uniform sampler2D texture_sampler;
uniform vec3 skyColor;
uniform vec3 lightColor;
uniform float phongExponent;
uniform float specularStrength;

const float ambientStrength = 0.2;
const float diffuseStrength = 0.7;

void main()
{
    vec4 textureColor = texture(texture_sampler, passTexCoord * 256.0);

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