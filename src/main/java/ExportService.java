import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import ConvexHull.Point3d;
import ConvexHull.ConvexHull3D;
import Octree.AABB;
import Octree.Cube3d;
import Octree.Octree;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import javax.swing.filechooser.FileSystemView;

public class ExportService extends Service<Void> {

    private Boolean currentState;
    private ObjectProperty<String> currentWork = new SimpleObjectProperty<>();
    private List<File> files;
    private long pointsCount;
    private long pointsExportCount;
    private Long pointsPerFile;
    private Long filesCount;
    private List<Point3d> points3dList;
    private List<Cube3d> cube3dList;
    //    private List<Cube3d> cube3dListLocalTemp;
//    private List<Cube3d> cube3dListLocal;
    private Point3d[] vertices;
    private Octree octree;
    private int counter = 0;
    private long max = 0;
    private int heightLevel = 0;
    private int splitAAbbs = 1;
    private String mainFilesPath;

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(
                        () -> {
                            setCurrentWork("Start");
                        }
                );

                points3dList = new ArrayList<Point3d>();
                counter = 0;
                max = 0;
                currentState = true;

                heightLevel = 0;
                splitAAbbs = 1;

                while (splitAAbbs < filesCount) {
                    heightLevel++;
                    splitAAbbs = splitAAbbs * 8;

                }

                splitAAbbs = splitAAbbs * 8;

                max = pointsCount + pointsCount + pointsExportCount;

                try {

                    for (File file : files) {

                        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                        String row;

                        Platform.runLater(
                                () -> {
                                    setCurrentWork("1 z 4: Dekodowanie plikow...");
                                }
                        );

                        while ((row = bufferedReader.readLine()) != null) {
                            if (!currentState) return null;

                            String[] pointArray = new String[6];
                            pointArray = row.split(" ");

                            if (pointArray.length == 6) {

                                Point3d point3d = new Point3d();
                                point3d.x = Double.parseDouble(pointArray[0]);
                                point3d.y = Double.parseDouble(pointArray[1]);
                                point3d.z = Double.parseDouble(pointArray[2]);
                                point3d.r = Integer.parseInt(pointArray[3]);
                                point3d.g = Integer.parseInt(pointArray[4]);
                                point3d.b = Integer.parseInt(pointArray[5]);
                                points3dList.add(point3d);


                            } else if (pointArray.length == 8) {

                                Point3d point3d = new Point3d();
                                point3d.x = Double.parseDouble(pointArray[2]);
                                point3d.y = Double.parseDouble(pointArray[3]);
                                point3d.z = Double.parseDouble(pointArray[4]);
                                point3d.r = Integer.parseInt(pointArray[5]);
                                point3d.g = Integer.parseInt(pointArray[6]);
                                point3d.b = Integer.parseInt(pointArray[7]);
                                points3dList.add(point3d);

                            }

                            counter++;
                            updateProgress(counter, max);
                        }

                    }
                    System.out.println("Prawidlowych pkt: " + points3dList.size());

                } catch (Exception e) {
                    e.printStackTrace();
                }


                ConvexHull3D hull = new ConvexHull3D();
                String myDocuments;
                File dirMain;
                String fName;
                File dirMain2;

