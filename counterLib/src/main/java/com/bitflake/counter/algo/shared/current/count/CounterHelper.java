package com.bitflake.counter.algo.shared.current.count;

import com.bitflake.counter.algo.shared.current.CountState;

import com.bitflake.counter.algo.shared.current.tools.VecHelper;
import org.apache.commons.math3.linear.*;

import java.util.List;

/**
 * Created by fahor on 02/09/2016.
 */
public class CounterHelper {
//    private static final RealMatrix ROTATION_MATRIX1 = new BlockRealMatrix(
//            new double[][]{
//                    {-1, 0, 0},
//                    {0, -1, 0},
//                    {0, 0, 1}});
//    private static final RealMatrix ROTATION_MATRIX2 = new BlockRealMatrix(
//            new double[][]{
//                    {1, 0, 0},
//                    {0, -1, 0},
//                    {0, 0, -1}});

    private static final VersionBuilder versionBuilder = new VersionBuilder();


    public static CounterVersion[] createCounterVersions(List<CountState> states) {
        return versionBuilder.getCounterVersions(states);

//        CounterVersion[] versions = new CounterVersion[4];
//
//        for (int i = 0; i < versions.length; i++) {
//            versions[i] = new CounterVersion(new RealVector[states.size()]);
//        }
//        for (int i = 0; i < states.size(); i++) {
//            versions[0].states[i] = new ArrayRealVector(states.get(i).values);
//            versions[1].states[i] = ROTATION_MATRIX1.preMultiply(versions[0].states[i]);
//            versions[2].states[i] = ROTATION_MATRIX2.preMultiply(versions[0].states[i]);
//            versions[3].states[i] = ROTATION_MATRIX1.preMultiply(ROTATION_MATRIX2.preMultiply(versions[0].states[i]));
//        }
//        return versions;
    }

    private static class VersionBuilder {
        private final RealMatrix I = MatrixUtils.createRealIdentityMatrix(3);
        private RealMatrix[][] manipulations;
        private RealMatrix[] matrixPowerSet;
        private int matI = 0;

        public VersionBuilder() {
            buildManipulations();
            buildPowerSet();
        }


        private void buildManipulations() {
            manipulations = new RealMatrix[0][];
//            manipulations[0] = new RealMatrix[]{
//                    I,
////                    getRotationMatrix(Axis.X, 180)
//            };
//            manipulations[1] = new RealMatrix[]{
//                    I,
//                    getRotationMatrix(Axis.Y, 180),
//            };
//            for (int i = 0; i < 3; i++) {
//                Axis a = Axis.values()[i];
//                manipulations[0 + i] = new RealMatrix[]{
//                        getRotationMatrix(a, -30),
//                        I,
//                        getRotationMatrix(a, 30),
//                };
//            }
        }

        public void buildPowerSet() {
            int matrixPowerSetCnt = 1;
            for (int i = 0; i < manipulations.length; i++) {
                matrixPowerSetCnt *= manipulations[i].length;
            }
            matrixPowerSet = new RealMatrix[matrixPowerSetCnt];
            fillPowerSet(0, I);
        }

        private void fillPowerSet(int i, RealMatrix matrix) {
            if (i < manipulations.length) {
                for (int j = 0; j < manipulations[i].length; j++) {
                    fillPowerSet(i + 1, matrix.multiply(manipulations[i][j]));
                }
            } else {
                matrixPowerSet[matI++] = matrix;
            }
        }

        public CounterVersion[] getCounterVersions(List<CountState> states) {

            CounterVersion[] versions = new CounterVersion[matrixPowerSet.length];

            for (int i = 0; i < versions.length; i++) {
                versions[i] = new CounterVersion(new RealVector[states.size()]);
            }
            for (int i = 0; i < states.size(); i++) {
                ArrayRealVector vec = new ArrayRealVector(states.get(i).values);
                for (int j = 0; j < matrixPowerSet.length; j++) {
                    versions[j].states[i] = matrixPowerSet[j].preMultiply(vec);
                }
            }
            return versions;
        }
    }

    private enum Axis {
        X, Y, Z
    }

    private static RealMatrix getRotationMatrix(Axis axis, double deg) {
        deg = deg / 180 * Math.PI;
        double sin = Math.sin(deg);
        double cos = Math.cos(deg);
        switch (axis) {
            // @formatter:off
            case X:
                return new BlockRealMatrix(
                        new double[][]{
                                {1,     0,     0},
                                {0,     cos,   -sin},
                                {0,     sin,   -cos}});
            case Y:
                return new BlockRealMatrix(
                        new double[][]{
                                {cos,   0,      sin},
                                {0,     1,      0},
                                {-sin,  0,      cos}});
            case Z:
                return new BlockRealMatrix(
                        new double[][]{
                                {cos,   -sin,   0},
                                {sin,   cos,    0},
                                {0,     0,      1}});
            // @formatter:on
        }
        return null;
    }

    public static CounterVersion getCurrentVersion(List<CountState> states, ArrayRealVector currentValues) {
        RealMatrix rotationMatrix = VecHelper.getRotMatrix(states.get(0).values, currentValues.getDataRef());
        RealVector[] stateValues = new RealVector[states.size()];
        for (int i = 0; i < stateValues.length; i++) {
            CountState s = states.get(i);
            RealVector vec = new ArrayRealVector(s.values);
            stateValues[i] = rotationMatrix.preMultiply(vec);
        }
        return new CounterVersion(stateValues);
    }
    public static CounterVersion getDefaultVersion(List<CountState> states) {
        RealVector[] stateValues = new RealVector[states.size()];
        for (int i = 0; i < stateValues.length; i++) {
            CountState s = states.get(i);
            stateValues[i] = new ArrayRealVector(s.values);
        }
        return new CounterVersion(stateValues);
    }
}
