#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform float GameTime;

uniform float BlackHoleRadius;
uniform float AccretionDiskRadius;
uniform float AspectRatioCorrectionFactor;
uniform float CameraAngle;
uniform vec2 Zoom;
uniform vec3 CameraRotationAxis;
uniform vec3 BlackHoleCenter;
uniform vec3 AccretionDiskColor;
uniform vec3 AccretionDiskScale;

in vec2 texCoord0;
out vec4 fragColor;

float QuadraticBump(float x) {
    return x * (4 - x * 4);
}

float Hash13(vec3 p) {
    return fract(sin(dot(p, vec3(12.9898, 78.233, 51.9852))) * 30000);
}

float SignedTorusDistance(vec3 p, vec2 t){
    vec2 q = vec2(length(p.xz) - t.x, p.y);
    return length(q) - t.y;
}

vec2 RotatedBy(vec2 v, float theta){
    float s = sin(theta);
    float c = cos(theta);
    return vec2(v.x * c - v.y * s, v.x * s + v.y * c);
}

vec4 Sample(vec3 position) {
    // Calculate the amount of glow for the accretion disk based on the distance relative to a torus surrounding the black hole.
    vec3 offsetFromBlackHole = (position - BlackHoleCenter) / AccretionDiskScale;
    float accretionDiskDistance = -SignedTorusDistance(offsetFromBlackHole, vec2(0.75, AccretionDiskRadius));
    float accretionDiskGlow = pow(max(0, accretionDiskDistance / AccretionDiskRadius), 0.9);

    // Apply some noise to the glow calculation to make it feel less artifically halo-y.
    vec2 radial = vec2(atan(offsetFromBlackHole.x, offsetFromBlackHole.z) / 6.283 + 0.5, length(offsetFromBlackHole));
    accretionDiskGlow *= texture(Sampler1, radial * vec2(3, 3.5) + (GameTime * 600) * vec2(6.3, -2)).r;

    // Combine the results with the base accretion disk color.
    vec4 accretionDiskColorWithAlpha = vec4(pow(clamp(AccretionDiskColor, 0.0, 1.0), vec3(1.1)), 1) * accretionDiskGlow * 0.75;

    return mix(accretionDiskColorWithAlpha, vec4(0, 0, 0, 1), smoothstep(0.01, 0, length(offsetFromBlackHole) - BlackHoleRadius));
}

vec3 RodriguesRotation(vec3 v, vec3 axis, float angle){
    float c = cos(angle);
    float s = sin(angle);
    return v * c + cross(v, axis) * s + axis * dot(axis, v) * (1 - c);
}

void main() {
    vec2 baseCoords = texCoord0;
    vec2 coords = texCoord0;

    // Rearrage coordinates into a -1 to 1 UV range.
    coords = (texCoord0 - 0.5) * vec2(AspectRatioCorrectionFactor, 1) + 0.5;
    coords = coords * 2 - 1;

    // Initialize positional information.
    float capturedLightInterpolant = 0;
    vec3 samplePoint = vec3(coords / Zoom, -0.9);
    vec3 standardLightPositionIncrement = vec3(0, 0, 1);
    float distanceFromBlackHole = 0;
    float distanceFromBlackHoleEdge = 0;

    vec3 startingSamplePoint = vec3(samplePoint.xy, 0);

    // Apply camera rotation.
    samplePoint = RodriguesRotation(samplePoint - BlackHoleCenter, CameraRotationAxis, CameraAngle) + BlackHoleCenter;
    standardLightPositionIncrement = RodriguesRotation(standardLightPositionIncrement, CameraRotationAxis, CameraAngle);

    // Slightly nudge the starting sample point around a touch to make banding artifacts from the limited step count virtually unnoticeable.
    samplePoint += standardLightPositionIncrement * Hash13(samplePoint * 10 + (GameTime * 600)) * 0.0175;

    vec4 result = vec4(0);
    vec2 distortionOffset = vec2(0);
    for (float i = 0; i < 75; i++) {
        // Calculate the distance from the black hole and its edge.
        distanceFromBlackHole = distance(samplePoint, BlackHoleCenter);
        distanceFromBlackHoleEdge = distanceFromBlackHole - BlackHoleRadius;

        // Determine how much light was captured by the black hole on this update step.
        capturedLightInterpolant = smoothstep(0.01, -0.1, distanceFromBlackHoleEdge);

        // Move the sample point forward and towards the black hole based on proximity.
        float step = mix(0.02, 0.021, 1 - QuadraticBump(i / 75));
        float distortionIntensity = clamp(0.005 / pow(distanceFromBlackHole, 2), 0, 0.1) * BlackHoleRadius;
        vec3 distortion = normalize(BlackHoleCenter - samplePoint) * distortionIntensity;

        samplePoint += distortion;
        samplePoint += standardLightPositionIncrement * (1 - capturedLightInterpolant) * step;

        // Accumulate total distortion offsets in the lightmarch for later.
        distortionOffset += distortion.xy;

        // Additively apply color samples to the result.
        // This determines the base of the accretion disk's color.
        result += Sample(samplePoint);
    }

    // Apply glow effects around the black hole.
    float glowAttenuation = smoothstep(5, 0, distanceFromBlackHole / BlackHoleRadius);
    vec4 accretionDiskColorWithAlpha = vec4(AccretionDiskColor, 1);
    result += clamp(0.3 / pow(distanceFromBlackHole, 3) * accretionDiskColorWithAlpha, 0, 2) * glowAttenuation * mix(vec4(1), accretionDiskColorWithAlpha * 0.12, capturedLightInterpolant);

    float glowDistance = (distance(startingSamplePoint, BlackHoleCenter) - BlackHoleRadius * 1.7);
    result += 0.015 / abs(glowDistance) * smoothstep(0.2, 0.1, glowDistance);

    // Apply gravitational lensing UV effects.
    float angleOffset = length(distortionOffset) * 50 - GameTime * 3600;
    vec2 blackHolePosition2D = (BlackHoleCenter.xy + 1) * 0.5;
    vec2 rotatedCoords = RotatedBy(baseCoords - blackHolePosition2D, angleOffset) + blackHolePosition2D;
    vec2 interpolatedCoords = mix(baseCoords, rotatedCoords, smoothstep(0.125, 0.3, length(distortionOffset))) + distortionOffset;

    fragColor = texture(Sampler0, interpolatedCoords) * (1 - capturedLightInterpolant) + result;//
    if (fragColor.a < 0.1) discard;
}