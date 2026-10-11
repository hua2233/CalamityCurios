#version 150

// Uniforms
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;

out vec4 vertexColor;
out vec2 texCoord0;

void main() {
    texCoord0 = UV0;
    vertexColor = Color;
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1);
    // UV2 is not the illumination UV, its compressed X component is used to correct the vertex UV, holds the width of the vertex at the given point
    // Reconstruct the full 32-bit quantized width from both signed-short components:
    // .uv2(int) packs the low 16 bits into x and the high 16 bits into y.
    // mod() undoes sign-extension for values whose 16th bit is set.
    float rawWidth = (mod(float(UV2.x), 65536.0) + mod(float(UV2.y), 65536.0) * 65536.0) / 3000.0f;
    texCoord0.y = (UV0.y - .5) / rawWidth + .5;
}