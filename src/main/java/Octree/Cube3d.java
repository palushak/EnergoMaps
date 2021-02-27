package Octree;

import ConvexHull.Point3d;

import java.util.ArrayList;
import java.util.List;

public class Cube3d {
    public double minX, minY, minZ, maxX, maxY, maxZ;
    private List<Point3d> point3dsList = new ArrayList<Point3d>();


    public Cube3d(double _minX, double _maxX, double _minY, double _maxY, double _minZ, double _maxZ) {
        this.minX = _minX;
        this.maxX = _maxX;
        this.minY = _minY;
        this.maxY = _maxY;
        this.minZ = _minZ;
        this.maxZ = _maxZ;
    }

    public boolean ifInCube(double x, double y, double z) {
        return (x <= maxX && x >= minX) && (y <= maxY && y >= minY) && (z <= maxZ && z >= minZ);
    }

    public void addPoint(Point3d point) {
        point3dsList.add(point);
    }

    public void removePoint(int i) {
        point3dsList.remove(i);
    }

    public Point3d getPoint(int i) {
        return  point3dsList.get(i);
    }

    public int getPointsSize(){
        return point3dsList.size();
    }

    public List<Point3d> getPoint3dsList() {
        return point3dsList;
    }

    public void setPoint3dsList(List<Point3d> point3dsList) {
        this.point3dsList = point3dsList;
    }
}
