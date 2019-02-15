precision mediump float; 

uniform sampler2D inputImageTexture;
uniform lowp vec4 inputColor;

varying vec2 textureCoordinate;


uniform lowp float brightness;
uniform lowp float contrast;
uniform lowp float saturate;

uniform lowp float temperature;
uniform lowp float tint;

const lowp vec3 warmFilter = vec3(0.93, 0.54, 0.0);

const mediump mat3 RGBtoYIQ = mat3(0.299, 0.587, 0.114, 0.596, -0.274, -0.322, 0.212, -0.523, 0.311);
const mediump mat3 YIQtoRGB = mat3(1.0, 0.956, 0.621, 1.0, -0.272, -0.647, 1.0, -1.105, 1.702);

vec3 ContrastSaturationBrightness(vec3 color, float brt, float sat, float con)
{
	const float AvgLumR = 0.5;
	const float AvgLumG = 0.5;
	const float AvgLumB = 0.5;
	
	const vec3 LumCoeff = vec3(0.2125, 0.7154, 0.0721);
	
	vec3 AvgLumin = vec3(AvgLumR, AvgLumG, AvgLumB);
	vec3 brtColor = color * brt;
	vec3 intensity = vec3(dot(brtColor, LumCoeff));
	vec3 satColor = mix(intensity, brtColor, sat);
	vec3 conColor = mix(AvgLumin, satColor, con);
	
	return conColor;
}

void main()
{
	vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);
	vec3 source = ContrastSaturationBrightness(textureColor.rgb, brightness, saturate, contrast);
	
	mediump vec3 yiq = RGBtoYIQ * source;
	yiq.b = clamp(yiq.b + tint*0.5226*0.1, -0.5226, 0.5226);
	lowp vec3 rgb = YIQtoRGB * yiq;

	lowp vec3 processed = vec3(
		(rgb.r < 0.5 ? (2.0 * rgb.r * warmFilter.r) : (1.0 - 2.0 * (1.0 - rgb.r) * (1.0 - warmFilter.r))), //adjusting temperature
		(rgb.g < 0.5 ? (2.0 * rgb.g * warmFilter.g) : (1.0 - 2.0 * (1.0 - rgb.g) * (1.0 - warmFilter.g))), 
		(rgb.b < 0.5 ? (2.0 * rgb.b * warmFilter.b) : (1.0 - 2.0 * (1.0 - rgb.b) * (1.0 - warmFilter.b))));

	gl_FragColor = vec4(mix(rgb, processed, temperature), textureColor.a) * inputColor;	
}


