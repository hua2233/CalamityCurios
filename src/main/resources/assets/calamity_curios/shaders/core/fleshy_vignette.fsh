#version 150

uniform sampler2D Sampler0;
uniform float GameTime;

uniform float RadialOffsetTime;
uniform float AnimationSpeed;
uniform float VignettePower;
uniform float VignetteBrightness;
uniform float CrackBrightness;
uniform float AspectRatioCorrectionFactor;

in vec2 texCoord0;
out vec4 fragColor;

float InverseLerp(float from, float to, float x) {
    return clamp((x - from) / (to - from), 0, 1);
}

vec2 AspectRatioCorrect(vec2 coords) {
    return (coords - 0.5) * vec2(AspectRatioCorrectionFactor, 1) + 0.5;
}

vec2 CalculateRadiallyOffsetCoords(vec2 coords) {
    float originalRadius = distance(coords, vec2(0.5));
    float radius =  (originalRadius + RadialOffsetTime) - 3 * trunc((originalRadius + RadialOffsetTime) / 3);

    float offsetAngle = cos((GameTime * 30) + originalRadius * (coords.x > 0.5 ? 1 : -1) * 30 + (coords.x + 2) * 40) * 0.047;
    float angle = atan(coords.y - 0.5, coords.x - 0.5) + offsetAngle;
    return vec2(cos(angle), sin(angle)) * radius;
}

void main() {
    float crackOffset = texture(Sampler0, texCoord0 * 4.5 + vec2(0, (GameTime * 5500) * -AnimationSpeed * 0.6)).r;

    float backgroundCrackColor = texture(Sampler0, texCoord0 * 4.7 + -crackOffset * 0.19 + vec2((GameTime * 5500) * -AnimationSpeed * 0.67, 0.16)).r;
    float tendrilCrackColor = texture(Sampler0, CalculateRadiallyOffsetCoords(texCoord0) * 2 + crackOffset * 0.05).r;

    float crackColor = 1.2 - (backgroundCrackColor * 0.6 + tendrilCrackColor);

    float distanceToCenter = distance(AspectRatioCorrect(texCoord0), vec2(0.5));
    float vignetteInterpolant = clamp(pow(distanceToCenter, VignettePower) * VignetteBrightness + crackColor * 0.2, 0, 1) * 0.8;
    float blacknessInterpolant = vignetteInterpolant + pow(CrackBrightness, 2) * 0.9;

    float redInterpolant = clamp(pow(crackColor, 2), 0, 1) + vignetteInterpolant * 0.12;
    vec4 baseColor = vec4(redInterpolant * 0.3, redInterpolant * 0.014, tendrilCrackColor * 0.02, 1);

    float whiteAccent = crackColor * vignetteInterpolant * pow(CrackBrightness, 5) * 0.13;
    fragColor = (vec4(redInterpolant * 0.3, redInterpolant * 0.014, tendrilCrackColor * 0.02, 1) * blacknessInterpolant + whiteAccent) * CrackBrightness;
}