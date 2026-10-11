#version 150
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

in vec3 Position;

out vec3 viewPos;

void main() {
    vec4 vp = ModelViewMat * vec4(Position, 1.0);
    viewPos = vp.xyz;
    gl_Position = ProjMat * vp;
}