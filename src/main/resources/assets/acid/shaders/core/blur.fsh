#version 150

uniform sampler2D InputSampler;
uniform vec2 InputResolution;
uniform vec2 uSize;
uniform vec2 uLocation;

uniform float radius;
uniform float Brightness;
uniform float Quality;
uniform vec4 color1;

in vec2 texCoord;
out vec4 fragColor;

float roundedBoxSDF(vec2 center, vec2 size, float radius) {
    return length(max(abs(center) - size + radius, 0.0)) - radius;
}

vec4 blur() {
    #define TAU 6.28318530718
    #define NUM_SAMPLES 16

    vec2 Radius = Quality / InputResolution.xy;
    vec2 uv = gl_FragCoord.xy / InputResolution.xy;
    vec4 Color = texture(InputSampler, uv);

    // Precompute the step size and direction for the blur loop
    float step = TAU / float(NUM_SAMPLES);
    vec2 offset;

    for (int d = 0; d < NUM_SAMPLES; ++d) {
        // Sample texture in a circular pattern
        float angle = float(d) * step;
        vec2 direction = vec2(cos(angle), sin(angle));

        // Accumulate texture samples at different radii
        for (float i = 0.2; i <= 1.0; i += 0.2) {
            offset = direction * Radius * i;
            Color += texture(InputSampler, uv + offset);
        }
    }

    Color /= float(NUM_SAMPLES) * 4.0;  // Averaging the results
    return (Color + color1) * Brightness;
}

void main() {
    vec2 halfSize = uSize / 2.0;
    float smoothedAlpha =  (1.0 - smoothstep(0.0, 1.0, roundedBoxSDF(gl_FragCoord.xy - uLocation - halfSize, halfSize, radius)));
    fragColor = vec4(blur().rgb, smoothedAlpha);
}
