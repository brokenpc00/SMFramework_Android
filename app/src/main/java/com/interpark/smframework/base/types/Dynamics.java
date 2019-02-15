package com.interpark.smframework.base.types;

public class Dynamics {
    public Dynamics() {
        reset();
    }

    private final float MAX_TIMESTEP = 1.0f / 60.0f;

    private float _position = 0.0f;

    private float _velocity = 0.0f;

    private float _maxPosition = Float.MAX_VALUE;

    private float _minPosition = Float.MIN_VALUE;

    private float _lastTime = 0.0f;

    private float _friction = 0.0f;

    private float _stiffness = 0.0f;

    private float _damping = 0.0f;

    public void reset() {
        _position = 0;
        _velocity = 0;
        _maxPosition = Float.MAX_VALUE;
        _minPosition = Float.MIN_VALUE;
        _lastTime = 0;
        _friction = 0;
        _stiffness = 0;
        _damping = 0;
    }

    public void setState(float position, float velocity, float nowTime) {
        _velocity = velocity;
        _position = position;
        _lastTime = nowTime;
    }

    public float getPosition() {
        return _position;
    }

    public boolean isAtRest(final float velocityTolerance, final float positionTolerance) {
        return isAtRest(velocityTolerance, positionTolerance, 1.0f);
    }

    public boolean isAtRest(final float velocityTolerance, final float positionTolerance, final float range) {
        final boolean standingStill = Math.abs(_velocity) < velocityTolerance;

        boolean withinLimits;
        if (range==1) {
            withinLimits = ((_position - positionTolerance < _maxPosition) && (_position + positionTolerance > _minPosition));
        } else {
            withinLimits = ((_position*range - positionTolerance < _maxPosition*range) && (_position*range + positionTolerance > _minPosition*range));
        }

        return (standingStill && withinLimits);
    }

    public void setMaxPosition(final float maxPosition) {
        _maxPosition = maxPosition;
    }

    public void setMinPosition(final float minPosition) {
        _minPosition = minPosition;
    }

    public void update(float now) {
        float dt = now - _lastTime;
        if (dt > MAX_TIMESTEP) {
            dt = MAX_TIMESTEP;
        }

        // Calculate current acceleration
        float acceleration = calculateAcceleration();

        // Calculate next position based on current velocity and acceleration
        _position += _velocity * dt + .5f * acceleration * dt * dt;

        // Update velocity
        _velocity += acceleration * dt;

        _lastTime = now;
    }

    public float getDistanceToLimit() {
        float distanceToLimit = 0;

        if (_position > _maxPosition) {
            distanceToLimit = _maxPosition - _position;
        } else if (_position < _minPosition) {
            distanceToLimit = _minPosition - _position;
        }

        return distanceToLimit;
    }

    public void setFriction(final float friction)
    {
        _friction = friction;
    }

    public void setSpring(final float stiffness, final float dampingRatio)
    {
        _stiffness = stiffness;
        _damping = dampingRatio * 2 * (float)Math.sqrt(stiffness);
    }

    public float calculateAcceleration()
    {
        float acceleration;
        float distanceFromLimit = getDistanceToLimit();

        if (distanceFromLimit != 0.0f) {
            acceleration = distanceFromLimit * _stiffness - _damping * _velocity;
        } else {
            acceleration = -_friction * _velocity;
        }

        return acceleration;
    }
}
