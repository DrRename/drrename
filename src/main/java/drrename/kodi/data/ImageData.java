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

package drrename.kodi.data;

public class ImageData extends Qualified<byte[]> {

    public static ImageData from(byte[] data) {
        Qualified.Type type = Qualified.Type.INVALID;
        if(data == null){
            type = Qualified.Type.NOT_FOUND;
        } else {
            type = Type.OK;
        }
        return new ImageData(data, type);
    }

    public ImageData(byte[] element, Type type) {
        super(element, type);
    }
}