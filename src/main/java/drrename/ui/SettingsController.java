/*
 *     Dr.Rename - A Minimalistic Batch Renamer
 *
 *     Copyright (C) 2022
 *
 *     This file is part of Dr.Rename.
 *
 *     You can redistribute it and/or modify it under the terms of the GNU Affero
 *     General Public License as published by the Free Software Foundation, either
 *     version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful, but WITHOUT
 *     ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *     FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package drrename.ui;

import drrename.Settings;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@RequiredArgsConstructor
@Slf4j
@Component
@FxmlView("/fxml/SettingsView.fxml")
public class SettingsController implements Initializable {

    private static class UiThemeConverter extends StringConverter<UiTheme> {

        private final ResourceBundle resourceBundle;

        UiThemeConverter(ResourceBundle resourceBundle) {
            this.resourceBundle = resourceBundle;
        }

        @Override
        public String toString(UiTheme impl) {
            return resourceBundle.getString(impl.getDisplayName());
        }

        @Override
        public UiTheme fromString(String string) {
            throw new UnsupportedOperationException();
        }

    }

    private final Settings settings;

    public VBox root;

    public ChoiceBox<UiTheme> themeChoiceBox;

    private Stage mainStage;

   private final FxApplicationStyle applicationStyle;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mainStage = new Stage();
        mainStage.setScene(new Scene(root));
        mainStage.setTitle("Settings");

        themeChoiceBox.getItems().addAll(UiTheme.applicableValues());
        if (!themeChoiceBox.getItems().contains(settings.getTheme())) {
            settings.setTheme(UiTheme.LIGHT);
        }
        themeChoiceBox.valueProperty().bindBidirectional(settings.themeProperty());
        themeChoiceBox.setConverter(new UiThemeConverter(resourceBundle));

        System.out.println(applicationStyle);
    }

    public void show() {
        mainStage.show();
    }
}