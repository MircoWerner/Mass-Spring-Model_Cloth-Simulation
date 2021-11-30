#version 330

layout (location=0) in vec2 position;

out vec2 passTexCoord;

uniform mat4 transformationMatrix;
uniform int flipped;

void main()
{
    gl_Position = transformationMatrix * vec4(position, 0.0, 1.0);
    if (flipped == 0) {
        passTexCoord = vec2((position.x + 1.0) / 2.0, 1 - (position.y + 1.0) / 2.0);
    } else {
        passTexCoord = vec2((position.x + 1.0) / 2.0, (position.y + 1.0) / 2.0);
    }
}