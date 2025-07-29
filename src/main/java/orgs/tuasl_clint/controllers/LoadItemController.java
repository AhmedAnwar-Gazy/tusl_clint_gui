//
//package orgs.tuasl_clint.controllers;
//
//import javafx.event.ActionEvent;
//import javafx.fxml.FXML;
//import javafx.fxml.Initializable;
//import javafx.scene.control.Button;
//import javafx.scene.control.Label;
//import javafx.scene.control.ProgressIndicator;
//import javafx.scene.image.ImageView;
//import javafx.scene.input.MouseEvent;
//import javafx.scene.layout.HBox;
//import orgs.tuasl_clint.client.ChatClient;
//import orgs.tuasl_clint.client.OnFileTransferListener;
//import orgs.tuasl_clint.models2.Media;
//import orgs.tuasl_clint.protocol.Response;
//import orgs.tuasl_clint.utils.FilesHelper;
//
//import javax.swing.*;
//import java.io.File;
//import java.net.URL;
//import java.sql.SQLException;
//import java.util.ResourceBundle;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReentrantLock;
//
//public class LoadItemController implements Initializable {
//
//    @FXML
//    private HBox buttonsContainer;
//
//    @FXML
//    private Button cancelDownloadButton;
//
//    @FXML
//    private Button downloadButton;
//
//    @FXML
//    private ImageView downloadImg;
//
//    @FXML
//    private ProgressIndicator downloadProgressPar;
//
//    @FXML
//    private Label fileInfoLabel;
//
//    @FXML
//    private Label fileNameLabel;
//
//    @FXML
//    private HBox readyFileContainer;
//
//    @Override
//    public void initialize(URL url, ResourceBundle resourceBundle) {
//        buttonsContainer.getChildren().remove(cancelDownloadButton);
//    }
//    private static final String FILES_ROOT_PATH = FilesHelper.getFilesRootPath();
//    private String FOLDER_FOR_MEDIA;
//
//    public static enum FileState {
//        READY,
//        IS_ON_DOWNLOADING,
//        NOT_DOWNLOADED,
//        FINISHED_NOW,
//        TERMINATED
//    }
//
//    File file;
//    Media media;
//
//    public interface OnReadyItemListener {
//        void onReadyItem(HBox fileItemContainer);
//    }
//
//    public interface OnDownloadingItemListener {
//        void onDownloadingItem(long currentSize, long fileSize, ProgressIndicator progressIndicator);
//    }
//
//    public interface OnDownloadButtonClickedListener {
//        void onDownloadButtonClicked(Media media, File file);
//    }
//
//    public interface OnCancelDownloadButtonClickedListener {
//        void onCancelDownloadButtonClicked(Media media, File file);
//    }
//
//    private OnDownloadingItemListener onDownloadingItemListener;
//    private OnReadyItemListener onReadyItemListener;
//    private OnDownloadButtonClickedListener onDownloadButtonClickedListener;
//    private OnCancelDownloadButtonClickedListener onCancelDownloadButtonClickedListener;
//
//    private final Lock threadLock = new ReentrantLock();
//    private volatile FileState state = FileState.NOT_DOWNLOADED;
//    private Thread updateDownloadingStatusThread;
//
//    @FXML
//    void handleCancelDownloadItemClicked(ActionEvent event) {
//        threadLock.lock();
//        try {
//            if (this.onCancelDownloadButtonClickedListener != null) {
//                this.onCancelDownloadButtonClickedListener.onCancelDownloadButtonClicked(this.media, this.file);
//            }
//            this.state = FileState.NOT_DOWNLOADED;
//
//            buttonsContainer.getChildren().remove(this.cancelDownloadButton);
//            buttonsContainer.getChildren().add(this.downloadButton);
//
//            if (this.updateDownloadingStatusThread != null) {
//                this.updateDownloadingStatusThread.interrupt();
//            }
//        } finally {
//            threadLock.unlock();
//        }
//    }
//
//    @FXML
//    void handleCancelDownloadItemClickedI(MouseEvent event) {
//        threadLock.lock();
//        try {
//            if (this.onCancelDownloadButtonClickedListener != null) {
//                this.onCancelDownloadButtonClickedListener.onCancelDownloadButtonClicked(this.media, this.file);
//            }
//            this.state = FileState.NOT_DOWNLOADED;
//
//            buttonsContainer.getChildren().remove(this.cancelDownloadButton);
//            buttonsContainer.getChildren().add(this.downloadButton);
//
//            if (this.file != null) {
//                this.fileNameLabel.setText(file.getName());
//                this.fileInfoLabel.setText(String.valueOf(this.media.getFileSize()) + "    " +
//                        FilesHelper.getFileExtension(file));
//            }
//
//            if (this.updateDownloadingStatusThread != null) {
//                this.updateDownloadingStatusThread.interrupt();
//            }
//        } finally {
//            threadLock.unlock();
//        }
//    }
//
//    @FXML
//    void handleDownloadItemClicked(ActionEvent event) {
//        System.out.println("File Is Downloading");
//            if(file == null)
//                file= new File(FilesHelper.getFilesPath(media)+media.getFileName());
//            System.out.println("Media Object Is : "+media.toString());
//            System.out.println("File Path Is : "+ file.getAbsolutePath());
//            System.out.println("Files Path From Helper Is :"+FilesHelper.getFilesPath(media));
//            System.out.println("RootFile Is : "+FilesHelper.getFilesRootPath());
//                Response res =  ChatClient.getInstance().getFileByMedia(media/*Hint:The media Object Of The Message*/,FILES_ROOT_PATH+FOLDER_FOR_MEDIA+media.getFileName() /*Hint:  file path to save to*/, new OnFileTransferListener()/*Hint: Run On The Clint Object*/ {
//                    @Override
//                    public void onFail(String msg) {
//                        JOptionPane.showMessageDialog(null,msg);
//                        buttonsContainer.getChildren().add(downloadButton);
//                        buttonsContainer.getChildren().remove(cancelDownloadButton);
//                    }
//
//                    @Override
//                    public void onProgress(long transferredBytes, long totalSize) {
//                        downloadProgressPar.setProgress((double) transferredBytes / totalSize);
//                        state = FileState.IS_ON_DOWNLOADING;
//                    }
//
//                    @Override
//                    public void onComplete(File file) {
//                        state = FileState.READY;
//                        System.out.println("Success Response From Get File By Media");
//                        if(onReadyItemListener != null)
//                            onReadyItemListener.onReadyItem(readyFileContainer);
//                        String name = media.getFileName();
//                        media.setFileName(file.getName());
//                        try {
//                            media.update();
//                        } catch (SQLException e) {
//                            System.out.println("----- Fail To Update The Media File Name");
//                            media.setFileName(name);
//                        }
//                    }
//                });
//        threadLock.lock();
//        try {
//
//
//            buttonsContainer.getChildren().remove(this.downloadButton);
//            buttonsContainer.getChildren().add(this.cancelDownloadButton);
//
//            if (this.onDownloadButtonClickedListener != null) {
//                this.onDownloadButtonClickedListener.onDownloadButtonClicked(this.media, this.file);
//            }
//
//            if (this.updateDownloadingStatusThread == null ||
//                    this.updateDownloadingStatusThread.getState() == Thread.State.TERMINATED) {
//                initializeUpdateThread();
//            } else if (this.updateDownloadingStatusThread.isAlive()) {
//                this.updateDownloadingStatusThread.interrupt();
//            }
//        } finally {
//            threadLock.unlock();
//        }
//    }
//
//    private void initializeUpdateThread() {
//        this.updateDownloadingStatusThread = new Thread(() -> {
//            try {
//                while (!Thread.currentThread().isInterrupted() &&
//                        state == FileState.IS_ON_DOWNLOADING) {
//
//                    long currentSize = (file != null && file.exists()) ? file.length() : 0;
//
//                    javafx.application.Platform.runLater(() -> {
//                        if (onDownloadingItemListener != null) {
//                            onDownloadingItemListener.onDownloadingItem(
//                                    currentSize,
//                                    media.getFileSize(),
//                                    downloadProgressPar
//                            );
//                        }
//                    });
//
//                    TimeUnit.SECONDS.sleep(1);
//                }
//
//                if (this.state == FileState.READY && this.onReadyItemListener != null) {
//                    javafx.application.Platform.runLater(() -> {
//                        if(this.file.isFile() && this.file.exists()){
//                            File newFile = new File(file.getPath(),file.getName());
//                            if(newFile.exists()){
//                                if(newFile.delete() && file.renameTo(newFile)){
//                                    System.out.println("----- From LoadItemController : File Is Ready And Is Not Temp Now..!!");
//                                }else {
//                                    System.out.println("----- From LoadItemController : Cannot Rename The File This Will Addume That The File Is Not Downloaded..And Wont Be Obenable...!!");
//                                }
//                            }else if(file.renameTo(newFile)){
//                                System.out.println("----- From LoadItemController : File Is Ready And Is Not Temp Now..!!");
//                            }
//                        }
//                            onReadyItemListener.onReadyItem(readyFileContainer);
//
//                    });
//                }
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//        this.updateDownloadingStatusThread.setDaemon(true);
//        this.updateDownloadingStatusThread.start();
//    }
//
//    public void setOnDownloadingItemListener(OnDownloadingItemListener listener) {
//        this.onDownloadingItemListener = listener;
//    }
//
//    public void setOnReadyItemListener(OnReadyItemListener listener) {
//        this.onReadyItemListener = listener;
//    }
//
//    public void setOnDownloadButtonClickedListener(OnDownloadButtonClickedListener listener) {
//        this.onDownloadButtonClickedListener = listener;
//        initializeUpdateThread();
//    }
//
//    public void setOnCancelDownloadButtonClickedListener(OnCancelDownloadButtonClickedListener listener) {
//        this.onCancelDownloadButtonClickedListener = listener;
//    }
//
//    public void setMedia(Media media) {
//        this.media = media;
//        FOLDER_FOR_MEDIA = switch (FilesHelper.getFileType(this.extention_of(media.getFilePathOrUrl()))){
//            case AUDIO -> "voiceNote";
//            case IMAGE -> "images";
//            case VIDEO -> "videos";
//            default -> "file";
//        }+"/";
//        this.fileNameLabel.setText(media.getFileName());
//        this.fileInfoLabel.setText(FilesHelper.formatFileSize(media.getFileSize())+"  "+media.getMediaType());
//        if(this.file == null || !this.file.exists()){
//            this.file = new File(FilesHelper.getFilesPath(media)+media.getFilePathOrUrl());
//        }
//    }
//
//    public void setFile(File file) {
//        this.file = file;
//    }
//
//    public void setState(FileState state) {
//        this.state = state;
//    }
//}


