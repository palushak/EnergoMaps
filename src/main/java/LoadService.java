import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LoadService extends Service<Void> {

    private Boolean currentState;
    //private List<File> files;
    private File file;
    private ObjectProperty<String> currentWork = new SimpleObjectProperty<>();
    private ObjectProperty<String> fileNames = new SimpleObjectProperty<>();
    private ObjectProperty<Long> pointsCount = new SimpleObjectProperty<>();


    @Override
    protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() {

                Platform.runLater(
                        () -> {
                            setCurrentWork("Start");
                        }
                );

                try {

                    Platform.runLater(
                            () -> {
                                setPointsCount((long) 0);
                            }
                    );

                    StringBuilder fileNamesString = new StringBuilder();
//                    for (File file : files) {

                        if (file != null) {
                            fileNamesString.append(file.getName() + ", ");

                            Platform.runLater(
                                    () -> {
                                        setFileNames(fileNamesString.toString());
                                    }
                            );

                            try {
                                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                                String row = bufferedReader.readLine();

                                String[] polaHeaders;
                                polaHeaders = row.split(";");

//                                String head1 = polaHeaders[0].replace("\"", ""); //
                                String head2 = polaHeaders[1].replace("\"", ""); //
                                String head3 = polaHeaders[2].replace("\"", ""); //
//                                String head4 = polaHeaders[3].replace("\"", ""); //
                                String head5 = polaHeaders[4].replace("\"", ""); //

//                                System.out.println("Kontrola naglowka: 1 " + head1 + ", 2 " + head2 + ", 3 " + head3 + ", 4 " + head4 + ", 5 " + head5);

                                if (
                                                head2.equals("Ulica")
                                                && head3.equals("Nr domu")
                                                && head5.equals("Nr punktu poboru energii")
                                ) {
                                    Path path = Paths.get(file.getPath());
//                                    System.out.println(path);
                                    long pointsCountTemp = Files.lines(path, Charset.defaultCharset()).count()-1;
//                                    System.out.println(pointsCountTemp);

                                    Platform.runLater(
                                            () -> {
                                                setPointsCount(getPointsCount() + pointsCountTemp);
                                            }
                                    );
                                } else {
                                    System.out.println("sdfljhas");
                                    Platform.runLater(
                                            () -> {
                                                setCurrentWork("Błędny format pliku: " + file.getName());
                                            }
                                    );
                                    this.cancel();
                                }

                            } catch (FileNotFoundException ex) {
                                ex.printStackTrace();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }

//                    } end for files


                } catch (Exception e) {
                    e.printStackTrace();
                }
                Platform.runLater(
                        () -> {
                            setCurrentWork("Gotowe");
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


    public ObjectProperty<String> currentWorkProperty() {
        return currentWork;
    }

    public final String getCurrentWork() {
        return currentWorkProperty().get();
    }

    public final void setCurrentWork(String currentWork) {
        currentWorkProperty().set(currentWork);
    }

    public ObjectProperty<String> currentFileNamesProperty() {
        return fileNames;
    }

    public final String getFileNames() {
        return currentFileNamesProperty().get();
    }

    public final void setFileNames(String fileNames) {
        currentFileNamesProperty().set(fileNames);
    }

    public ObjectProperty<Long> currentPointsCountProperty() {
        return pointsCount;
    }

    public final Long getPointsCount() {
        return currentPointsCountProperty().get();
    }

    public final void setPointsCount(Long pointsCount) {
        currentPointsCountProperty().set(pointsCount);
    }



    public void setFile(File file) {
        this.file = file;
    }
}
