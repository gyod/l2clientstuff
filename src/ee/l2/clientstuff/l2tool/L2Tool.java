/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ee.l2.clientstuff.l2tool;

import ee.l2.clientstuff.files.streams.l2file.L2FileInputStream;
import ee.l2.clientstuff.l2encdec.L2DecTask;
import ee.l2.clientstuff.l2encdec.L2EncTask;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

/**
 * @author acmi
 */
public class L2Tool extends Application implements Initializable {
    private Stage stage;

    private ObjectProperty<File> originalFileProperty = new SimpleObjectProperty<>();
    private IntegerProperty cryptVersion = new SimpleIntegerProperty();
    private StringProperty cryptKey = new SimpleStringProperty("");

    @FXML
    TextField originalFilePath;
    @FXML
    ComboBox<Integer> cryptVersionCB;
    @FXML
    TextField cryptKeyTF;
    @FXML
    Button crypt;
    @FXML
    ProgressIndicator encdecProgress;

    @FXML
    TextField ddsPath;
    @FXML
    TextField utxPath;
    @FXML
    RadioButton c0;
    @FXML
    RadioButton ct1;
    @FXML
    RadioButton ct23;
    ToggleGroup l2version = new ToggleGroup();
    @FXML
    ComboBox<String> textures;
    @FXML
    Button set;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;

        Parent p = FXMLLoader.load(L2Tool.class.getResource("/ee/l2/clientstuff/l2tool/l2tool.fxml"));
        stage.setScene(new Scene(p));
        stage.setTitle("L2Tool");
        stage.setResizable(false);
        stage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        encdecProgress.setVisible(false);

        cryptVersionCB.getItems().addAll(111, 120, 121, 211, 212, 411, 412, 413, 414);
        cryptVersionCB.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer integer2) {
                switch (integer2) {
                    case 121:
                        cryptKeyTF.setDisable(false);
                        File file;
                        if ((file = originalFileProperty.getValue()) != null)
                            cryptKeyTF.setText(file.getName());
                        break;
                    default:
                        cryptKeyTF.setDisable(true);
                        cryptKeyTF.setText("");
                }
            }
        });
        cryptVersionCB.getSelectionModel().select(0);

        cryptVersion.bind(cryptVersionCB.getSelectionModel().selectedItemProperty());
        cryptKeyTF.textProperty().bindBidirectional(cryptKey);

        originalFileProperty.addListener(new ChangeListener<File>() {
            @Override
            public void changed(ObservableValue<? extends File> observableValue, File file, File file2) {
                if (file2 == null)
                    return;

                crypt.setText("enc");
                cryptKeyTF.setText("");
                cryptVersionCB.setDisable(false);

                FileInputStream fis;
                try {
                    fis = new FileInputStream(file2);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                originalFilePath.setText(file2.getAbsolutePath());
                cryptKeyTF.setText(file2.getName());

                try {
                    int version = L2FileInputStream.readVersion(fis);
                    cryptVersionCB.getSelectionModel().select(Integer.valueOf(version));
                    cryptVersionCB.setDisable(true);
                    crypt.setText("dec");
                } catch (IOException ignore) {
                }
            }
        });

        l2version.getToggles().addAll(c0, ct1, ct23);
        l2version.selectToggle(c0);
    }

    public void openOriginalFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");

        originalFileProperty.setValue(fileChooser.showOpenDialog(stage));
    }

    public void processFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");

        final File dest = fileChooser.showSaveDialog(stage);
        if (dest == null)
            return;

        new Thread() {
            @Override
            public void run() {
                try {
                    int version = cryptVersion.intValue();
                    final Callable<Void> task = version == 0 ?
                            new L2DecTask(new FileInputStream(originalFileProperty.get()), new FileOutputStream(dest), cryptKey.getValue(), true) :
                            new L2EncTask(new FileInputStream(originalFileProperty.get()), new FileOutputStream(dest), version, cryptKey.getValue(), false);

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            encdecProgress.setVisible(true);
                        }
                    });

                    task.call();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            encdecProgress.setVisible(false);
                        }
                    });
                }
            }
        }.start();
    }

    public void selectDDS() {
        //TODO
    }

    public void selectUTX() {
        //TODO
    }

    public void showUTXTree() {
        //TODO
    }

    public void replaceTexture() {
        //TODO
    }

    public void convertToUnrealED() {
        //TODO
    }

    public static void main(String[] args) {
        launch(L2Tool.class, args);
    }
}
