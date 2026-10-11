#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform float GameTime;
uniform float CharStep;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec2 noiseUV = vec2(texCoord0.x / CharStep, texCoord0.y) * 0.15;

    float noise = texture(Sampler1, noiseUV - vec2(0, GameTime * 540)).r;
    float pixelOffsetNoise = texture(Sampler1, noiseUV - vec2(0, GameTime * 430)).r * 2 - 1;

    vec4 baseColor = texture(Sampler0, texCoord0 + vec2(pixelOffsetNoise * (CharStep * 0.194), 0)) * vertexColor;
    fragColor = baseColor - (1 - vec4(0.502, 0.0, 0.502, 0)) * baseColor.a * pow(noise, 3) * 2;
}