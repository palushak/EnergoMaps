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
    //private List<File> files;
    private File file;
    private long pointsCount;

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

                counter = 0;
                max = 0;
                currentState = true;

                heightLevel = 0;
                splitAAbbs = 1;



                try {

//                    for (File file : files) {

                        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                        String row;

                        Platform.runLater(
                                () -> {
                                    setCurrentWork("1 z 3: dekodowanie plikow...");
                                }
                        );

                        while ((row = bufferedReader.readLine()) != null) {
                            if (!currentState) return null;



                            counter++;
                            updateProgress(counter, max);
                        }

                    //} end for files


                } catch (Exception e) {
                    e.printStackTrace();
                }


                try {
                    Platform.runLater(
                            () -> {
                                setCurrentWork("2 z 3: dzielenie na warstwy ...");
                            }
                    );




                } catch (Exception e) {
                    e.printStackTrace();
                }


                String myDocuments;
                File dirMain;
                String fName;
                File dirMain2;

                try {
                    Platform.runLater(
                            () -> {
                                setCurrentWork("3 z 3: generowanie plikow KML...");
                            }
                    );


                    if (!currentState) return null;

                    myDocuments = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();

                    dirMain = new File(myDocuments + "\\" + "EZ_KML-pliki");

                    if (!dirMain.exists()) {
                        dirMain.mkdir();
                    }

                    fName = file.getName().replaceFirst("[.][^.]+$", "");


//                    dirMain2 = new File(dirMain + "\\" + fName);
//
//                    if (!dirMain2.exists()) {
//                        dirMain2.mkdir();
//                    }
//                    mainFilesPath = dirMain2.toString() + "\\" + fName;

                    String filename = dirMain.toString() + "\\" + fName + "__1.kml";

                    FileWriter fw = new FileWriter(filename);
                    BufferedWriter bw = new BufferedWriter(fw);


                    try {
                        bw.write("VERSION .7");
                        bw.newLine();





                            counter++;
                            updateProgress(counter, max);


                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        bw.close();
                        fw.close();
                    }



                } catch (Exception e) {
                    e.printStackTrace();
                }





                Platform.runLater(
                        () -> {
                            setCurrentWork("");
                        }
                );
                return null;
            }




        };
    }


    public Boolean getCurrentState() {
        return currentState;
    }

    public void setCurrentState(Boolean stateNew) {
        this.currentState = stateNew;
    }

//    public void setFiles(List<File> files) {
//        this.files = files;
//    }

    public void setPointsCount(long pointsCount) {
        this.pointsCount = pointsCount;
    }

//    public void setFilesCount(long filesCount) {
//        this.filesCount = filesCount;
//    }


    public ObjectProperty<String> currentWorkProperty() {
        return currentWork;
    }

    public final String getCurrentWork() {
        return currentWorkProperty().get();
    }

    public final void setCurrentWork(String currentWork) {
        currentWorkProperty().set(currentWork);
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

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
