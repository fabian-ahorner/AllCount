package com.bitflake.counter.algo.shared.current.tools;


import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.*;

/**
 * Created by fahor on 30/07/2016.
 */
public class VecHelper {
    public static RealMatrix getRotMatrix(double[] vec1, double[] vec2) {
        Vector3D a = new Vector3D(vec1);
        Vector3D b = new Vector3D(vec2);
        a = a.normalize();
        b = b.normalize();
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
//        return i;
    }

    public static RealVector rotate(RealVector v, RealMatrix m) {
        double norm = v.getNorm();
        return m.preMultiply(v.unitVector()).mapMultiplyToSelf(norm);
    }

    public static Vector3D[] createFlippedVersions(Vector3D values) {
        Rotation r1 = new Rotation(Vector3D.MINUS_I, Math.PI, RotationConvention.FRAME_TRANSFORM);
        Rotation r2 = new Rotation(Vector3D.MINUS_J, Math.PI, RotationConvention.FRAME_TRANSFORM);
        return new Vector3D[]{
                values,
                r1.applyTo(values),
                r2.applyTo(values),
                r1.applyTo(r2.applyTo(values)),
        };
    }
}
