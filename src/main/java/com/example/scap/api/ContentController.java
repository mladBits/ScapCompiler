package com.example.scap.api;

import com.example.scap.model.ContentPackage;
import com.example.scap.service.ContentIngestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class ContentController {
    private final ContentIngestService contentIngestService;

    @PostMapping(value = "/packages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ContentPackage upload(@RequestPart("files") List<MultipartFile> files) {
        return contentIngestService.ingest(files);
    }
}
