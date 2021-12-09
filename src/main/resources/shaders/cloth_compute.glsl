#version 430 core

struct Point {
    vec4 position;
    vec4 velocity;
    vec4 data;// x = locked, yz = uv tex coord, w = padding
};

struct RenderDataPosition {
    vec4 position;
};

struct RenderDataNormal {
    vec4 normal;
};

struct RenderDataTangent {
    vec4 tangent;
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
// buffer where the calculated information will be stored that is used for rendering in the vertex/fragment shader
layout(binding = 4, std430) buffer renderDataTangentBuffer {
    RenderDataTangent renderDataTangent[];
};

uniform float time;// time step for integration
uniform int normalSign;// normal orientation \in {-1,1}
uniform int sphereEnabled;// 1 if sphere collisions are enabled, 0 otherwise
uniform int width;// amount of points
uniform int height;// amount of points
uniform float mass;// mass of one point
uniform float viscousDamping;// damping constant >= 0, higher damping constant causes more friction
uniform vec3 velocityFluid; // velocity of a viscous fluid like wind or water (used for viscous interaction force)
uniform float springConstant;// spring constant >= 0, higher spring constant makes cloth more stiff
uniform int state;// 0 => apply forces, 1 => relaxation of the joints, any other number => only write output buffers (and calculate normals, tangents etc.)

const float restingLengthHorizontal = 1;
const float maxRestingLengthHorizontal = 1.1 * restingLengthHorizontal;
const float restingLengthVertical = 1;
const float maxRestingLengthVertical = 1.1 * restingLengthVertical;
const float restingLengthDiagonal = sqrt(2);
const float maxRestingLengthDiagonal = 1.1 * restingLengthDiagonal;
const float restingLengthEpsilon = 0.01;
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

vec3 calcTangent(vec3 pos1, vec3 pos2, vec3 pos3, vec2 uv1, vec2 uv2, vec2 uv3) {
    vec3 edge1 = pos2 - pos1;
    vec3 edge2 = pos3 - pos1;
    vec2 deltaUV1 = uv2 - uv1;
    vec2 deltaUV2 = uv3 - uv1;

    float f = 1.0 / (deltaUV1.x * deltaUV2.y - deltaUV2.x * deltaUV1.y);

    vec3 tangent = vec3(0.0);
    tangent.x = f * (deltaUV2.y * edge1.x - deltaUV1.y * edge2.x);
    tangent.y = f * (deltaUV2.y * edge1.y - deltaUV1.y * edge2.y);
    tangent.z = f * (deltaUV2.y * edge1.z - deltaUV1.y * edge2.z);
    return tangent;
}

void applyForce(uvec2 id, uint i) {
    // external force
    // Important: Taking the old normal might be dangerous in some situations. If the GPU does not initialize
    // the renderDataNormal buffer with zeros, old data will be read during the first invocation of the compute shader.
    // The viscousInteractionFoce might become unlimitedly high for some points, making them disappear.
    // Solution: Initialize renderDataNormal buffer on the CPU side before the first invocation or make sure to call
    // applyFore in the second invocation for the first time (when the buffer has been initialized by the compute shader).
    vec3 oldNormal = renderDataNormal[i].normal.xyz;
    vec3 oldVelocity = pointIn[i].velocity.xyz;
    vec4 viscousInteractionForce = vec4(-dot(oldNormal, velocityFluid - oldVelocity) * oldNormal, 0.0);
    vec4 force = vec4(0, -mass * gravity, 0, 0) + pointIn[i].velocity * -viscousDamping + viscousInteractionForce;

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
            velocity *= 0.9;
        }
    }
    if (position.y < 0.05) {
        position.y = 0.05;
        velocity.y = 0;
        velocity *= 0.9;
    }

    pointOut[i].velocity = velocity;
    pointOut[i].position = position;
}

vec3 calcRelaxationDirection(vec4 pos1, vec4 pos2, float locked2, float maxRestingLength, uint i) {
    vec3 relaxDir = pos2.xyz - pos1.xyz;
    float length = length(relaxDir);
    if (length < maxRestingLength + restingLengthEpsilon) { // only relax if it is too long (plus some epsilon)
        return vec3(0.0);
    }
    relaxDir *= ((length - maxRestingLength) / length);// ensure direction vector to have the correct length
    if (locked2 == 0) {
        pointOut[i].velocity = vec4(0.0);
        return relaxDir / 2;// only move half the way because the other unlocked point will also be moved half the way
    } else {
        pointOut[i].velocity = vec4(0.0);
        return relaxDir;// move all the way because the other point is locked
    }
}

