precision mediump float; 

uniform vec4 inputColor;
uniform float radius;
uniform float border;

varying vec2 textureCoordinate;

void main()
{
	float dist = 1.0 - 2.0 * distance( textureCoordinate, vec2( 0.5, 0.5 ) );
	float t = min( max( dist/(border/radius), 0.0 ), 1.0 );
	
    gl_FragColor = inputColor * t;
} 


