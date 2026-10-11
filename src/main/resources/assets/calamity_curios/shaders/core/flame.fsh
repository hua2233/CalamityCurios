#version 150

uniform sampler2D Sampler0;
uniform float GameTime;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 color = vertexColor;
    vec4 fadeMapColor = texture(Sampler0, vec2(fract(texCoord0.y), fract(texCoord0.x - GameTime * 600)));
    fadeMapColor.r *= pow(texCoord0.x, 0.04);

    float opacity = mix(1.45, 1.95, fadeMapColor.r) * color.a;
    opacity *= pow(sin(texCoord0.y * 3.141), mix(1, 6, pow(texCoord0.x, 2)));
    opacity *= pow(sin(texCoord0.x * 3.141), 0.4);
    opacity *= fadeMapColor.r * 1.5 + 1;
    opacity *= mix(0.4, 0.9, fadeMapColor.r);

    vec3 transformColor = mix(vec3(1, 205 / 255.0, 119 / 255.0), vec3(1, 76 / 255.0, 79 / 255.0), fadeMapColor.r);
    color.rgb = mix(color.rgb, transformColor, fadeMapColor.r);

    fragColor = color * opacity * 1.6;
    if (fragColor.a < 0.5) discard;
}