package orgs.tuasl_clint.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import orgs.tuasl_clint.client.ChatClient;
import orgs.tuasl_clint.client.OnFileTransferListener;
import orgs.tuasl_clint.models2.Media;
import orgs.tuasl_clint.protocol.Response;
import orgs.tuasl_clint.utils.FilesHelper;

import javax.swing.*;
import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LoadItemController implements Initializable,Controller {

    @FXML private HBox buttonsContainer;
    @FXML private Button cancelDownloadButton;
    @FXML private Button downloadButton;
    @FXML private ImageView downloadImg;
    @FXML private ProgressIndicator downloadProgressPar;
    @FXML private Label fileInfoLabel;
    @FXML private Label fileNameLabel;
    @FXML private HBox readyFileContainer;
    @FXML private VBox mainContainer;

    private static final String FILES_ROOT_PATH = "src/main/resources/orgs/tuasl_clint/";//= FilesHelper.getFilesRootPath();
    private String FOLDER_FOR_MEDIA;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Lock threadLock = new ReentrantLock();
    private final AtomicBoolean isDownloading = new AtomicBoolean(false);

    private File file;
    private Media media;
    private FileState state = FileState.NOT_DOWNLOADED;

    private StackPane mainView;
    @Override
    public StackPane getView() {
        if(mainView == null)
            mainView = new StackPane(mainContainer);
        return mainView;
    }

    public enum FileState {
        READY,
        IS_ON_DOWNLOADING,
        NOT_DOWNLOADED,
        FINISHED_NOW,
        TERMINATED
    }

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        buttonsContainer.getChildren().remove(cancelDownloadButton);
        downloadProgressPar.setProgress(0);
    }

    @FXML
    void handleCancelDownloadItemClicked(ActionEvent event) {
        handleCancelDownload();
    }

    @FXML
    void handleCancelDownloadItemClickedI(MouseEvent event) {
        handleCancelDownload();
    }

    private void handleCancelDownload() {
        threadLock.lock();
        try {
            if (onCancelDownloadButtonClickedListener != null) {
                onCancelDownloadButtonClickedListener.onCancelDownloadButtonClicked(media, file);
            }
            state = FileState.NOT_DOWNLOADED;
            isDownloading.set(false);

            Platform.runLater(() -> {
                buttonsContainer.getChildren().remove(cancelDownloadButton);
                buttonsContainer.getChildren().add(downloadButton);
                downloadProgressPar.setProgress(0);

                if (file != null) {
                    fileNameLabel.setText(file.getName());
                    fileInfoLabel.setText(String.valueOf(media.getFileSize()) + "    " +
                            FilesHelper.getFileExtension(file));
                }
            });
        } finally {
            threadLock.unlock();
        }
    }

    @FXML
    void handleDownloadItemClicked(ActionEvent event) {
        if (isDownloading.get()) {
            return;
        }
        isDownloading.set(true);

        executorService.execute(() -> {
            try {
                if (file == null) {
                    file = new File(FilesHelper.getFilesPath(media) + media.getFileName());
                }

                System.out.println("Starting download...");
                System.out.println("Media Object: " + media);
                System.out.println("File Path: " + file.getAbsolutePath());
                System.out.println("Files Path From Helper: " + FilesHelper.getFilesPath(media));
                System.out.println("RootFile: " + FilesHelper.getFilesRootPath());
                Platform.runLater(() -> {
                    buttonsContainer.getChildren().remove(downloadButton);
                    buttonsContainer.getChildren().add(cancelDownloadButton);
                });
                if(this.onDownloadButtonClickedListener != null)
                    onDownloadButtonClickedListener.onDownloadButtonClicked(media,file);
                media.setFileName(media.getFilePathOrUrl());
                System.out.println(ChatClient.getInstance().getFileByMedia(
                        media,
                        FILES_ROOT_PATH + FOLDER_FOR_MEDIA.substring(0,FOLDER_FOR_MEDIA.length()-1),
                        new OnFileTransferListener() {
                            @Override
                            public void onFail(String msg) {
                                Platform.runLater(() -> {
                                    System.out.println("------- An Error Occurred While Reciving The File Error Message Is : "+ msg);
                                    JOptionPane.showMessageDialog(null, msg);
                                    buttonsContainer.getChildren().add(downloadButton);
                                    buttonsContainer.getChildren().remove(cancelDownloadButton);
                                    isDownloading.set(false);
                                });
                            }

                            @Override
                            public void onProgress(long transferredBytes, long totalSize) {
                                Platform.runLater(() -> {
                                    System.out.println("------ FIle is download ...."+transferredBytes+" / "+totalSize);
                                    downloadProgressPar.setProgress((double) transferredBytes / totalSize);
                                    state = FileState.IS_ON_DOWNLOADING;
                                });
                            }

                            @Override
                            public void onComplete(File file) {
                                Platform.runLater(() -> {
                                    state = FileState.READY;
                                    System.out.println("Success Response From Get File By Media");

                                    if (onReadyItemListener != null) {
                                        onReadyItemListener.onReadyItem(readyFileContainer);
                                    }
                                    String name = media.getFileName();
                                    media.setFileName(file.getName());
                                    try {
                                        media.update();
                                    } catch (SQLException e) {
                                        System.out.println("----- Fail To Update The Media File Name");
                                        media.setFileName(name);
                                    }
                                    isDownloading.set(false);
                                    ChatClient.getInstance().responseQueue.offer(new Response(true,"File Recived Sucessfully",null));
                                });
                            }
                        }
                ).toString());
            } catch (Exception e) {
                Platform.runLater(() -> {
                    JOptionPane.showMessageDialog(null, "Download failed: " + e.getMessage());
                    buttonsContainer.getChildren().add(downloadButton);
                    buttonsContainer.getChildren().remove(cancelDownloadButton);
                });
                isDownloading.set(false);
            }
        });
    }

    public void setOnDownloadingItemListener(OnDownloadingItemListener listener) {
        this.onDownloadingItemListener = listener;
    }

    public void setOnReadyItemListener(OnReadyItemListener listener) {
        this.onReadyItemListener = listener;
    }

    public void setOnDownloadButtonClickedListener(OnDownloadButtonClickedListener listener) {
        this.onDownloadButtonClickedListener = listener;
    }

    public void setOnCancelDownloadButtonClickedListener(OnCancelDownloadButtonClickedListener listener) {
        this.onCancelDownloadButtonClickedListener = listener;
    }

    public void setMedia(Media media) {
        this.media = media;
        FOLDER_FOR_MEDIA = switch (FilesHelper.getFileType(this.extention_of(media.getFilePathOrUrl()))) {
            case AUDIO -> "voiceNote";
            case IMAGE -> "images";
            case VIDEO -> "videos";
            default -> "file";
        } + "/";

        Platform.runLater(() -> {
            this.fileNameLabel.setText(media.getFileName());
            this.fileInfoLabel.setText(FilesHelper.formatFileSize(media.getFileSize()) + "  " + media.getMediaType());
        });

        if (this.file == null || !this.file.exists()) {
            this.file = new File(FilesHelper.getFilesPath(media) +  media.getFilePathOrUrl());
        }
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setState(FileState state) {
        this.state = state;
    }

    private String extention_of(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        int lastDot = path.lastIndexOf('.');
        return lastDot > 0 ? path.substring(lastDot + 1) : "";
    }

    public void shutdown() {
//        executorService.shutdownNow();
//        try {
//            if (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
//                executorService.shutdownNow();
//            }
//        } catch (InterruptedException e) {
//            executorService.shutdownNow();
//            Thread.currentThread().interrupt();
//        }
    }
}