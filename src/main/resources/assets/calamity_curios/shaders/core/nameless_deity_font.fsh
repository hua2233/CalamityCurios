#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

uniform float GameTime;
uniform float ChromaticAberrationOffset;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec2 TextureSize = textureSize(Sampler0, 0);
    vec2 pixelationFactor = 1 / TextureSize;
    vec2 coords = floor(texCoord0 / pixelationFactor) * pixelationFactor;

    // Calculate values for glitch noise.
    // This will be used to offset the texture in bar-like formations.
    float timeStepFactor = 11;
    float time = GameTime * 400;
    float glitchPixelationFactor = 6;
    float glitchCoords = floor(coords.y * glitchPixelationFactor) / glitchPixelationFactor + floor(time * timeStepFactor) / timeStepFactor;
    float glitchNoise = texture(Sampler1, vec2(glitchCoords)).r;

    glitchCoords = floor(coords.y * glitchPixelationFactor * 0.5) / glitchPixelationFactor + floor(time * timeStepFactor * 1.5) / timeStepFactor;
    glitchNoise = (glitchNoise + texture(Sampler1, vec2(glitchCoords)).r) * 0.67;

    glitchCoords = floor(coords.y * glitchPixelationFactor * 0.4) / glitchPixelationFactor + floor(time * timeStepFactor * 1.3) / timeStepFactor;
    glitchNoise = (glitchNoise + texture(Sampler1, vec2(glitchCoords)).r) * 0.62;

    // Perform the glitch offset and re-pixelate.
    coords.x += smoothstep(0.8, 1, glitchNoise) * 0.32;
    coords = round(coords / pixelationFactor) * pixelationFactor;

    //Color dispersion
    float r = texture(Sampler0, coords + vec2(-0.707, 0) / TextureSize * ChromaticAberrationOffset).r;
    float g = texture(Sampler0, coords + vec2(0.707, 0) / TextureSize * ChromaticAberrationOffset).g;
    float b = texture(Sampler0, coords + vec2(0, 0.707) / TextureSize * ChromaticAberrationOffset).b;
    vec4 color = vec4(r, g, b, texture(Sampler0, coords).a);

    fragColor = color * vertexColor;
}