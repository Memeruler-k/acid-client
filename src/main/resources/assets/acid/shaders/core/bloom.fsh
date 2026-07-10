#version 140

uniform sampler2D InputSampler;
uniform vec2 InputResolution;
uniform vec4 color1;
uniform float uSize;
uniform vec2 uLocation;
uniform float radius;

in vec4 v_colour;
in vec2 tex_coord;
out vec4 pixel;

void main() {
    ivec2 size = textureSize(InputSampler, 0);
    vec2 texSize = vec2(size.x, size.y);

    vec4 sum = vec4(0.0);
    vec2 uv = tex_coord * texSize;

    for (int n = -4; n <= 4; ++n) {
        vec2 offset = vec2(float(n) * uSize, 0.0);
        sum += texelFetch(InputSampler, ivec2(uv + offset), 0);
    }

    vec4 finalSum = vec4(0.0);
    for (int n = -4; n <= 4; ++n) {
        vec2 offset = vec2(0.0, float(n) * uSize);
        finalSum += texelFetch(InputSampler, ivec2(uv + offset), 0);
    }

    sum /= 9.0;
    finalSum /= 9.0;

    pixel = texture(InputSampler, tex_coord) - ((sum + finalSum) * radius * color1);
}
