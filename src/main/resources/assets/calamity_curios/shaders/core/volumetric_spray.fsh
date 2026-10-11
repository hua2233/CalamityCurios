#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

uniform float GameTime;
uniform mat4 ModelViewMat;
uniform mat3 IViewRotMat;
uniform float HalfSize;
uniform float JetLength;
uniform vec3 NozzlePos;
uniform vec3 JetDir;

in vec3 viewPos;
out vec4 fragColor;

float grain(vec3 lp, vec2 rnd) {
    vec2 c = lp.xy / (2.0 * HalfSize) + 0.5;// 截面归一化 0..1
    float zn = lp.z / (2.0 * HalfSize);
    vec2 jitter = rnd * 0.04;
    float n1 = texture(Sampler1, c * 0.12 + jitter + vec2(zn * 0.12)).r;
    float n2 = texture(Sampler1, c * 0.14 + jitter.yx + vec2(zn * 0.14)).r;
    return sqrt(n1 * n2);
}

vec2 rayBox(vec3 ro, vec3 rd, vec3 bmin, vec3 bmax) {
    vec3 inv = 1.0 / rd;
    vec3 t0 = (bmin - ro) * inv, t1 = (bmax - ro) * inv;
    vec3 tsm = min(t0, t1), tbg = max(t0, t1);
    return vec2(max(max(tsm.x, tsm.y), tsm.z), min(min(tbg.x, tbg.y), tbg.z));
}

float noise(vec2 uv) { return texture(Sampler0, uv).r; }

// lp：喷口局部系（x、y 为截面，z 沿喷射方向，0=喷口，JetLength=远端）
float density(vec3 lp) {
    float t = lp.z / JetLength;
    float radial = length(lp.xy);

    float coneR = HalfSize * mix(0.04, 0.82, pow(t, 1.15));

    float ft = GameTime * 1000;
    float zs = lp.z / HalfSize * 1.4 - ft;
    float n = 0.0;
    n += noise(vec2(lp.x / HalfSize * 1.2, zs));
    n += noise(vec2(lp.y / HalfSize * 1.2, zs) + 5.7);
    n += noise(lp.xy / HalfSize * 1.2 + vec2(ft * 0.15));
    n *= 0.3333;

    float w1 = noise(vec2(lp.y / HalfSize * 1.6, lp.z / HalfSize - ft * 0.8) + 11.3);
    float w2 = noise(vec2(lp.x / HalfSize * 1.6, lp.z / HalfSize - ft * 0.8) + 7.9);
    float effR = radial - (0.5 * (w1 + w2) - 0.5) * HalfSize * 0.55 * (0.25 + 0.75 * t);

    float inCone = 1.0 - smoothstep(coneR * 0.45, coneR, effR);
    float puff = mix(1.0, smoothstep(0.34, 0.64, n), smoothstep(0.2, 0.5, t) * 0.7);
    float lengthFade = 1.0 - smoothstep(0.55, 0.8, t);
    float core = exp(-(effR * effR) / (coneR * coneR * 0.18 + 1e-5)) * (1.0 - smoothstep(0.0, 0.75, t));

    return inCone * puff * lengthFade + core * 0.55 * lengthFade;
}

void main() {
    // 喷口与轴向：用同一个 ModelViewMat 变到视空间，与 viewPos 保持同系
    vec3 jd = normalize(mat3(ModelViewMat) * (transpose(IViewRotMat) * normalize(JetDir)));
    vec3 helper = abs(jd.y) < 0.99 ? vec3(0.0, 1.0, 0.0) : vec3(1.0, 0.0, 0.0);
    vec3 t1 = normalize(cross(jd, helper));
    vec3 t2 = cross(jd, t1);
    vec3 nozzleV = (ModelViewMat * vec4(NozzlePos, 1.0)).xyz;

    // 视空间里相机恒在原点，射线 = (原点 -> 片元)
    vec3 ro = vec3(0.0);
    vec3 rd = normalize(viewPos);

    vec3 rel = ro - nozzleV;
    vec3 roL = vec3(dot(rel, t1), dot(rel, t2), dot(rel, jd));
    vec3 rdL = vec3(dot(rd, t1), dot(rd, t2), dot(rd, jd));
    vec2 tb = rayBox(roL, rdL, vec3(-HalfSize, -HalfSize, 0.0), vec3(HalfSize, HalfSize, JetLength));
    float tStart = max(tb.x, 0.0), tEnd = tb.y;
    if (tEnd <= tStart) discard;

    const int STEPS = 48;
    vec3 entry = roL + rdL * tStart;

    float stepLen = (tEnd - tStart) / float(STEPS);
    float jit = fract(sin(dot(entry, vec3(127.1, 311.7, 74.7))) * 43758.5453);

    vec3 stepL = rdL * stepLen;
    vec3 posL = roL + rdL * (tStart + stepLen * jit);

    float absorb = 4.6;
    vec4 accum = vec4(0.0);
    float tickNow = floor(GameTime * 24000.0);
    vec2 rnd = vec2(fract(sin(tickNow * 12.9898) * 43758.5453), fract(sin(tickNow * 78.233) * 12543.1233));

    for (int i = 0; i < STEPS; i++) {
        float d = density(posL);
        if (d > 0.001) {
            float a = 1.0 - exp(-d * absorb * stepLen);
            float s = grain(posL, rnd);
            vec3 color = vec3(clamp(s * 1.8 - 0.25, 0.0, 1.0));
            accum.rgb += (1.0 - accum.a) * a * color;
            accum.a   += (1.0 - accum.a) * a;
            if (accum.a > 0.98) break;
        }
        posL += stepL;
    }

    if (accum.a < 0.01) discard;

    float alpha = smoothstep(0.06, 0.75, accum.a);
    vec3 straight = accum.rgb / max(accum.a, 1e-4);
    fragColor = vec4(straight * alpha, alpha);
}