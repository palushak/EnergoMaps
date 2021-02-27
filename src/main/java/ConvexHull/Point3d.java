package ConvexHull;

import Octree.AABB;
import Octree.Volume;

public class Point3d extends Vector3d{

    public Point3d() {
    }

    public Point3d(double x, double y, double z, Integer r, Integer g, Integer b) {
        set(x, y, z, r, g ,b);
    }

}
