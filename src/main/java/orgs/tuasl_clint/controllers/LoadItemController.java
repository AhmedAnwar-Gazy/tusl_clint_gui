
package orgs.tuasl_clint.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import orgs.tuasl_clint.client.ChatClient;
import orgs.tuasl_clint.client.OnFileTransferListener;
import orgs.tuasl_clint.models2.Media;
import orgs.tuasl_clint.protocol.Response;
import orgs.tuasl_clint.utils.FilesHelper;

import javax.swing.*;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LoadItemController implements Initializable {

    @FXML
    private HBox buttonsContainer;

    @FXML
    private Button cancelDownloadButton;

    @FXML
    private Button downloadButton;

    @FXML
    private ImageView downloadImg;

    @FXML
    private ProgressIndicator downloadProgressPar;

    @FXML
    private Label fileInfoLabel;

    @FXML
    private Label fileNameLabel;

    @FXML
    private HBox readyFileContainer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        buttonsContainer.getChildren().remove(cancelDownloadButton);
    }
    private static final String FILES_ROOT_PATH = "src/main/resources/orgs/tuasl_clint/";
    private String FOLDER_FOR_MEDIA;

    public static enum FileState {
        READY,
        IS_ON_DOWNLOADING,
        NOT_DOWNLOADED,
        FINISHED_NOW,
        TERMINATED
    }

    File file;
    Media media;

    public interface OnReadyItemListener {
        void onReadyItem(HBox fileItemContainer);
    }

    public interface OnDownloadingItemListener {
        void onDownloadingItem(long currentSize, long fileSize, ProgressIndicator progressIndicator);
    }

    public interface OnDownloadButtonClickedListener {
        void onDownloadButtonClicked(Media media, File file);
    }

    public interface OnCancelDownloadButtonClickedListener {
        void onCancelDownloadButtonClicked(Media media, File file);
    }

    private OnDownloadingItemListener onDownloadingItemListener;
    private OnReadyItemListener onReadyItemListener;
    private OnDownloadButtonClickedListener onDownloadButtonClickedListener;
    private OnCancelDownloadButtonClickedListener onCancelDownloadButtonClickedListener;

    private final Lock threadLock = new ReentrantLock();
    private volatile FileState state = FileState.NOT_DOWNLOADED;
    private Thread updateDownloadingStatusThread;

    @FXML
    void handleCancelDownloadItemClicked(ActionEvent event) {
        threadLock.lock();
        try {
            if (this.onCancelDownloadButtonClickedListener != null) {
                this.onCancelDownloadButtonClickedListener.onCancelDownloadButtonClicked(this.media, this.file);
            }
            this.state = FileState.NOT_DOWNLOADED;

            buttonsContainer.getChildren().remove(this.cancelDownloadButton);
            buttonsContainer.getChildren().add(this.downloadButton);

            if (this.updateDownloadingStatusThread != null) {
                this.updateDownloadingStatusThread.interrupt();
            }
        } finally {
            threadLock.unlock();
        }
    }

    @FXML
    void handleCancelDownloadItemClickedI(MouseEvent event) {
        threadLock.lock();
        try {
            if (this.onCancelDownloadButtonClickedListener != null) {
                this.onCancelDownloadButtonClickedListener.onCancelDownloadButtonClicked(this.media, this.file);
            }
            this.state = FileState.NOT_DOWNLOADED;

            buttonsContainer.getChildren().remove(this.cancelDownloadButton);
            buttonsContainer.getChildren().add(this.downloadButton);

            if (this.file != null) {
                this.fileNameLabel.setText(file.getName());
                this.fileInfoLabel.setText(String.valueOf(this.media.getFileSize()) + "    " +
                        FilesHelper.getFileExtension(file));
            }

            if (this.updateDownloadingStatusThread != null) {
                this.updateDownloadingStatusThread.interrupt();
            }
        } finally {
            threadLock.unlock();
        }
    }

    @FXML
    void handleDownloadItemClicked(ActionEvent event) {
        System.out.println("File Is Downloading");
            if(file == null)
                file= new File(FilesHelper.getFilesPath(media)+media.getFileName());
            System.out.println("Media Object Is : "+media.toString());
            System.out.println("File Path Is : "+ file.getAbsolutePath());
            System.out.println("Files Path From Helper Is :"+FilesHelper.getFilesPath(media));
            System.out.println("RootFile Is : "+FilesHelper.getFilesRootPath());
                Response res =  ChatClient.getInstance().getFileByMedia(media/*Hint:The media Object Of The Message*/, file.getAbsolutePath()/*Hint:  file path to save to*/, new OnFileTransferListener()/*Hint: Run On The Clint Object*/ {
                    @Override
                    public void onFail(String msg) {
                        JOptionPane.showMessageDialog(null,msg);
                        buttonsContainer.getChildren().add(downloadButton);
                        buttonsContainer.getChildren().remove(cancelDownloadButton);
                    }

                    @Override
                    public void onProgress(long transferredBytes, long totalSize) {
                        downloadProgressPar.setProgress((double) transferredBytes / totalSize);
                        state = FileState.IS_ON_DOWNLOADING;
                    }

                    @Override
                    public void onComplete(File file) {
                        state = FileState.READY;
                        System.out.println("Success Response From Get File By Media");
                        if(onReadyItemListener != null)
                            onReadyItemListener.onReadyItem(readyFileContainer);
                    }
                });
        threadLock.lock();
        try {


            buttonsContainer.getChildren().remove(this.downloadButton);
            buttonsContainer.getChildren().add(this.cancelDownloadButton);

            if (this.onDownloadButtonClickedListener != null) {
                this.onDownloadButtonClickedListener.onDownloadButtonClicked(this.media, this.file);
            }

            if (this.updateDownloadingStatusThread == null ||
                    this.updateDownloadingStatusThread.getState() == Thread.State.TERMINATED) {
                initializeUpdateThread();
            } else if (this.updateDownloadingStatusThread.isAlive()) {
                this.updateDownloadingStatusThread.interrupt();
            }
        } finally {
            threadLock.unlock();
        }
    }

    private void initializeUpdateThread() {
        this.updateDownloadingStatusThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted() &&
                        state == FileState.IS_ON_DOWNLOADING) {

                    long currentSize = (file != null && file.exists()) ? file.length() : 0;

                    javafx.application.Platform.runLater(() -> {
                        if (onDownloadingItemListener != null) {
                            onDownloadingItemListener.onDownloadingItem(
                                    currentSize,
                                    media.getFileSize(),
                                    downloadProgressPar
                            );
                        }
                    });

                    TimeUnit.SECONDS.sleep(1);
                }

                if (this.state == FileState.READY && this.onReadyItemListener != null) {
                    javafx.application.Platform.runLater(() -> {
                        if(this.file.isFile() && this.file.exists()){
                            File newFile = new File(file.getPath(),file.getName());
                            if(newFile.exists()){
                                if(newFile.delete() && file.renameTo(newFile)){
                                    System.out.println("----- From LoadItemController : File Is Ready And Is Not Temp Now..!!");
                                }else {
                                    System.out.println("----- From LoadItemController : Cannot Rename The File This Will Addume That The File Is Not Downloaded..And Wont Be Obenable...!!");
                                }
                            }else if(file.renameTo(newFile)){
                                System.out.println("----- From LoadItemController : File Is Ready And Is Not Temp Now..!!");
                            }
                        }
                            onReadyItemListener.onReadyItem(readyFileContainer);

                    });
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        this.updateDownloadingStatusThread.setDaemon(true);
        this.updateDownloadingStatusThread.start();
    }

    public void setOnDownloadingItemListener(OnDownloadingItemListener listener) {
        this.onDownloadingItemListener = listener;
    }

    public void setOnReadyItemListener(OnReadyItemListener listener) {
        this.onReadyItemListener = listener;
    }

    public void setOnDownloadButtonClickedListener(OnDownloadButtonClickedListener listener) {
        this.onDownloadButtonClickedListener = listener;
        initializeUpdateThread();
    }

    public void setOnCancelDownloadButtonClickedListener(OnCancelDownloadButtonClickedListener listener) {
        this.onCancelDownloadButtonClickedListener = listener;
    }

    public void setMedia(Media media) {
        this.media = media;
        FOLDER_FOR_MEDIA = switch (FilesHelper.)
        this.fileNameLabel.setText(media.getFileName());
        this.fileInfoLabel.setText(FilesHelper.formatFileSize(media.getFileSize())+"  "+media.getMediaType());
        if(this.file == null || !this.file.exists()){
            this.file = new File(FilesHelper.getFilesPath(media)+media.getFilePathOrUrl());
        }
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setState(FileState state) {
        this.state = state;
    }
}