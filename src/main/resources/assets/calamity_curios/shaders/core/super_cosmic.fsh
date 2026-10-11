#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

uniform float GameTime;

in vec2 texCoord0;
out vec4 fragColor;

float InverseLerp(float from, float to, float x){
    return clamp((x - from) / (to - from), 0.0, 1.0);
}

float OpacityFromCutoffInterpolant(float cutoffInterpolant, float edgeOffset, float edgeCenter, float maxWidth) {
    float edgeCutoff = 1 - pow(1 - cutoffInterpolant, 10);
    float edgeOpacity = InverseLerp(maxWidth, maxWidth * 0.83, distance(edgeOffset, edgeCenter) / edgeCutoff);
    return edgeOpacity;
}

float CalculateBackLineInterpolant(vec2 coords, vec2 zeroCenteredCoords, float scrollTime) {
    float noise2 = texture(Sampler1, coords * 1.42 + vec2(scrollTime * -1.97, 0.0)).r;
    float noise3 = texture(Sampler1, coords * 9.3 + vec2(scrollTime * -3.67, 0.0)).r;
    float noise4 = texture(Sampler1, coords * 5.0 - vec2(scrollTime * 2.27, noise2)).r;

    float widthFactor = 0.5;
    float lengthFactor = 1.0;

    vec2 distancesFromCenter = vec2(abs(zeroCenteredCoords.x), abs(zeroCenteredCoords.y));
    float edgeFade = InverseLerp(0.1, 0.08, (distancesFromCenter.y - (noise3 - 0.5) * 0.076) / widthFactor);
    float lengthStartInterpolant = InverseLerp(0.0, widthFactor * 0.3, distancesFromCenter.x);
    float lengthStartFade = OpacityFromCutoffInterpolant(lengthStartInterpolant, coords.y, 0.5, widthFactor) * InverseLerp(0.0, 0.012, distancesFromCenter.x);

    float lengthEndFade = InverseLerp(0.2, 0.193, distancesFromCenter.x / lengthFactor + noise4 * 0.046);
    return edgeFade * lengthStartFade * lengthEndFade;
}

void main() {
    float scrollTime = GameTime * 250.0;

    vec2 scrolledCoords = texCoord0 + vec2(scrollTime * 0.05, scrollTime * -0.55);

    float time = scrollTime * 0.2;
    vec4 fadeMapColor1 = texture(Sampler0, scrolledCoords * 2.0 - vec2(0.0, time * 1.6));
    vec4 fadeMapColor2 = texture(Sampler0, scrolledCoords * 3.0 - vec2(time * 0.8, time * 0.8));
    float opacity = 1.0 + fadeMapColor1.g;

    vec4 spaceColor = vec4(0.196, 0.07, 0.392, 1.0) * opacity;
    spaceColor += fadeMapColor2 * spaceColor.a;

    spaceColor.rgb = pow(spaceColor.rgb * 1.09, vec3(2.2)) * 0.25;

    vec4 finalColor = clamp(spaceColor, 0.0, 1.0);
    vec3 backColor = vec3(1.0 - dot(finalColor.rgb, vec3(0.3, 0.59, 0.11)) * .6);
    fragColor = mix(finalColor, vec4(backColor, 1.0), CalculateBackLineInterpolant(texCoord0, texCoord0 - 0.5, scrollTime));
}
