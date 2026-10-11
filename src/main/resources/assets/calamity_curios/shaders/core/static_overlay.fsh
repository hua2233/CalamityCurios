#version 150

#moj_import <fog.glsl>

const vec4 ReserveColor = vec4(0, 0, 0, 1);

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

uniform vec4 Zoom;
uniform float Rate;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float FogStart;
uniform float GameTime;

in vec2 texCoord0;
in vec4 vertexColor;
in float vertexDistance;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0);

    if(color.a < .1f) discard;
    else if(color != ReserveColor) fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
    else {
        //略微缩放一点点不需要太大的采样
        vec2 ZoomCoord = vec2((Zoom.x - texCoord0.x) / Zoom.y * .3, (Zoom.z - texCoord0.y) / Zoom.w * .3);

        float t = GameTime * Rate;
        float staticOverlay = texture(Sampler1, ZoomCoord * 0.5 * 0.5 + t * 0.5 * 17.494754).r;
        staticOverlay = sqrt(staticOverlay * texture(Sampler1, ZoomCoord * 0.5 * 0.55 + t * 0.5 * 19.1)).r;

        float staticAdditive = pow(staticOverlay, 1.5) * color.a;
        vec4 staticColor = color + staticAdditive * 0.45;
        fragColor = linear_fog(mix(staticColor, vec4(staticAdditive, staticAdditive, staticAdditive, 1), 1.1), vertexDistance, FogStart, FogEnd, FogColor);
    }
}