void applyRelaxation(uvec2 id, uint i) {
    // relaxation direction
    vec3 relaxDir = vec3(0.0);

    // internal force
    // HORIZONTAL/VERTICAL
    // left
    if (id.x > 0) {
        relaxDir += calcRelaxationDirection(pointIn[i].position, pointIn[i - 1].position, pointIn[i - 1].data.x, maxRestingLengthHorizontal, i);
    }
    // right
    if (id.x < width - 1) {
        relaxDir += calcRelaxationDirection(pointIn[i].position, pointIn[i + 1].position, pointIn[i + 1].data.x, maxRestingLengthHorizontal, i);
    }
    // top
    if (id.y < height - 1) {
        relaxDir += calcRelaxationDirection(pointIn[i].position, pointIn[i + width].position, pointIn[i + width].data.x, maxRestingLengthVertical, i);
    }
    // bottom
    if (id.y > 0) {
        relaxDir += calcRelaxationDirection(pointIn[i].position, pointIn[i - width].position, pointIn[i - width].data.x, maxRestingLengthVertical, i);
    }
    // DIAGONAL
    // top left
    if ((id.x > 0) && (id.y < height - 1)) {
        relaxDir += calcRelaxationDirection(pointIn[i].position, pointIn[i + width - 1].position, pointIn[i + width - 1].data.x, maxRestingLengthDiagonal, i);
    }
    // bottom left
    if ((id.x > 0) && (id.y > 0)) {
        relaxDir += calcRelaxationDirection(pointIn[i].position, pointIn[i - width - 1].position, pointIn[i - width - 1].data.x, maxRestingLengthDiagonal, i);
    }
    // top right
    if ((id.x < width - 1) && (id.y < height - 1)) {
        relaxDir += calcRelaxationDirection(pointIn[i].position, pointIn[i + width + 1].position, pointIn[i + width + 1].data.x, maxRestingLengthDiagonal, i);
    }
    // bottom right
    if ((id.x < width - 1) && (id.y > 0)) {
        relaxDir += calcRelaxationDirection(pointIn[i].position, pointIn[i - width + 1].position, pointIn[i - width + 1].data.x, maxRestingLengthDiagonal, i);
    }

    pointOut[i].position += vec4(relaxDir, 0.0);
}

void main() {
    uvec2 id = gl_GlobalInvocationID.xy;

    uint i = id.y * width + id.x;

    if (i >= width * height) {
        return;
    }

    pointOut[i].position = pointIn[i].position;
    pointOut[i].velocity = pointIn[i].velocity;
    pointOut[i].data = pointIn[i].data;

    if (pointIn[i].data.x == 0) {
        if (state == 0) {
            applyForce(id, i);
        } else if (state == 1) {
            applyRelaxation(id, i);
        }
    }

    renderDataPosition[i].position = pointOut[i].position;

    // normal
    vec3 normal = vec3(0.0);
    vec3 position = renderDataPosition[i].position.xyz;
    if (id.x > 0) {
        if (id.y > 0) {
            normal += calcNormal(pointOut[i - 1].position.xyz, pointOut[i - width - 1].position.xyz, pointOut[i - width].position.xyz, position);
        }
        if (id.y < height - 1) {
            normal += calcNormal(pointOut[i + width].position.xyz, pointOut[i + width - 1].position.xyz, pointOut[i - 1].position.xyz, position);
        }
    }
    if (id.x < width - 1) {
        if (id.y > 0) {
            normal += calcNormal(pointOut[i - width].position.xyz, pointOut[i - width + 1].position.xyz, pointOut[i + 1].position.xyz, position);
        }
        if (id.y < height - 1) {
            normal += calcNormal(pointOut[i + 1].position.xyz, pointOut[i + width + 1].position.xyz, pointOut[i + width].position.xyz, position);
        }
    }
    renderDataNormal[i].normal = vec4(normalize(normalSign * normal), 0.0);

    // tangent
    vec3 tangent = vec3(0.0);
    if (id.x > 0) {
        if (id.y > 0) {
            tangent += calcTangent(position, pointOut[i - width - 1].position.xyz, pointOut[i - 1].position.xyz, pointOut[i].data.yz, pointOut[i - width - 1].data.yz, pointOut[i - 1].data.yz);
            tangent += calcTangent(position, pointOut[i - width - 1].position.xyz, pointOut[i - width].position.xyz, pointOut[i].data.yz, pointOut[i - width - 1].data.yz, pointOut[i - width].data.yz);
        }
    }
    if (id.x < width - 1) {
        if (id.y < height - 1) {
            tangent += calcTangent(position, pointOut[i + width + 1].position.xyz, pointOut[i + 1].position.xyz, pointOut[i].data.yz, pointOut[i + width + 1].data.yz, pointOut[i + 1].data.yz);
            tangent += calcTangent(position, pointOut[i + width + 1].position.xyz, pointOut[i + width].position.xyz, pointOut[i].data.yz, pointOut[i + width + 1].data.yz, pointOut[i + width].data.yz);
        }
    }
    if (id.x == 0 && id.y == height - 1) {
        // top left corner
        tangent = vec3(1.0, 0.0, 0.0);// or maybe take average of the two adjacent points
    } else if (id.x == width - 1 && id.y == 0) {
        // bottom right corner
        tangent = vec3(1.0, 0.0, 0.0);// or maybe take average of the two adjacent points
    }
    renderDataTangent[i].tangent = vec4(normalize(tangent), 0.0);
}
