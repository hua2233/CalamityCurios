#version 150

uniform sampler2D Sampler0;

uniform vec3 ShieldEdgeColor;
uniform float NoiseScale;
uniform float GameTime;

//The power the blow up effect is set as. Ideally betwen 0 and 1
const float BlowUpPower = 2.5;
//The strenght of the expansion caused by the blow up effect
const float BlowUpSize = 0.5;
const float ShieldEdgeBlendStrenght = 4.0;
const vec3 ShieldColor = vec3(51.0 / 255.0, 102.0 / 255.0, 1.0);

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    //Chebyshev Distance
    float distanceFromCenter = max(abs(texCoord0.x - 0.5), abs(texCoord0.y - 0.5)) * 2.0;
    if (distanceFromCenter > 1) discard;

    //"Blow up" the noise map so it looks circular.
    float blownUpUVX = pow((abs(texCoord0.x - 0.5)) * 2, BlowUpPower);
    float blownUpUVY = pow((abs(texCoord0.y - 0.5)) * 2, BlowUpPower);
    vec2 blownUpUV = vec2(-blownUpUVY * BlowUpSize * 0.5 + texCoord0.x * (1 + blownUpUVY * BlowUpSize), -blownUpUVX * BlowUpSize * 0.5 + texCoord0.y * (1 + blownUpUVX * BlowUpSize));

    //Rescale
    blownUpUV *= NoiseScale;

    //Scroll effect
    blownUpUV.x = fract(blownUpUV.x + GameTime * 10);

    //Get the noise color
    vec4 noiseColor = texture(Sampler0, blownUpUV);

    //Apply a layers of fake fresnel
    noiseColor += pow(distanceFromCenter, 6);
    //Fade the edges
    if (distanceFromCenter > 0.95)
    noiseColor *= (1 - ((distanceFromCenter - 0.95) / 0.05));

    fragColor = noiseColor * vec4(mix(ShieldColor, ShieldEdgeColor, pow(distanceFromCenter, ShieldEdgeBlendStrenght)), vertexColor.a);
}
