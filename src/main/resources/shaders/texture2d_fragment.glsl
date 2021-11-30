#version 330

in vec2 passTexCoord;

out vec4 fragColor;

uniform sampler2D texture_sampler;
uniform int hover;
uniform int enabled;

void main()
{
    fragColor = texture(texture_sampler, passTexCoord);

    if (enabled == 0) {
        fragColor.a = 0.5;
    } else if (hover == 1) {
        fragColor.a = 0.75;
    }
}