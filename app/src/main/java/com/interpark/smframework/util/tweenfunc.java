package com.interpark.smframework.util;

public class tweenfunc {

    public static final double M_PI = Math.PI;
    public static final double M_PI_2 = Math.PI/2;
    public static final double M_PI_4 = Math.PI/4;
    public static final double M_1_PI = 1/Math.PI;
    public static final double M_2_PI = 2/Math.PI;
    public static final double M_PI_X_2 = M_PI * 2.0f;

    public enum TweenType {
        CUSTOM_EASING,
        Linear,

        Sine_EaseIn,
        Sine_EaseOut,
        Sine_EaseInOut,


        Quad_EaseIn,
        Quad_EaseOut,
        Quad_EaseInOut,

        Cubic_EaseIn,
        Cubic_EaseOut,
        Cubic_EaseInOut,

        Quart_EaseIn,
        Quart_EaseOut,
        Quart_EaseInOut,

        Quint_EaseIn,
        Quint_EaseOut,
        Quint_EaseInOut,

        Expo_EaseIn,
        Expo_EaseOut,
        Expo_EaseInOut,

        Circ_EaseIn,
        Circ_EaseOut,
        Circ_EaseInOut,

        Elastic_EaseIn,
        Elastic_EaseOut,
        Elastic_EaseInOut,

        Back_EaseIn,
        Back_EaseOut,
        Back_EaseInOut,

        Bounce_EaseIn,
        Bounce_EaseOut,
        Bounce_EaseInOut,
    }
    public static float tweenTo(float time, TweenType type, float[] easingParam) {
        float delta = 0;

        switch (type) {
            case CUSTOM_EASING:
                delta = customEase(time, easingParam);
                break;

            case Linear:
                delta = linear(time);
                break;

            case Sine_EaseIn:
                delta = sineEaseIn(time);
                break;
            case Sine_EaseOut:
                delta = sineEaseOut(time);
                break;
            case Sine_EaseInOut:
                delta = sineEaseInOut(time);
                break;

            case Quad_EaseIn:
                delta = quadEaseIn(time);
                break;
            case Quad_EaseOut:
                delta = quadEaseOut(time);
                break;
            case Quad_EaseInOut:
                delta = quadEaseInOut(time);
                break;

            case Cubic_EaseIn:
                delta = cubicEaseIn(time);
                break;
            case Cubic_EaseOut:
                delta = cubicEaseOut(time);
                break;
            case Cubic_EaseInOut:
                delta = cubicEaseInOut(time);
                break;

            case Quart_EaseIn:
                delta = quartEaseIn(time);
                break;
            case Quart_EaseOut:
                delta = quartEaseOut(time);
                break;
            case Quart_EaseInOut:
                delta = quartEaseInOut(time);
                break;

            case Quint_EaseIn:
                delta = quintEaseIn(time);
                break;
            case Quint_EaseOut:
                delta = quintEaseOut(time);
                break;
            case Quint_EaseInOut:
                delta = quintEaseInOut(time);
                break;

            case Expo_EaseIn:
                delta = expoEaseIn(time);
                break;
            case Expo_EaseOut:
                delta = expoEaseOut(time);
                break;
            case Expo_EaseInOut:
                delta = expoEaseInOut(time);
                break;

            case Circ_EaseIn:
                delta = circEaseIn(time);
                break;
            case Circ_EaseOut:
                delta = circEaseOut(time);
                break;
            case Circ_EaseInOut:
                delta = circEaseInOut(time);
                break;

            case Elastic_EaseIn:
            {
                float period = 0.3f;
                if (null != easingParam) {
                    period = easingParam[0];
                }
                delta = elasticEaseIn(time, period);
            }
            break;
            case Elastic_EaseOut:
            {
                float period = 0.3f;
                if (null != easingParam) {
                    period = easingParam[0];
                }
                delta = elasticEaseOut(time, period);
            }
            break;
            case Elastic_EaseInOut:
            {
                float period = 0.3f;
                if (null != easingParam) {
                    period = easingParam[0];
                }
                delta = elasticEaseInOut(time, period);
            }
            break;


            case Back_EaseIn:
                delta = backEaseIn(time);
                break;
            case Back_EaseOut:
                delta = backEaseOut(time);
                break;
            case Back_EaseInOut:
                delta = backEaseInOut(time);
                break;

            case Bounce_EaseIn:
                delta = bounceEaseIn(time);
                break;
            case Bounce_EaseOut:
                delta = bounceEaseOut(time);
                break;
            case Bounce_EaseInOut:
                delta = bounceEaseInOut(time);
                break;

            default:
                delta = sineEaseInOut(time);
                break;
        }

        return delta;
    }

