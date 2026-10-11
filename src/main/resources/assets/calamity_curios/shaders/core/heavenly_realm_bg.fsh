#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform float GameTime;
uniform vec3 FrontStarColor;
uniform vec3 BackStarColor;

in vec2 texCoord0;
in vec4 vertexColor;
out vec4 fragColor;

void main() {
    vec4 result = vec4(0);
    float volumetricLayerFade = 1.0;
    float time = GameTime * 1500;
    float distanceFromBottom = distance(texCoord0.y, 1);
    float detailIterationsClamped = 19;
    vec2 offset = vec2(0.22, 0.05);

    for (int i = 0; i < detailIterationsClamped; i++) {
        float scrollTime = time * pow(volumetricLayerFade, 2) * 3;
        vec2 p = (texCoord0 - offset) * 0.214 + offset;
        p.y += 1.5;

        // Perform scrolling behaviors. Each layer should scroll a bit slower than the previous one, to give an illusion of 3D.
        p += vec2(scrollTime * 0.0015, scrollTime * 0.0015);
        p /= volumetricLayerFade;

        float totalChange = texture(Sampler0, p).r;
        vec4 layerColor = vec4(mix(FrontStarColor, BackStarColor, i / detailIterationsClamped), 1.0);
        result += layerColor * totalChange * volumetricLayerFade;

        // Make the next layer exponentially weaker in intensity.
        volumetricLayerFade *= 0.92;
    }

    // Apply color change interpolants. This will be used later.
    float colorChangeBrightness1 = texture(Sampler1, texCoord0 * 1.5).r;
    float colorChangeBrightness2 = texture(Sampler1, texCoord0 * 1.65 + time * 0.0015).r;
    float totalColorChange = colorChangeBrightness1 + colorChangeBrightness2;

    // Account for the accumulated scale from the fractal noise.
    result.rgb = pow(result.rgb * 0.010714, vec3(2.64 - totalColorChange * 1.4 + pow(distanceFromBottom, 3.0) * 3.9)) * 0.57;

    // Apply color changing accents to the result, to keep it less homogenous.
    result.rgb += vec3(-2.1, 0.5, 2.15) * dot(result.rgb, vec3(0.3333)) * colorChangeBrightness1 * .6;
    result.rgb += vec3(2.4, -0.9, 5.8) * dot(result.rgb, vec3(0.3333)) * pow(colorChangeBrightness2, 4) * .81;

    fragColor = result * vertexColor;
}