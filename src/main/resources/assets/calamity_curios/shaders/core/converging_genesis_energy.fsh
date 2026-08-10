#version 150

uniform sampler2D Sampler0;

in vec4 vertexColor;
in vec3 texCoord0;

out vec4 fragColor;

void main() {
    vec2 coords = texCoord0.xy;
    coords.y = (texCoord0.y - 0.5) / texCoord0.z + 0.5;

    float offset = 0.025;
    vec2 baseCoords = coords * vec2(0.2, 1);
    vec4 left = texture(Sampler0, baseCoords + vec2(-offset, 0));
    vec4 right = texture(Sampler0, baseCoords + vec2(offset, 0));
    vec4 top = texture(Sampler0, baseCoords + vec2(0, -offset));
    vec4 bottom = texture(Sampler0, baseCoords + vec2(0, offset));
    vec4 center = texture(Sampler0, baseCoords);

    float streak = pow((left + right + top + bottom + center) * 0.2, vec4(0.7)).r;
    streak = smoothstep(0.5, 0.93, streak) * 9;
    fragColor = vertexColor * streak * pow(coords.y * (4 - coords.y * 4), 2) * 2;
}