/*
 *     Dr.Rename - A Minimalistic Batch Renamer
 *
 *     Copyright (C) 2022
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package drrename.kodi;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CheckResult {

    protected final StringProperty result;

    protected final BooleanProperty warning;

    public CheckResult(String result, boolean warning) {
        this.result = new SimpleStringProperty(result);
        this.warning = new SimpleBooleanProperty(warning);
    }

    public CheckResult() {
        this.result = new SimpleStringProperty();
        this.warning = new SimpleBooleanProperty();
    }

    @Override
    public String toString() {
        return getResult();
    }

    // Getter / Setter //

    public String getResult() {
        return result.get();
    }

    public StringProperty resultProperty() {
        return result;
    }

    public void setResult(String result) {
        this.result.set(result);
    }

    public boolean isWarning() {
        return warning.get();
    }

    public BooleanProperty warningProperty() {
        return warning;
    }

    public void setWarning(boolean warning) {
        this.warning.set(warning);
    }


}