    public static float linear(float time) {
        return time;
    }

    public static float sineEaseIn(float time)
    {
        return -1 * (float)Math.cos(time * (float)M_PI_2) + 1;
    }

    public static float sineEaseOut(float time)
    {
        return (float)Math.sin(time * (float)M_PI_2);
    }

    public static float sineEaseInOut(float time)
    {
        return -0.5f * ((float)Math.cos((float)M_PI * time) - 1);
    }

    public static float quadEaseIn(float time)
    {
        return time * time;
    }

    public static float quadEaseOut(float time)
    {
        return -1 * time * (time - 2);
    }

    public static float quadEaseInOut(float time)
    {
        time = time*2;
        if (time < 1)
            return 0.5f * time * time;
        --time;
        return -0.5f * (time * (time - 2) - 1);
    }

    public static float cubicEaseIn(float time)
    {
        return time * time * time;
    }

    public static float cubicEaseOut(float time)
    {
        time -= 1;
        return (time * time * time + 1);
    }

    public static float cubicEaseInOut(float time)
    {
        time = time*2;
        if (time < 1)
            return 0.5f * time * time * time;
        time -= 2;
        return 0.5f * (time * time * time + 2);
    }

    public static float quartEaseIn(float time)
    {
        return time * time * time * time;
    }

    public static float quartEaseOut(float time)
    {
        time -= 1;
        return -(time * time * time * time - 1);
    }

    public static float quartEaseInOut(float time)
    {
        time = time*2;
        if (time < 1)
            return 0.5f * time * time * time * time;
        time -= 2;
        return -0.5f * (time * time * time * time - 2);
    }

    public static float quintEaseIn(float time)
    {
        return time * time * time * time * time;
    }

    public static float quintEaseOut(float time)
    {
        time -=1;
        return (time * time * time * time * time + 1);
    }

    public static float quintEaseInOut(float time)
    {
        time = time*2;
        if (time < 1)
            return 0.5f * time * time * time * time * time;
        time -= 2;
        return 0.5f * (time * time * time * time * time + 2);
    }

    public static float expoEaseIn(float time)
    {
        return time == 0 ? 0 : (float)Math.pow(2, 10 * (time/1 - 1)) - 1 * 0.001f;
    }

    public static float expoEaseOut(float time)
    {
        return time == 1 ? 1 : (-(float)Math.pow(2, -10 * time / 1) + 1);
    }

    public static float expoEaseInOut(float time)
    {
        if(time == 0 || time == 1)
            return time;

        if (time < 0.5f)
            return 0.5f * (float)Math.pow(2, 10 * (time * 2 - 1));

        return 0.5f * (-(float)Math.pow(2, -10 * (time * 2 - 1)) + 2);
    }

    public static float circEaseIn(float time)
    {
        return -1 * ((float)Math.sqrt(1 - time * time) - 1);
    }

    public static float circEaseOut(float time)
    {
        time = time - 1;
        return (float)Math.sqrt(1 - time * time);
    }

    public static float circEaseInOut(float time)
    {
        time = time * 2;
        if (time < 1)
            return -0.5f * ((float)Math.sqrt(1 - time * time) - 1);
        time -= 2;
        return 0.5f * ((float)Math.sqrt(1 - time * time) + 1);
    }

    public static float elasticEaseIn(float time, float period)
    {

        float newT = 0;
        if (time == 0 || time == 1)
        {
            newT = time;
        }
        else
        {
            float s = period / 4;
            time = time - 1;
            newT = -(float)Math.pow(2, 10 * time) * (float)Math.sin((time - s) * (float)M_PI_X_2 / period);
        }

        return newT;
    }

    public static float elasticEaseOut(float time, float period)
    {

        float newT = 0;
        if (time == 0 || time == 1)
        {
            newT = time;
        }
        else
        {
            float s = period / 4;
            newT = (float)Math.pow(2, -10 * time) * (float)Math.sin((time - s) * M_PI_X_2 / period) + 1;
        }

        return newT;
    }

