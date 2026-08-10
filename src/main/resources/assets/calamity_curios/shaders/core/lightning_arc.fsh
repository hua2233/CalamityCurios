#version 150

uniform sampler2D Sampler0;
uniform float GameTime;

in vec4 vertexColor;
in vec3 texCoord0;

out vec4 fragColor;

void main() {
    vec2 coords = texCoord0.xy;
    coords.y = (texCoord0.y - .5) / texCoord0.z + .5;
    float distortion = mix(-1,  1, texture(Sampler0, coords + vec2(0, (GameTime * -600) * sign(coords.y > 0.5 ? -1 : 1) * 1.81)).r);
    float opacity = pow(sin((coords.y + distortion * 0.15) * 3.141), distortion * 3.95 + 7);
    fragColor = vertexColor * vec4(pow(opacity, 0.25)) + vec4(opacity);
    if(fragColor.r < 0.02 && fragColor.g < 0.02 && fragColor.b < 0.02) discard;
}
