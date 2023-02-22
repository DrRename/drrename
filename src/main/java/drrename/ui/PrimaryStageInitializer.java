package drrename.ui;

import drrename.event.StageReadyEvent;
import drrename.ui.config.UiConfig;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.ResourceBundle;

@RequiredArgsConstructor
@Slf4j
@Component
public class PrimaryStageInitializer {

    private final FxWeaver fxWeaver;

    private final UiConfig uiConfig;

    private final ResourceBundle resourceBundle;

    private Scene mainScene;

    @EventListener
    public void onApplicationEvent(StageReadyEvent event) {
        Platform.runLater(() -> {
            Stage stage = event.stage();
            mainScene = new Scene(fxWeaver.loadView(TabController.class, resourceBundle), uiConfig.getInitialWidth(), uiConfig.getInitialHeight());
            mainScene.getStylesheets().add("/css/general.css");
            stage.setTitle(uiConfig.getAppTitle());
            stage.setScene(mainScene);
            stage.getIcons().setAll(SystemUtils.IS_OS_MAC ? List.of() : List.of(
                    new Image(PrimaryStageInitializer.class.getResource("/img/drrename_16.png").toString()),
                    new Image(PrimaryStageInitializer.class.getResource("/img/drrename_20.png").toString()),
                    new Image(PrimaryStageInitializer.class.getResource("/img/drrename_24.png").toString()),
                    new Image(PrimaryStageInitializer.class.getResource("/img/drrename_32.png").toString()),
                    new Image(PrimaryStageInitializer.class.getResource("/img/drrename_40.png").toString()),
                    new Image(PrimaryStageInitializer.class.getResource("/img/drrename_48.png").toString()),
                    new Image(PrimaryStageInitializer.class.getResource("/img/drrename_64.png").toString()),
                    new Image(PrimaryStageInitializer.class.getResource("/img/drrename_128.png").toString()),
                    new Image(PrimaryStageInitializer.class.getResource("/img/drrename_256.png").toString()),
                    new Image(PrimaryStageInitializer.class.getResource("/img/drrename_512.png").toString())));
            stage.show();
        });
    }

    public Scene getMainScene() {
        return mainScene;
    }
}