    public static float elasticEaseInOut(float time, float period)
    {

        float newT = 0;
        if (time == 0 || time == 1)
        {
            newT = time;
        }
        else
        {
            time = time * 2;
            if (period==0)
            {
                period = 0.3f * 1.5f;
            }

            float s = period / 4;

            time = time - 1;
            if (time < 0)
            {
                newT = -0.5f * (float)Math.pow(2, 10 * time) * (float)Math.sin((time -s) * M_PI_X_2 / period);
            }
            else
            {
                newT = (float)Math.pow(2, -10 * time) * (float)Math.sin((time - s) * M_PI_X_2 / period) * 0.5f + 1;
            }
        }
        return newT;
    }

    public static float backEaseIn(float time)
    {
        float overshoot = 1.70158f;
        return time * time * ((overshoot + 1) * time - overshoot);
    }

    public static float backEaseOut(float time)
    {
        float overshoot = 1.70158f;

        time = time - 1;
        return time * time * ((overshoot + 1) * time + overshoot) + 1;
    }

    public static float backEaseInOut(float time)
    {
        float overshoot = 1.70158f * 1.525f;

        time = time * 2;
        if (time < 1)
        {
            return (time * time * ((overshoot + 1) * time - overshoot)) / 2;
        }
        else
        {
            time = time - 2;
            return (time * time * ((overshoot + 1) * time + overshoot)) / 2 + 1;
        }
    }

    public static float bounceTime(float time)
    {
        if (time < 1 / 2.75f)
        {
            return 7.5625f * time * time;
        }
        else if (time < 2 / 2.75f)
        {
            time -= 1.5f / 2.75f;
            return 7.5625f * time * time + 0.75f;
        }
        else if(time < 2.5f / 2.75f)
        {
            time -= 2.25f / 2.75f;
            return 7.5625f * time * time + 0.9375f;
        }

        time -= 2.625f / 2.75f;
        return 7.5625f * time * time + 0.984375f;
    }

    public static float bounceEaseIn(float time)
    {
        return 1 - bounceTime(1 - time);
    }

    public static float bounceEaseOut(float time)
    {
        return bounceTime(time);
    }

    public static float bounceEaseInOut(float time)
    {
        float newT = 0;
        if (time < 0.5f)
        {
            time = time * 2;
            newT = (1 - bounceTime(1 - time)) * 0.5f;
        }
        else
        {
            newT = bounceTime(time * 2 - 1) * 0.5f + 0.5f;
        }

        return newT;
    }

    public static float customEase(float time, float [] easingParam)
    {
        if (easingParam!=null && easingParam.length>7)
        {
            float tt = 1-time;
            return easingParam[1]*tt*tt*tt + 3*easingParam[3]*time*tt*tt + 3*easingParam[5]*time*time*tt + easingParam[7]*time*time*time;
        }
        return time;
    }

    public static float easeIn(float time, float rate)
    {
        return (float)Math.pow(time, rate);
    }

    public static float easeOut(float time, float rate)
    {
        return (float)Math.pow(time, 1 / rate);
    }

    public static float easeInOut(float time, float rate)
    {
        time *= 2;
        if (time < 1)
        {
            return 0.5f * (float)Math.pow(time, rate);
        }
        else
        {
            return (1.0f - 0.5f * (float)Math.pow(2 - time, rate));
        }
    }

    public static float quadraticIn(float time)
    {
        return (float)Math.pow(time,2);
    }

    public static float quadraticOut(float time)
    {
        return -time*(time-2);
    }

    public static float quadraticInOut(float time)
    {

        float resultTime = time;
        time = time*2;
        if (time < 1)
        {
            resultTime = time * time * 0.5f;
        }
        else
        {
            --time;
            resultTime = -0.5f * (time * (time - 2) - 1);
        }
        return resultTime;
    }

    public static float bezieratFunction( float a, float b, float c, float d, float t )
    {
        return ((float)Math.pow(1-t,3) * a + 3*t*((float)Math.pow(1-t,2))*b + 3*(float)Math.pow(t,2)*(1-t)*c + (float)Math.pow(t,3)*d );
    }
}
