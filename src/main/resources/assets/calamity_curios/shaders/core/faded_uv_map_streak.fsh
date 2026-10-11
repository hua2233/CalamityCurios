#version 150

// Uniforms
uniform sampler2D Sampler0;
uniform float GameTime;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 fadeMapColor = texture(Sampler0, vec2(fract(texCoord0.x - GameTime / 20 * 2.5), texCoord0.y));
    float opacity = fadeMapColor.r;

    float power = mix(3.0, 10.0, texCoord0.x);
    opacity = mix(pow(sin(texCoord0.y * 3.141), power), opacity, texCoord0.x);

    if (texCoord0.x > 0.85) opacity *= pow(1.0 - (texCoord0.x - 0.85) / 0.3, 6.0);
    fragColor = vertexColor * opacity * 1.5;
}
