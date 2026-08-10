#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D NoiseSampler;

uniform float Time;
uniform float MaterializeInterpolant;
uniform float FadeToWhite;
uniform vec2 OutSize;

in vec2 texCoord;
out vec4 fragColor;

vec2 Pixelate(vec2 coords) {
    vec2 pixelationFactor = 2 / OutSize;
    vec2 pixelatedCoords = round(coords / pixelationFactor) * pixelationFactor;
    return pixelatedCoords;
}

void main() {
    vec2 pixelatedCoords = Pixelate(texCoord);
    vec2 polar = vec2(atan(0.5 - pixelatedCoords.y, 0.5 - pixelatedCoords.x) / 6.283 + 0.5, distance(pixelatedCoords, vec2(0.5)));
    polar.y *= 0.2;
    polar.x += polar.y * 2.2;

    vec2 scatterNoiseCoords = polar * vec2(1, 0.85);

    vec2 scatteredCoords = vec2(texture(NoiseSampler, scatterNoiseCoords).r, texture(NoiseSampler, scatterNoiseCoords + 0.32).r);
    scatteredCoords = texCoord + (scatteredCoords - 0.5) * 1.1;

    float localMaterializeInterpolant = clamp(MaterializeInterpolant + texture(NoiseSampler, pixelatedCoords * 4 + 0.54) * 0.2, 0, 1).r;
    vec2 warpedCoords = mix(Pixelate(scatteredCoords), texCoord, localMaterializeInterpolant);

    vec4 color = texture(DiffuseSampler, warpedCoords);
    color = mix(color, vec4(color.a), FadeToWhite);

    float outerFade = smoothstep(0.5, 0.43, distance(texCoord, vec2(0.5)));
    float edgeFade = smoothstep(0.3, 0.2, distance(texCoord.x, 0.5) - (1 - MaterializeInterpolant) * 0.2);
    fragColor = color * vec4(0.863, 0.078, 0.235, 0.1) * smoothstep(0, 0.5, localMaterializeInterpolant) * outerFade * edgeFade;
}