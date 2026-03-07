#version 150

const float Intensity = 5.0;
uniform sampler2D DiffuseSampler;
uniform sampler2D NoiseSampler;
uniform float Time;
uniform vec2 ScreenSize;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 dynamicOffset = vec2(Time * 0.6, -Time * 0.4);
    vec2 noise = texture(NoiseSampler, texCoord * 8.0 + dynamicOffset).xy;
    vec2 distortion = (noise - 0.5) * Intensity / ScreenSize;
    fragColor = texture(DiffuseSampler, texCoord + distortion);
}