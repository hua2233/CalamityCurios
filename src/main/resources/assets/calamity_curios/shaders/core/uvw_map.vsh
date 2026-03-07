#version 150

// Uniforms
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;

out vec4 vertexColor;
out vec3 texCoord0;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1);

    vertexColor = Color;
    texCoord0 = vec3(UV0, float(UV2.x) / 3000.0f);
}