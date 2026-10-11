#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

uniform float GameTime;
const float BaseCutoffRadius = 0.1;
const float SwirlOutwardnessExponent = 0.15;
const float SwirlOutwardnessFactor = 3.67;
const float VanishInterpolant = 0;
const float EdgeColorBias = .15;
const vec4 EdgeColor = vec4(1, 0.08, 0.08, 1);

in vec2 texCoord0;
out vec4 fragColor;

void main() {
    //Calculate polar coordinates.
    float time = GameTime * 80;
    float angleFromCenter = atan(texCoord0.y - 0.5, texCoord0.x - 0.5);
    float distanceToCenter = distance(texCoord0, vec2(0.5));
    vec2 polar = vec2(distanceToCenter, angleFromCenter / 6.283 + 0.5);

    vec2 polarA = polar;
    polarA.y = fract(polarA.y + time * 1.91);

    vec2 polarB = polar;
    polarB.y = fract(polarB.y + time * -1.91);

    //Calculate noise textures and combine them together.
    float noiseA = texture(Sampler1, polarA * vec2(3, 1) + vec2(-3.8, 0) * time).r;
    float noiseB = texture(Sampler1, polarB * vec2(7, 1) + vec2(-2.78, 0) * time).r;
    float combinedNoise = dot(vec2(noiseA, noiseB), vec2(0.582, 0.813)) - 0.74;

    //Determine the erasure value.
    //Once this meets or exceeds 1, a pixel is erased.
    float erasureValue = distanceToCenter / BaseCutoffRadius;
    erasureValue += pow(erasureValue, SwirlOutwardnessExponent) * combinedNoise * clamp(0.15 / distanceToCenter, 0.0, 1.0) * SwirlOutwardnessFactor * (1 - VanishInterpolant);
    erasureValue += VanishInterpolant * 1.6;

    //Calculate the base color and ensure that edges receive a good amount of red.
    vec4 baseColor = texture(Sampler0, vec2(polarA) + texCoord0 * 0.5) * float(erasureValue < 1);
    baseColor.rgb *= smoothstep(0.05, 0.187, distanceToCenter);
    baseColor += EdgeColor * smoothstep(0.72, 0.9, erasureValue + EdgeColorBias) * baseColor.a;

    fragColor = baseColor;
    if(fragColor.a < .1) discard;
}