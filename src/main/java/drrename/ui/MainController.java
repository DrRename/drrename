package drrename.ui;

import drrename.*;
import drrename.config.AppConfig;
import drrename.event.MainViewButtonCancelEvent;
import drrename.event.MainViewButtonGoEvent;
import drrename.filecreator.DummyFileCreatorController;
import drrename.kodi.KodiToolsController;
import drrename.ui.mainview.GoCancelButtonsComponentController;
import drrename.ui.mainview.ReplacementStringComponentController;
import drrename.ui.mainview.StartDirectoryComponentController;
import drrename.ui.mainview.controller.FileListComponentController;
import drrename.model.RenamingEntry;
import drrename.service.EntriesService;
import drrename.strategy.*;
import drrename.ui.service.FileTypeService;
import drrename.ui.service.LoadPathsService;
import drrename.ui.service.PreviewService;
import drrename.ui.service.RenamingService;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Slf4j
@Component
@FxmlView("/fxml/MainView.fxml")
public class MainController implements Initializable {

    private static final String RENAMING_FILES = "mainview.status.renaming_files";

    private static final String LOADING_FILES = "mainview.status.loading_files";

    private static final String LOADING_PREVIEVS = "mainview.status.loading_previews";

    private static final String LOADING_FILE_TYPES = "mainview.status.loading_filetypes";

    private final AppConfig config;

    private final LoadPathsService loadPathsService;
    private final PreviewService previewService;

    private final FileTypeService fileTypeService;

    private final ResourceBundle resourceBundle;

    private final Executor executor;

    public HBox goCancelButtonsComponent;

    public BorderPane layer04_2;

    public VBox fileListComponent;

    public BorderPane startDirectoryComponent;

    public HBox replacementStringComponent;

    private final RenamingService renamingService;

    private final EntriesService entriesService;

    public ListView<Control> content1;

    public ListView<Control> content2;

    public Label statusLabelLoaded;

    public Label statusLabelLoadedFileTypes;

    public Label statusLabelFilesWillRename;

    public Label statusLabelFilesWillRenameFileTypes;

    public Label statusLabelRenamed;

    public Label statusLabelRenamedFileTypes;

    public Label loadingFilesStatusLabel;

    public Label previewFilesStatusLabel;

    public Label renameFilesStatusLabel;

    public VBox statusBox;

    public Label fileTypeStatusLabel;

    public ComboBox<RenamingStrategy> comboBoxRenamingStrategy;

    public TextField textFieldReplacementStringFrom;

    public TextField textFieldReplacementStringTo;

    public CheckBox showOnlyChanging;

    private ChangeListener<? super String> replaceStringFromChangeListener;

    private ChangeListener<? super String> replaceStringToChangeListener;

    private ChangeListener<? super Boolean> ignoreDirectoriesChangeListener;

    private ChangeListener<? super Boolean> ignoreHiddenFilesChangeListener;

    private ChangeListener<? super String> textFieldChangeListener;

    @FXML
    private ProgressBar progressBar;
    @FXML
    MenuBar menuBar;

    @FXML
    Node layer01;

    @FXML
    Node layer02_3;

    @FXML
    Node comboboxBox;

    @FXML
    Node layer04_1;
    @FXML
    CheckBox ignoreHiddenFiles;
    @FXML
    CheckBox ignoreDirectories;

    @FXML
    Node layer05_1;

    private final FxWeaver fxWeaver;

    public GoCancelButtonsComponentController goCancelButtonsComponentController;

    public StartDirectoryComponentController startDirectoryComponentController;

    public FileListComponentController fileListComponentController;

    public ReplacementStringComponentController replacementStringComponentController;

    private final BooleanProperty draggingOver = new SimpleBooleanProperty();

    private final ListProperty<Path> loadedPaths = new SimpleListProperty<>(FXCollections.observableArrayList());

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        content1 = fileListComponentController.content1;
        content2 = fileListComponentController.content2;

        textFieldReplacementStringTo = replacementStringComponentController.textFieldReplacementStringTo;
        textFieldReplacementStringFrom = replacementStringComponentController.textFieldReplacementStringFrom;

        initAppMenu(menuBar);

        initRenamingStrategies();

        initServices();

        registerInputChangeListener();

        configureButtons();

        configureStatusLabels();

        initDragAndDrop();

        progressBar.visibleProperty().bind(loadPathsService.runningProperty().or(previewService.runningProperty().or(renamingService.runningProperty())));

        if (config.isDebug())
            applyRandomColors();

