#version 150

uniform sampler2D Sampler0;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    float offset = 0.025;
    vec2 basetexCoord0 = texCoord0 * vec2(0.2, 1);
    vec4 left = texture(Sampler0, basetexCoord0 + vec2(-offset, 0));
    vec4 right = texture(Sampler0, basetexCoord0 + vec2(offset, 0));
    vec4 top = texture(Sampler0, basetexCoord0 + vec2(0, -offset));
    vec4 bottom = texture(Sampler0, basetexCoord0 + vec2(0, offset));
    vec4 center = texture(Sampler0, basetexCoord0);

    float streak = pow((left + right + top + bottom + center) * 0.2, vec4(0.7)).r;
    streak = smoothstep(0.5, 0.93, streak) * 9;
    fragColor = vertexColor * streak * pow(texCoord0.y * (4 - texCoord0.y * 4), 2) * 2;
}