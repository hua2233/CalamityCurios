#version 150

uniform sampler2D Sampler0;
uniform float GameTime;

in vec2 texCoord0;
in vec4 vertexColor;
out vec4 fragColor;

void main() {

    vec2 polar = vec2(atan(texCoord0.y - 0.5, texCoord0.x - 0.5) / 6.283 + 0.5, distance(texCoord0, vec2(0.5)));

    float noiseA = texture(Sampler0, polar * vec2(2, 0.02) + vec2(0, GameTime * -22)).r;
    float noiseB = texture(Sampler0, polar * vec2(3, 0.04) + vec2(0, GameTime * -16)).r;

    float distanceGlow = smoothstep(0.5 - noiseB * 0.3, 0, polar.y);
    float baseGlow = sqrt(noiseA * noiseB) * distanceGlow * 3;

    float centerGlow = smoothstep(0.18, 0, polar.y) * 0.4 / polar.y;
    vec4 result = smoothstep(0, 0.85, pow(baseGlow, 2.4)) * vertexColor + centerGlow * vertexColor.a;

    if(!(texCoord0.x > 0.3 && texCoord0.x < 0.7) || !(texCoord0.y > 0.3 && texCoord0.y < 0.7)) discard;

    fragColor = vec4(clamp(result.rgb, 0.0, 0.97), vertexColor.a);
}