        entriesService.getEntries().addListener((ListChangeListener<RenamingEntry>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    var list = new ArrayList<>(c.getAddedSubList());
                    executor.execute(() -> addToContent(list));
                }
            }
        });

        /* Make scrolling of both lists symmetrical */
        Platform.runLater(() -> {
            FXUtil.getListViewScrollBar(content1).valueProperty()
                    .bindBidirectional(FXUtil.getListViewScrollBar(content2).valueProperty());
        });



    }

    private void initDragAndDrop() {
        startDirectoryComponentController.textFieldDirectory.setOnDragEntered(this::handleDragEvent);
        startDirectoryComponentController.textFieldDirectory.setOnDragEntered(this::handleDragEvent);
        startDirectoryComponentController.textFieldDirectory.setOnDragOver(this::handleDragEvent);
        startDirectoryComponentController.textFieldDirectory.setOnDragDropped(this::handleDragEvent);
        startDirectoryComponentController.textFieldDirectory.setOnDragExited(this::handleDragEvent);

        content1.setOnDragEntered(this::handleDragEvent);
        content1.setOnDragEntered(this::handleDragEvent);
        content1.setOnDragOver(this::handleDragEvent);
        content1.setOnDragDropped(this::handleDragEvent);
        content1.setOnDragExited(this::handleDragEvent);
    }

    private void handleDragEvent(DragEvent event) {
        if (DragEvent.DRAG_ENTERED.equals(event.getEventType()) && event.getGestureSource() == null) {
            draggingOver.set(true);
        } else if (DragEvent.DRAG_OVER.equals(event.getEventType()) && event.getGestureSource() == null && event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.ANY);
        } else if (DragEvent.DRAG_DROPPED.equals(event.getEventType()) && event.getGestureSource() == null && event.getDragboard().hasFiles()) {

            if(event.getDragboard().getFiles().size() == 1 && event.getDragboard().getFiles().iterator().next().isDirectory()){
               // Only update the input field, this will trigger a loading
               startDirectoryComponentController.textFieldDirectory.setText(event.getDragboard().getFiles().iterator().next().toString());
            } else {
                Set<Path> vaultPaths = event.getDragboard().getFiles().stream().map(File::toPath).collect(Collectors.toSet());
                loadedPaths.setAll(vaultPaths);
               Platform.runLater(this::updateInputView);
            }
            event.setDropCompleted(true);
            event.consume();
        } else if (DragEvent.DRAG_EXITED.equals(event.getEventType())) {
            draggingOver.set(false);
        }
    }

    private void configureStatusLabels() {
        statusLabelLoaded.textProperty().bind(entriesService.statusLoadedProperty());
        statusLabelLoadedFileTypes.textProperty().bind(entriesService.statusLoadedFileTypesProperty());
        statusLabelFilesWillRename.textProperty().bind((entriesService.statusWillRenameProperty()));
        statusLabelFilesWillRenameFileTypes.textProperty().bind(entriesService.statusWillRenameFileTypesProperty());
        statusLabelRenamed.textProperty().bind(entriesService.statusRenamedProperty());
        statusLabelRenamedFileTypes.textProperty().bind(entriesService.statusRenamedFileTypesProperty());
    }

    private void configureButtons() {
        goCancelButtonsComponentController.buttonGo.setTooltip(new Tooltip(resourceBundle.getString("mainview.button.go.tooltip")));
        goCancelButtonsComponentController.setButtonCancelActionEventFactory(MainViewButtonCancelEvent::new);
        goCancelButtonsComponentController.setButtonGoActionEventFactory(MainViewButtonGoEvent::new);
    }

    private void initRenamingStrategies() {
        comboBoxRenamingStrategy.getItems().add(new SimpleReplaceRenamingStrategy());
        comboBoxRenamingStrategy.getItems().add(new MediaMetadataRenamingStrategy());
        comboBoxRenamingStrategy.getItems().add(new RegexReplaceRenamingStrategy());
        comboBoxRenamingStrategy.getItems().add(new ToLowerCaseRenamingStrategy());
        comboBoxRenamingStrategy.getItems().add(new SpaceToCamelCaseRenamingStrategy());
        comboBoxRenamingStrategy.getItems().add(new UnhideStrategy());
        comboBoxRenamingStrategy.getItems().add(new ExtensionFromMimeStrategy());
        comboBoxRenamingStrategy.getItems().add(new CapitalizeFirstStrategy());
        comboBoxRenamingStrategy.getSelectionModel().selectFirst();
    }

    private void registerInputChangeListener() {
        replaceStringFromChangeListener = (e, o, n) -> Platform.runLater(this::updateOutputView);
        replaceStringToChangeListener = (e, o, n) -> Platform.runLater(this::updateOutputView);
        textFieldChangeListener = (e, o, n) -> Platform.runLater(() -> {loadedPaths.setAll(Path.of(n));
        Platform.runLater(this::updateInputView);});
        ignoreDirectoriesChangeListener = (e, o, n) -> Platform.runLater(this::updateOutputView);
        ignoreHiddenFilesChangeListener = (e, o, n) -> Platform.runLater(this::updateOutputView);
        textFieldReplacementStringFrom.textProperty().addListener(replaceStringFromChangeListener);
        textFieldReplacementStringTo.textProperty().addListener(replaceStringToChangeListener);
        startDirectoryComponentController.textFieldDirectory.textProperty().addListener(textFieldChangeListener);
        ignoreDirectories.selectedProperty().addListener(ignoreDirectoriesChangeListener);
        ignoreHiddenFiles.selectedProperty().addListener(ignoreHiddenFilesChangeListener);
        comboBoxRenamingStrategy.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            textFieldReplacementStringFrom.setDisable(!newValue.isReplacing());
            textFieldReplacementStringTo.setDisable(!newValue.isReplacing());
            updateOutputView();
        });
        goCancelButtonsComponentController.buttonGo.disableProperty().bind(renamingService.runningProperty().or(previewService.runningProperty()).or(loadPathsService.runningProperty()));
        goCancelButtonsComponentController.buttonCancel.disableProperty().bind(renamingService.runningProperty().not());

    }

    private void initServices() {
        setLoadingServiceCallbacks();
        setRenamingServiceCallbacks();
        setPreviewServiceCallbacks();
        registerStateChangeListeners();
    }

    private void registerStateChangeListeners() {
        loadPathsService.runningProperty().addListener((observable, oldValue, newValue) -> loadingFilesServiceStausChanged(newValue));
        previewService.runningProperty().addListener((observable, oldValue, newValue) -> previewFilesServiceStateChanged(newValue));
        fileTypeService.runningProperty().addListener((observable, oldValue, newValue) -> fileTypeServiceStateChanged(newValue));
        renamingService.runningProperty().addListener((observable, oldValue, newValue) -> renamingServiceStatusChange(newValue));
    }


    private void loadingFilesServiceStausChanged(Boolean newValue) {
        if (newValue) loadingFilesStatusLabel.setText(String.format(resourceBundle.getString(LOADING_FILES)));
        else loadingFilesStatusLabel.setText(null);
    }

    private void previewFilesServiceStateChanged(Boolean newValue) {
        if (newValue) previewFilesStatusLabel.setText(String.format(resourceBundle.getString(LOADING_PREVIEVS)));
        else previewFilesStatusLabel.setText(null);
    }

    private void fileTypeServiceStateChanged(Boolean newValue) {
        if (newValue) fileTypeStatusLabel.setText(String.format(resourceBundle.getString(LOADING_FILE_TYPES)));
        else fileTypeStatusLabel.setText(null);
    }

    private void renamingServiceStatusChange(Boolean newValue) {
        if (newValue) renameFilesStatusLabel.setText(String.format(resourceBundle.getString(RENAMING_FILES)));
        else renameFilesStatusLabel.setText(null);
    }


    private void setLoadingServiceCallbacks() {
        loadPathsService.setOnFailed(e -> {
            log.error(e.toString());
        });
    }

    private void setPreviewServiceCallbacks() {
        previewService.setOnFailed(e -> {
            log.error(e.toString());
        });
    }

    private void setRenamingServiceCallbacks() {
        renamingService.setOnFailed(e -> {
            log.error(e.toString());
        });
    }

    private void applyRandomColors() {
        Stream.of(layer01, layer02_3, comboboxBox, layer04_1, layer04_2, layer05_1, goCancelButtonsComponent, statusLabelLoaded, statusLabelLoadedFileTypes, statusLabelFilesWillRename, statusLabelFilesWillRename, statusBox).forEach(l -> l.setStyle("-fx-background-color: " + getRandomColorString()));
    }

    private String getRandomColorString() {
        return String.format("#%06x", new Random().nextInt(256 * 256 * 256));
    }

    public static List<Path> filesToPathList(Collection<File> files) {
        return files.stream().map(File::toPath).collect(Collectors.toList());
    }

    private void addToContent(final Collection<? extends RenamingEntry> renamingBeans) {
        renamingBeans.forEach(this::addToContent);
    }

    private void addToContent(final RenamingEntry renamingEntry) {
        var left = RenamingBeanControlBuilder.buildLeft(renamingEntry);
        var right = RenamingBeanControlBuilder.buildRight(renamingEntry);
        Platform.runLater(() -> content1.getItems().add(left));
        Platform.runLater(() -> content2.getItems().add(right));
    }

    public static void initAppMenu(MenuBar menuBar) {
        final String os = System.getProperty("os.name");
        if (os != null && os.startsWith("Mac")) {
            menuBar.useSystemMenuBarProperty().set(true);
        }
    }

    public void handleMenuItemDummyFileCreator(ActionEvent actionEvent) {

        DummyFileCreatorController controller = fxWeaver.loadController(DummyFileCreatorController.class, resourceBundle);
        controller.show();

    }

    public void handleMenuItemKodiTools(ActionEvent actionEvent) {
        KodiToolsController controller = fxWeaver.loadController(KodiToolsController.class);
        controller.show();
    }

    private void updateInputView() {
        log.debug("Updating input view with {} elements", loadedPaths.size());
        clearView();
        initLoadService();
        startService(loadPathsService);
    }

    private void initLoadService() {
        loadPathsService.cancel();
        loadPathsService.reset();
        loadPathsService.setFiles(loadedPaths/*.stream().filter(Files::exists).toList()*/);
        loadPathsService.setOnSucceeded((e) -> {
            initFileTypeService(entriesService.getEntries());
            startService(fileTypeService);
            updateOutputView();

        });
        progressBar.progressProperty().bind(loadPathsService.progressProperty());

    }

    private void updateOutputView() {
        log.debug("Updating output view");
        initPreviewService();
        startService(previewService);
    }

    private void initFileTypeService(Collection<RenamingEntry> renamingEntries) {
        fileTypeService.cancel();
        fileTypeService.reset();
        fileTypeService.setRenamingEntries(renamingEntries);
        var typeProvider = initAndGetFileTypeStrategy();
        fileTypeService.setFileTypeProvider(typeProvider);
    }

    private FileTypeProvider initAndGetFileTypeStrategy() {
        return new FileTypeByMimeProvider();
    }

    private void initPreviewService() {
        previewService.cancel();
        previewService.reset();
        previewService.setRenamingEntries(entriesService.getEntries());
        progressBar.progressProperty().bind(previewService.progressProperty());
        var strat = initAndGetStrategy();
        if (strat != null)
            previewService.setRenamingStrategy(strat);
    }


    /**
     * Starts a {@link Service}. Call in UI thread.
     *
     * @param service Service to start
     */
    private void startService(Service<?> service) {

        service.setOnRunning(e -> {
            if (log.isDebugEnabled()) {
                log.debug("Service running " + service);
            }
        });
        service.setOnFailed(e -> {
            if (log.isDebugEnabled()) {
                log.debug("Service {} failed with exception {}", service, service.getException());
            }
        });

        /* This is only *one* callback. configure this separately. */
		/*service.setOnSucceeded(e -> {
			if (log.isDebugEnabled()) {
				log.debug("Service succeeded {} ", service);
			}
		});*/
        log.debug("Starting service {}", service);
        service.start();


    }

    private void clearView() {
        entriesService.getEntries().clear();
        content1.getItems().clear();
        content2.getItems().clear();
        entriesService.reset();
    }

    private void cancelCurrentOperation() {
        log.debug("Cancelling current operation");
        previewService.cancel();
        renamingService.cancel();
        loadPathsService.cancel();
        fileTypeService.cancel();
        updateInputView();
    }

    @EventListener
    public void onButtonGoEvent(MainViewButtonGoEvent event) {
        handleButtonActionGo(event.getActionEvent());
    }

    @EventListener
    public void onButtonCancelEvent(MainViewButtonCancelEvent event) {
        handleButtonActionCancel(event.getActionEvent());
    }

    private RenamingStrategy initAndGetStrategy() {

        final RenamingStrategy strategy = comboBoxRenamingStrategy.getSelectionModel().getSelectedItem();
        if (strategy == null)
            return null;
        strategy.setReplacementStringFrom(textFieldReplacementStringFrom.getText());
        strategy.setReplacementStringTo(textFieldReplacementStringTo.getText());
        return strategy;
    }

    public void handleButtonActionGo(ActionEvent actionEvent) {

        final RenamingStrategy s = initAndGetStrategy();
        if (s != null) {
            entriesService.reset();
            renamingService.cancel();
            renamingService.reset();
            renamingService.setEvents(entriesService.getEntries());
            renamingService.setStrategy(s);
            renamingService.setOnSucceeded(e -> {

            });
            progressBar.progressProperty().bind(renamingService.progressProperty());
            renamingService.start();
        } else {
            log.info("No renaming strategy selected");
        }
    }

    private void handleButtonActionCancel(ActionEvent actionEvent) {
        cancelCurrentOperation();
    }

    public void handleMenuItemAbout(ActionEvent actionEvent) {
    }


}
