#version 150

// Uniforms
uniform sampler2D Sampler0;
uniform float GameTime;

in vec4 vertexColor;
in vec3 texCoord0;

out vec4 fragColor;

void main() {
    vec2 coords = texCoord0.xy;

    coords.y = (coords.y - 0.5) / texCoord0.z + 0.5;

    vec4 fadeMapColor = texture(Sampler0, vec2(fract(coords.x - GameTime / 20 * 2.5), coords.y));
    float opacity = fadeMapColor.r;

    float power = mix(3.0, 10.0, coords.x);
    opacity = mix(pow(sin(coords.y * 3.141), power), opacity, coords.x);

    if (coords.x > 0.85) {
        opacity *= pow(1.0 - (coords.x - 0.85) / 0.3, 6.0);
    }

    fragColor = vertexColor * opacity * 1.5;
}
