#version 150

uniform sampler2D Sampler0;
uniform float GameTime;

in vec3 worldPos;
in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;
in vec4 normal;

out vec4 fragColor;

void main() {
    vec4 baseColor = texture(Sampler0, texCoord0);
    //剔除掉那些不被渲染的
    if (baseColor.a < 0.1) discard;

    vec2 textureSize = textureSize(Sampler0, 0);
    float overallAdjustedYCoord = 0.5 + mix(-textureSize.y / 114.0, textureSize.y / 114.0, texCoord0.y) * 0.5;

    float colorFade = abs(sin(overallAdjustedYCoord + GameTime * 600));
    float luminosity = (baseColor.r + baseColor.g + baseColor.b) / 3;

    vec4 endColor = baseColor * vec4(mix(vec3(vertexColor.rgb), vec3(0.0, 0.0, 1.0), colorFade), 1);
    endColor *= 1 + luminosity * 0.5;
    fragColor = (endColor * 0.7 + baseColor * 0.5) * baseColor.a * vertexColor.a;
}