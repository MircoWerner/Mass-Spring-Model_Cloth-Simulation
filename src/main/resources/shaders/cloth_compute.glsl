#version 430 core

struct Point {
    vec4 position;
    vec4 velocity;
    float locked;
    float pad1;
    float pad2;
    float pad3;
};

struct RenderDataPosition {
    vec4 position;
};

struct RenderDataNormal {
    vec4 normal;
};

// point data from the last iteration, do not write to this buffer
layout(binding = 0, std430) buffer pointInBuffer {
    Point pointIn[];
};

// updated points that will become the input (pointIn) in the next iteration
layout(binding = 1, std430) buffer pointOutBuffer {
    Point pointOut[];
};

// buffer where the calculated information will be stored that is used for rendering in the vertex shader
layout(binding = 2, std430) buffer renderDataPositionBuffer {
    RenderDataPosition renderDataPosition[];
};
// buffer where the calculated information will be stored that is used for rendering in the vertex/fragment shader
layout(binding = 3, std430) buffer renderDataNormalBuffer {
    RenderDataNormal renderDataNormal[];
};

uniform float time;
uniform int normalSign;// \in {-1,1}
uniform int sphereEnabled;
uniform int width;
uniform int height;
uniform float mass;
uniform float viscousDamping;
uniform float springConstant;

const float restingLengthHorizontal = 1;
const float restingLengthVertical = 1;
const float restingLengthDiagonal = sqrt(2);
const float gravity = 9.81;

const vec3 sphere = vec3(0.0, 30.0, 0.0);
const float sphereRadius = 10.0;

layout(local_size_x = 10, local_size_y = 10) in;

vec4 calcSpringForce(vec4 posA, vec4 posB, float restingLength) {
    vec3 dir = posB.xyz - posA.xyz;
    return vec4(springConstant * (dir - normalize(dir) * restingLength), 0.0);
}

vec3 calcNormal(vec3 pos1, vec3 pos2, vec3 pos3, vec3 position) {
    vec3 p1 = pos1 - position;
    vec3 p2 = pos2 - position;
    vec3 p3 = pos3 - position;
    return cross(p1, p2) + cross(p2, p3);
}

void main() {
    uvec2 id = gl_GlobalInvocationID.xy;

    uint i = id.y * width + id.x;

    if (i > width * height) {
        return;
    }

    pointOut[i].position = pointIn[i].position;
    pointOut[i].velocity = pointIn[i].velocity;
    pointOut[i].locked = pointIn[i].locked;

    if (pointIn[i].locked == 0) {
        // external force
        vec4 force = vec4(0, -mass * gravity, 0, 0) + pointIn[i].velocity * -viscousDamping;

        // internal force
        // HORIZONTAL/VERTICAL
        // left
        if (id.x > 0) {
            force += calcSpringForce(pointIn[i].position, pointIn[i - 1].position, restingLengthHorizontal);
        }
        // right
        if (id.x < width - 1) {
            force += calcSpringForce(pointIn[i].position, pointIn[i + 1].position, restingLengthHorizontal);
        }
        // top
        if (id.y < height - 1) {
            force += calcSpringForce(pointIn[i].position, pointIn[i + width].position, restingLengthVertical);
        }
        // bottom
        if (id.y > 0) {
            force += calcSpringForce(pointIn[i].position, pointIn[i - width].position, restingLengthVertical);
        }
        // TWO HORIZONTAL/VERTICAL
        // two left
        if (id.x > 1) {
            force += calcSpringForce(pointIn[i].position, pointIn[i - 2].position, 2 * restingLengthHorizontal);
        }
        // two right
        if (id.x < width - 2) {
            force += calcSpringForce(pointIn[i].position, pointIn[i + 2].position, 2 * restingLengthHorizontal);
        }
        // two top
        if (id.y < height - 2) {
            force += calcSpringForce(pointIn[i].position, pointIn[i + width + width].position, 2 * restingLengthVertical);
        }
        // two bottom
        if (id.y > 1) {
            force += calcSpringForce(pointIn[i].position, pointIn[i - width - width].position, 2 * restingLengthVertical);
        }
        // DIAGONAL
        // top left
        if ((id.x > 0) && (id.y < height - 1)) {
            force += calcSpringForce(pointIn[i].position, pointIn[i + width - 1].position, restingLengthDiagonal);
        }
        // bottom left
        if ((id.x > 0) && (id.y > 0)) {
            force += calcSpringForce(pointIn[i].position, pointIn[i - width - 1].position, restingLengthDiagonal);
        }
        // top right
        if ((id.x < width - 1) && (id.y < height - 1)) {
            force += calcSpringForce(pointIn[i].position, pointIn[i + width + 1].position, restingLengthDiagonal);
        }
        // bottom right
        if ((id.x < width - 1) && (id.y > 0)) {
            force += calcSpringForce(pointIn[i].position, pointIn[i - width + 1].position, restingLengthDiagonal);
        }

        // euler integration
        vec4 acceleration = force / mass;
        vec4 velocity = pointIn[i].velocity + acceleration * time;
        vec4 position = pointIn[i].position + velocity * time;

        // collision
        if (sphereEnabled != 0) {
            vec3 toSphere = position.xyz - sphere;
            if (length(toSphere) < sphereRadius + 0.05) {
                position.xyz = sphere + normalize(toSphere) * (sphereRadius + 0.05);
                velocity = vec4(0.0);
            }
        }

        pointOut[i].velocity = velocity;
        pointOut[i].position = position;
    }

    renderDataPosition[i].position = pointOut[i].position;

    // normal
    vec3 normal = vec3(0.0);
    vec3 position = renderDataPosition[i].position.xyz;
    if (id.x > 0) {
        if (id.y > 0) {
            normal += calcNormal(pointIn[i - 1].position.xyz, pointIn[i - width - 1].position.xyz, pointIn[i - width].position.xyz, position);
        }
        if (id.y < height - 1) {
            normal += calcNormal(pointIn[i + width].position.xyz, pointIn[i + width - 1].position.xyz, pointIn[i - 1].position.xyz, position);
        }
    }
    if (id.x < width - 1) {
        if (id.y > 0) {
            normal += calcNormal(pointIn[i - width].position.xyz, pointIn[i - width + 1].position.xyz, pointIn[i + 1].position.xyz, position);
        }
        if (id.y < height - 1) {
            normal += calcNormal(pointIn[i + 1].position.xyz, pointIn[i + width + 1].position.xyz, pointIn[i + width].position.xyz, position);
        }
    }
    renderDataNormal[i].normal = vec4(normalize(normalSign * normal), 0.0);
}