#version 150

in vec4 vertexColor;
in vec2 texCoord0;

uniform sampler2D Sampler0;
uniform float GameTime;
uniform mat2 Transformation;

out vec4 fragColor;

float InverseLerp(float from, float to, float x) {
    return clamp((x - from) / (to - from), 0.0, 1.0);
}

vec2 RotatedBy(vec2 v, float theta){
    float s = sin(theta);
    float c = cos(theta);
    return vec2(v.x * c - v.y * s, v.x * s + v.y * c);
}

void main() {
// Apply the matrix transformation to the coordinates firstly.
    vec2 coords = (texCoord0 - 0.5) * Transformation + 0.5;
    vec4 color = vec4(0);

// Create spiral arms based on the classic DoG galaxy spin effect.
    float distanceFromCenter = length(coords - 0.5);
    float angle = distanceFromCenter * 11.6 - GameTime * 370;
    color += texture(Sampler0, RotatedBy(coords - 0.5, angle) + 0.5);

// Make the galaxy fade out at the edges.
    color *= exp(distanceFromCenter * -7.4) * 3;

// Apply the sample color.
    color *= vertexColor;

// Make the center of the galaxy stronger.
    fragColor = color + InverseLerp(0.16, 0.04, distanceFromCenter) * vertexColor.a;
}