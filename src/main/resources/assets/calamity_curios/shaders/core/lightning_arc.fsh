#version 150

uniform sampler2D Sampler0;
uniform float GameTime;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    float distortion = mix(-1,  1, texture(Sampler0, texCoord0 + vec2(0, (GameTime * -600) * sign(texCoord0.y > 0.5 ? -1 : 1) * 1.81)).r);
    float opacity = pow(sin((texCoord0.y + distortion * 0.15) * 3.141), distortion * 3.95 + 7);
    fragColor = vertexColor * vec4(pow(opacity, 0.25)) + vec4(opacity);
    if(fragColor.r < 0.02 && fragColor.g < 0.02 && fragColor.b < 0.02) discard;
}