                try {
                    Platform.runLater(
                            () -> {
                                setCurrentWork("2 z 4: generowanie convex hull...");
                            }
                    );

                    Point3d[] digitPointArray = new Point3d[points3dList.size()];
                    digitPointArray = points3dList.toArray(digitPointArray);
                    hull.build(digitPointArray);

                    vertices = hull.getVertices();
                    if (!currentState) return null;

                    myDocuments = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();

                    dirMain = new File(myDocuments + "\\" + "XYZ-to-PCD-pliki");

                    if (!dirMain.exists()) {
                        dirMain.mkdir();
                    }

                    fName = files.get(0).getName().replaceFirst("[.][^.]+$", "");


                    dirMain2 = new File(dirMain + "\\" + fName);

                    if (!dirMain2.exists()) {
                        dirMain2.mkdir();
                    }

                    mainFilesPath = dirMain2.toString() + "\\" + fName;

                    String filename = mainFilesPath + "_1.pcd";

                    FileWriter fw = new FileWriter(filename);
                    BufferedWriter bw = new BufferedWriter(fw);


                    try {
                        bw.write("VERSION .7");
                        bw.newLine();
                        bw.write("FIELDS x y z rgb");
                        bw.newLine();
                        bw.write("SIZE 4 4 4 4");
                        bw.newLine();
                        bw.write("TYPE F F F F");
                        bw.newLine();
                        bw.write("COUNT 1 1 1 1");
                        bw.newLine();
                        bw.write("WIDTH " + vertices.length);
                        bw.newLine();
                        bw.write("HEIGHT 1");
                        bw.newLine();
                        bw.write("VIEWPOINT 0 0 0 1 0 0 0");
                        bw.newLine();
                        bw.write("POINTS " + vertices.length);
                        bw.newLine();
                        bw.write("DATA ascii");
                        bw.newLine();
                        for (Point3d point : vertices) {

                            int r = point.r;
                            int g = point.g;
                            int b = point.b;

                            String rgbColor = toRgb(r,g,b);

                            String pointLine = point.x + " " + point.y + " " + point.z + " " +  rgbColor;

                            bw.write(pointLine);
                            bw.newLine();

                            counter++;
                            updateProgress(counter, max);

                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        bw.close();
                        fw.close();
                    }

                    System.out.println("Vertices: " + vertices.length);

                } catch (Exception e) {
                    e.printStackTrace();
                }


                try {
                    Platform.runLater(
                            () -> {
                                setCurrentWork("3 z 4: generowanie octree...");
                            }
                    );

                    // 1. budujemy listę do zapisu
                    // 2. uzupełniamy nagłówek POINTS
                    // 3/ budujemy plik
                    // counter++ jak zdejmujemy pointy z głownej listy - az do 0

                    //tyle najmniejszych sześcianów w octree ile plików
                    //potem dzielimy punkty równo między pliki

                    System.out.println("x: " + hull.getMinX() + "," + hull.getMaxX());
                    System.out.println("y: " + hull.getMinY() + "," + hull.getMaxY());
                    System.out.println("z: " + hull.getMinZ() + "," + hull.getMaxZ());

                    double minX = hull.getMinX();
                    double maxX = hull.getMaxX();
                    double minY = hull.getMinY();
                    double maxY = hull.getMaxY();
                    double minZ = hull.getMinZ();
                    double maxZ = hull.getMaxZ();

                    AABB aaBB = new AABB(minX, maxX, minY, maxY, minZ, maxZ);

                    System.out.println("AABBs splitLvl : " + heightLevel + ", splitAAbbs: " + splitAAbbs);

                    //TODO octree global

                    cube3dList = new ArrayList<Cube3d>();

                    octree = buildOctree(aaBB, heightLevel);

                    System.out.println("cube3dList list : " + cube3dList.size());

                    for (Point3d point : points3dList) {

                        for (Cube3d cube3d : cube3dList) {
                            if (!currentState) return null;
                            if (cube3d.ifInCube(point.x, point.y, point.z)) {
                                cube3d.addPoint(point);
                            }
                        }

                        counter++;
                        updateProgress(counter, max);

                    }

                    for (int i = 0; i < cube3dList.size(); i++) {
                        if (cube3dList.get(i).getPointsSize() == 0) {
                            cube3dList.remove(i);
                        }
                    }

                    System.out.println("cube3dList list : " + cube3dList.size());


                } catch (Exception e) {
                    e.printStackTrace();
                }


//                try {
//                    Platform.runLater(
//                            () -> {
//                                setCurrentWork("4 z 6: generowanie lokalnych octree...");
//                            }
//                    );
//
//                    cube3dListLocal = new ArrayList<Cube3d>();
//
//                    for (Cube3d cube3d : cube3dList) {
//
//                        AABB aaBBlocal = new AABB(cube3d.minX, cube3d.maxX, cube3d.minY, cube3d.maxY, cube3d.minZ, cube3d.maxZ);
//                        cube3dListLocalTemp = new ArrayList<Cube3d>();
//                        Octree octreeLocal = buildOctreeLocal(aaBBlocal, heightLevel);
//
////                        List<Point3d> points3dListTemp = cube3d.getPoint3dsList();
////                        for (Point3d point : points3dListTemp) {
////
////                            for (Cube3d cube3dtemp : cube3dListLocalTemp){
////                                if (cube3dtemp.ifInCube(point.x, point.y, point.z)){
////                                    cube3dtemp.addPoint(point);
////                                }
////                            }
////
////                            counter++;
////                            updateProgress(counter, max);
////
////                        }
//
////                        for (int i=0;i<cube3dListLocalTemp.size();i++)
////                        {
////                            if (cube3dListLocalTemp.get(i).getPointsSize()>0){
////                                cube3dListLocal.add(cube3dListLocalTemp.get(i));
////                            }
////                        }
//                    }
//                } catch (Throwable e) {
//                    e.printStackTrace();
//                }


                try {
                    Platform.runLater(
                            () -> {
                                setCurrentWork("4 z 4: rozklad chmury do .pcd...");
                            }
                    );


                    for (int i = 0; i < filesCount; i++) {
                        if (!currentState) return null;

                        Integer nameNumber = i + 2;
                        String filename = mainFilesPath + "_" + nameNumber.toString() + ".pcd";
                        FileWriter fw = new FileWriter(filename);
                        BufferedWriter bw = new BufferedWriter(fw);


                        try {
                            bw.write("VERSION .7");
                            bw.newLine();
                            bw.write("FIELDS x y z rgb");
                            bw.newLine();
                            bw.write("SIZE 4 4 4 4");
                            bw.newLine();
                            bw.write("TYPE F F F F");
                            bw.newLine();
                            bw.write("COUNT 1 1 1 1");
                            bw.newLine();
                            bw.write("WIDTH " + pointsPerFile.toString());
                            bw.newLine();
                            bw.write("HEIGHT 1");
                            bw.newLine();
                            bw.write("VIEWPOINT 0 0 0 1 0 0 0");
                            bw.newLine();
                            bw.write("POINTS " + pointsPerFile.toString());
                            bw.newLine();
                            bw.write("DATA ascii");
                            bw.newLine();

                            boolean saving = true;
                            int l = 1;
                            while (saving) {
                                if (!currentState) return null;
                                Iterator<Cube3d> iter = cube3dList.iterator();

                                while(iter.hasNext()) {
                                    Cube3d cube3d = iter.next();

                                    Integer cubeSize = cube3d.getPointsSize();

                                    if (cubeSize>0) {

                                        Integer pointToTake = 0;

                                        if (cubeSize >2){
                                            pointToTake = ThreadLocalRandom.current().nextInt(0, cubeSize);
                                        }


                                        int r = cube3d.getPoint(pointToTake).r;
                                        int g = cube3d.getPoint(pointToTake).g;
                                        int b = cube3d.getPoint(pointToTake).b;

                                        String rgbColor = toRgb(r,g,b);

                                        String pointLine = cube3d.getPoint(pointToTake).x + " "
                                                + cube3d.getPoint(pointToTake).y + " "
                                                + cube3d.getPoint(pointToTake).z + " "
                                                + rgbColor;
                                        cube3d.removePoint(pointToTake);

                                        bw.write(pointLine);
                                        bw.newLine();
                                        l++;
                                        counter++;
                                        updateProgress(counter, max);
                                    } else {
                                        iter.remove();
                                    }

                                }


                                if (l >= pointsPerFile || cube3dList.size()==0) {
                                    saving = false;
                                }

                            }


                        } catch (Exception ex) {
                            ex.printStackTrace();
                        } finally {
                            bw.close();
                            fw.close();
                        }


                    }


                } catch (Throwable e) {
                    e.printStackTrace();
                }


                Platform.runLater(
                        () -> {
                            setCurrentWork("");
                        }
                );
                return null;
            }

