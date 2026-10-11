#version 150

uniform sampler2D Sampler0;
uniform float GameTime;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec2 noiseCoordsBase = texCoord0 * 1.2;
    vec4 noise1 = texture(Sampler0, noiseCoordsBase + vec2(0, GameTime * 64));
    vec4 noise2 = texture(Sampler0, noiseCoordsBase + vec2(GameTime * 22, GameTime * -49));
    vec4 noise = (noise1 + noise2) * 0.6;
    float fade = (sin(GameTime * 300) + 1) * 0.5;
    float noiseFadeInterpolant = 1 + fade * .4;
    vec4 noiseColor = pow(noise, vec4(3)) * noiseFadeInterpolant * 1.4 + 0.2;
    noiseColor *= vertexColor;
    fragColor = vec4(noiseColor.rgb, noiseFadeInterpolant * 0.5);
}