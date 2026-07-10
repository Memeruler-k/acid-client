#version 150

uniform sampler2D InputSampler;
uniform vec2 InputResolution;
uniform vec2 uSize;
uniform vec2 uLocation;

uniform float radius;
uniform float Quality;

in vec2 texCoord;
out vec4 fragColor;

vec4 blur() {
    #define TAU 6.28318530718
    #define NUM_SAMPLES 16

    vec2 Radius = Quality / InputResolution.xy;
    vec2 uv = gl_FragCoord.xy / InputResolution.xy;
    vec4 Color = texture(InputSampler, uv);

    float step = TAU / float(NUM_SAMPLES);
    vec2 offset;

    for (int d = 0; d < NUM_SAMPLES; ++d) {
        float angle = float(d) * step;
        vec2 direction = vec2(cos(angle), sin(angle));
        for (float i = 0.2; i <= 1.0; i += 0.2) {
            offset = direction * Radius * i;
            Color += texture(InputSampler, uv + offset);
        }
    }

    Color /= float(NUM_SAMPLES) * 4.0;
    return Color;
}

void main() {
    vec2 halfSize = uSize / 2.0;
    float smoothedAlpha =  (1.0 - smoothstep(0.0, 1.0, length(max(abs(gl_FragCoord.xy - uLocation - halfSize) - halfSize + radius, 0.0)) - radius));
    fragColor = vec4(blur().rgb, smoothedAlpha);
}
