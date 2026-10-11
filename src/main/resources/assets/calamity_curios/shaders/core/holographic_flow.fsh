#version 150
// Uniforms
uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform float GameTime;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 noiseOverlay1 = texture(Sampler0, texCoord0 * 2 + vec2(-0.2, 0.16) * GameTime * 300).r * vertexColor;
    vec4 noiseOverlay2 = texture(Sampler0, texCoord0 * 2 + vec2(0.18, 0.28) * GameTime * 300).r * vertexColor;
    vec4 codeOverlay = texture(Sampler1, vec2(texCoord0.y, 1 - texCoord0.x + GameTime * 350)) * clamp((texCoord0.x - .85) / (.67 - .85), 0.0, 1.0) * 0.2;
    fragColor = noiseOverlay1 + noiseOverlay2 + codeOverlay + vertexColor;
    fragColor.a *= 0.67;
}