#version 150

in vec4 vertexColor;
in vec2 texCoord0;

uniform sampler2D Sampler0;
uniform float GameTime;

out vec4 fragColor;

void main() {
    if (any(greaterThan(vertexColor, vec4(0.0)))) {
        // Temporal drift to make the sprite slide through the noise texture
        vec2 drift = vec2(5 * sin(22 * GameTime), -2800 * GameTime);

        vec2 noiseMapTexCoords = texCoord0 * vec2(2.5, 1) + drift;
        vec4 noiseColor = (texture(Sampler0, noiseMapTexCoords * 1.1) + texture(Sampler0, noiseMapTexCoords * 0.76)) * 0.5;

        // Define thresholds for total pixel erasure and glowing lines.
        //
        // Rapidly flickering sinewave produced by Desmos, loosely based on the Weierstrass function
        // (infinitely sharp vague sinewave, periodic, continuous everywhere but differentiable nowhere)
        // https://en.wikipedia.org/wiki/Weierstrass_function
        float flickerOne = cos(GameTime * 700) * 0.05;
        float flickerTwo = cos(GameTime * 3100) * 0.06;
        float flickerThree = sin(GameTime * 16700) * 0.04;
        float fullErasureThreshold = 0.61f + flickerOne + flickerTwo + flickerThree;
        float glowThreshold = fullErasureThreshold - 0.1;

        // If the noise over the erasure threshold, completely erase this pixel.
        if (noiseColor.r > fullErasureThreshold) {
            fragColor = vec4(1);
            return;
        }

        // Otherwise, if it's over the slightly lower threshold, replace it with a bright color.
        else if (noiseColor.r > glowThreshold)
        {
            // Ensure it accounts for the original alpha.
            fragColor = vec4(0.4902, 1, 0, 1) * vertexColor.a;
            return;
        }
    }

    fragColor = vertexColor;
}