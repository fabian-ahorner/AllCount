package com.bitflake.counter.algo.shared.current.count;

import com.bitflake.counter.algo.shared.current.CountState;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.List;

public class DirCounter {
    private static final Vector3D START_AXIS = new Vector3D(0, 1, 0);
    private final Vector3D[] states;
    private Rotation rotation;

    public DirCounter(List<CountState> states) {
        this(CountState.toArray(states));
    }

    public DirCounter(double[][] states) {
        // Normalise all rotations by moving the first vector to a defined axis
        // and rotating the second vector to the x-axis
        Vector3D v1 = new Vector3D(states[0]).normalize();
        Vector3D v2 = new Vector3D(states[1]).normalize();
        Rotation startRotation;
        double rotationAngle;
        if (v1.equals(START_AXIS)) {
            startRotation = Rotation.IDENTITY;
        } else {
            Vector3D rotationAxis = START_AXIS.crossProduct(v1);
            rotationAngle = -Math.acos(START_AXIS.dotProduct(v1));
            // Create rotation to move v1 to START_AXIS
            startRotation = new Rotation(rotationAxis, rotationAngle, RotationConvention.VECTOR_OPERATOR);
        }

        v2 = startRotation.applyTo(v2);
        rotationAngle = Math.atan2(v2.getZ(), v2.getX());

        // Create rotation to rotate the second vector to point in x direction
        Rotation relativeRotation = new Rotation(START_AXIS, rotationAngle, RotationConvention.VECTOR_OPERATOR);
        Rotation finalRotation = relativeRotation.applyTo(startRotation);

        this.states =
                rotate(states, finalRotation);
    }

    public Vector3D[] getStates() {
        return states;
    }

    private static Vector3D[] rotate(double[][] states, Rotation rotation) {
        Vector3D[] newStates = new Vector3D[states.length];
        for (int i = 0; i < states.length; i++) {
            newStates[i] = rotation.applyTo(new Vector3D(states[i]));
        }
        return newStates;
    }

    public void setSecoundState(double[] stateValues) {
        Vector3D second = new Vector3D(stateValues).normalize();
        second = rotation.applyTo(second);

        Vector3D av1 = states[0].crossProduct(second);
        Vector3D av2 = states[0].crossProduct(states[1]);
        double angle = -Math.acos(av1.dotProduct(av2));

        Rotation rotation = new Rotation(states[0], angle, RotationConvention.VECTOR_OPERATOR);
        this.rotation = rotation.applyTo(this.rotation);
    }

    public void getDistance(){

    }
}
