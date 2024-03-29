/*
 * Copyright 2014 OpenMarket Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.skipjack.adoi.messaging.media;

import org.matrix.androidsdk.crypto.model.crypto.EncryptedFileInfo;

import java.io.Serializable;

public class SlidableMediaInfo implements Serializable {

    // Message.MSGTYPE_XXX
    public String id;
    public String mFileName;
    public String mMessageType;
    public String mMediaUrl;
    public String mThumbnailUrl;
    public String mMimeType;
    public EncryptedFileInfo mEncryptedFileInfo;
    public long fileSize;
    public long date;
    // exif infos
    public int mRotationAngle = 0;
    public int mOrientation = 0;

    // default constructor
    public SlidableMediaInfo() {
    }
}
