package com.buy01.media.infrastructure.web.dto;

import com.buy01.media.domain.model.FileStatus;

public class MediaResponse {

    private final String imagesId;
    private final FileStatus status;

    public MediaResponse(String imagesId, FileStatus status) {
        this.imagesId = imagesId;
        this.status = status;
    }

    public String getImagesId() {
        return imagesId;
    }

    public FileStatus getStatus() {
        return status;
    }

    public String imagesId() {
        return imagesId;
    }

    public FileStatus status() {
        return status;
    }
}
