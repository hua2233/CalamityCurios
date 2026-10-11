#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D Sampler2;

uniform float GameTime;

const float CoronaIntensityFactor = 0.05;
const vec4 MainColor = vec4(1);
const vec3 DarkerColor = vec3(.8, .3608, .0980);
const vec3 SubtractiveAccentFactor = vec3(0.7098, 0, 0);

in vec2 texCoord0;
out vec4 fragColor;

float InverseLerp(float from, float to, float x) {
    return clamp((x - from) / (to - from), 0.0, 1.0);
}

void main() {
    vec2 coordsNormalizedToCenter = texCoord0 * 2 - 1;
    float distanceFromCenterSqr = dot(coordsNormalizedToCenter, coordsNormalizedToCenter) * 2;
    float starOpacity = InverseLerp(0.5, 0.42, distanceFromCenterSqr);

    float spherePinchFactor = (1 - sqrt(abs(1 - distanceFromCenterSqr))) / distanceFromCenterSqr + 0.045;
    vec2 sphereCoords = texCoord0 * spherePinchFactor + vec2(GameTime * 450, 0);

    // Calculate the star brightness texture from the sphere coordinates.
    float starCoordsOffset = texture(Sampler0, sphereCoords).r * 0.41 + GameTime * 200;
    vec2 starCoords = sphereCoords + vec2(starCoordsOffset, 0);
    vec3 starBrightnessTexture = texture(Sampler0, starCoords).rgb;

    float starGlow = clamp(1 - distanceFromCenterSqr * 0.91, 0.0, 1.0);

    vec3 result = spherePinchFactor * MainColor.rgb * 0.777 + starGlow * DarkerColor + starBrightnessTexture;

    result = mix(result, DarkerColor, clamp(1 - starBrightnessTexture.r, 0.0, 1.0) * 0.8);
    result -= (1 - SubtractiveAccentFactor) * texture(Sampler1, sphereCoords * 2).r * 1.1;

    vec2 uvOffset = texture(Sampler2, texCoord0 + vec2(0, GameTime * 260)).rg;
    result += pow(texture(Sampler1, sphereCoords * 1.2 + uvOffset * 0.04).r, 2) * 2.1;

    float coronaFadeOut = InverseLerp(0.2, 0.5, distanceFromCenterSqr) * InverseLerp(1.91, 0.98, distanceFromCenterSqr) * CoronaIntensityFactor;
    float coronaBrightness = coronaFadeOut / abs(distanceFromCenterSqr - 0.5 + uvOffset.y * 0.04 + 0.04);

    fragColor = (starOpacity * vec4(result, 1) + MainColor * coronaBrightness);
}