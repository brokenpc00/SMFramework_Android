uniform mat4 projection;
uniform mat4 model;
uniform vec4 inputColor;

uniform float texelWidthOffset;
uniform float texelHeightOffset;

attribute vec4 position;
attribute vec2 inputTextureCoordinate;

varying vec2 textureCoordinate;

#define GAUSSIAN_SAMPLES 9
varying vec2 blurCoordinates[GAUSSIAN_SAMPLES];

uniform vec2 iResolution;
uniform vec2 iCenter;
uniform vec2 iRadius;
uniform float iBorder;

varying vec2 center;
varying float aspect;
varying float border;
varying float invRadius;

void main()
{
	gl_Position = projection * model * position;
	textureCoordinate = inputTextureCoordinate.xy;
	
	center = iCenter / iResolution;
	if (iRadius.x > 0.0 && iRadius.y > 0.0) { 
		aspect = iRadius.x / iRadius.y;
		border = iBorder / iRadius.x;
		invRadius = 1.1 * iResolution.x / iRadius.x;
	} else {
		aspect = 1.0;
		border = 1.0;
		invRadius = 0.0;
	}
	
	int multiplier = 0;
	vec2 blurStep;
	vec2 singleStepOffset = vec2(texelWidthOffset, texelHeightOffset);
	
	for (int i = 0; i < GAUSSIAN_SAMPLES; i++) {
	    multiplier = (i - ((GAUSSIAN_SAMPLES - 1) / 2));
	    blurStep = float(multiplier) * singleStepOffset;
	    blurCoordinates[i] = inputTextureCoordinate.xy + blurStep;
	}
}