            private String toRgb(int r, int g, int b) {
                Integer rgb = new Color(r, g, b).getRGB();
                return rgb.toString();
            }


            public Octree buildOctree(AABB aabb, int height) {

                Octree octree = new Octree(aabb, height);
                AABB[] aabbs = aabb.createBoxes();
                if (height == 0) {
                    cube3dList.add(new Cube3d(aabb.getMinX(), aabb.getMaxX(), aabb.getMinY(), aabb.getMaxY(), aabb.getMinZ(), aabb.getMaxZ()));
                }

                for (int i = 0; i < 8; i++) {
                    if (height != 0) {
                        octree.setChildrenAt(i, buildOctree(aabbs[i], height - 1));
                    }
                }
                return octree;
            }

//            public Octree buildOctreeLocal(AABB aabb, int height) {
//
//                Octree octree = new Octree(aabb, height);
//                AABB[] aabbs = aabb.createBoxes();
//                if (height == 0) {
//                    cube3dListLocalTemp.add(new Cube3d(aabb.getMinX(), aabb.getMaxX(), aabb.getMinY(), aabb.getMaxY(), aabb.getMinZ(), aabb.getMaxZ()));
//                }
//
//                for (int i = 0; i < 8; i++) {
//                    if (height != 0) {
//                        octree.setChildrenAt(i, buildOctree(aabbs[i], height - 1));
//                    }
//                }
//                return octree;
//            }

        };
    }


    public Boolean getCurrentState() {
        return currentState;
    }

    public void setCurrentState(Boolean stateNew) {
        this.currentState = stateNew;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public void setPointsCount(long pointsCount) {
        this.pointsCount = pointsCount;
    }

    public void setFilesCount(long filesCount) {
        this.filesCount = filesCount;
    }


    public ObjectProperty<String> currentWorkProperty() {
        return currentWork;
    }

    public final String getCurrentWork() {
        return currentWorkProperty().get();
    }

    public final void setCurrentWork(String currentWork) {
        currentWorkProperty().set(currentWork);
    }

    public long getPointsExportCount() {
        return pointsExportCount;
    }

    public void setPointsExportCount(long pointsExportCount) {
        this.pointsExportCount = pointsExportCount;
    }

    public long getPointsPerFile() {
        return pointsPerFile;
    }

    public void setPointsPerFile(long pointsPerFile) {
        this.pointsPerFile = pointsPerFile;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }
}
