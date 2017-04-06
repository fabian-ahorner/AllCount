package com.bitflake.counter.algo.shared.current.count;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Created by fahor on 21/09/2016.
 */
public class CounterRotation {
    private final Vector3D firstState;
    private final Vector3D secondState;
    private final Vector3D startValues;
    private final double originalAngle;
    private Rotation rotation;

    private double firstStateRotation;
    private double secondStateRotation;
    private Vector3D referenceStartState;

    public CounterRotation(Vector3D[] states, Vector3D startValues) {
        this.firstState = states[0].normalize();
        this.secondState = states[1].normalize();
        this.originalAngle = Math.abs(Math.acos(firstState.dotProduct(secondState)));
        this.startValues = startValues;
        rotateFirstState(startValues);
    }

    private void rotateFirstState(Vector3D startValues) {
        Vector3D start = startValues.normalize();
        this.referenceStartState = start;
        if (start.equals(firstState)) {
            rotation = Rotation.IDENTITY;
            firstStateRotation = 0;
        } else {
            Vector3D rotationAxis = firstState.crossProduct(start).normalize();
            firstStateRotation = -Math.acos(firstState.dotProduct(start));
            // Create rotation to move v1 to START_AXIS
            rotation = new Rotation(rotationAxis, firstStateRotation, RotationConvention.VECTOR_OPERATOR);
        }
    }

    public void rotateSecondState(Vector3D stateValues) {
        Vector3D second = stateValues.normalize();
        second = rotation.applyTo(second);

        Vector3D av1 = firstState.crossProduct(second).normalize();
        Vector3D av2 = firstState.crossProduct(secondState).normalize();
        secondStateRotation = Math.acos(av1.dotProduct(av2));

        Rotation rotation = new Rotation(firstState, secondStateRotation, RotationConvention.VECTOR_OPERATOR);
        this.rotation = rotation.applyTo(this.rotation);
    }

    public Vector3D rotate(Vector3D values) {
        return rotation.applyTo(values);
    }

    public Rotation getRotation() {
        return rotation;
    }

    public double getFirstStateDistance(Vector3D v) {
        return startValues.distance(v);
    }

    public double getCurrentAngle(Vector3D v) {
        return Math.abs(Math.acos(startValues.dotProduct(v)));
    }

    public double getOriginalAngle() {
        return originalAngle;
    }

    public double getSecondRotation() {
        return secondStateRotation;
    }

    public double getFirstRotation() {
        return firstStateRotation;
    }
}
