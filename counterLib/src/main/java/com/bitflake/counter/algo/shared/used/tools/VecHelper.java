package com.bitflake.counter.algo.shared.used.tools;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.*;

/**
 * Created by fahor on 30/07/2016.
 */
public class VecHelper {
    public static RealMatrix getRotMatrix(double[] vec1, double[] vec2) {
        Vector3D a = new Vector3D(vec1).normalize();
        Vector3D b = new Vector3D(vec2).normalize();
        Vector3D v = a.crossProduct(b);
        double s = v.getNorm();
        double c = a.dotProduct(b);
        RealMatrix i = MatrixUtils.createRealIdentityMatrix(3);
        if (s == 0)
            return i;
        RealMatrix vx = new BlockRealMatrix(
                new double[][]{
                        {0, v.getZ(), -v.getY()},
                        {-v.getZ(), 0, v.getX()},
                        {v.getY(), -v.getX(), 0}});
        RealMatrix r = i.add(vx).add(vx.power(2).scalarMultiply((1 - c) / (s * s)));
        return r;
    }

    public static RealVector rotate(RealVector v, RealMatrix m) {
        double norm = v.getNorm();
        return m.preMultiply(v.unitVector()).mapMultiplyToSelf(norm);
    }
}
