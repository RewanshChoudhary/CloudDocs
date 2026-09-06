package com.example.CloudDocs.service.video;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VideoServiceTest {

    private final VideoService videoService = new VideoService(null, null);

    @Test
    void buildsSanitizedRawKey() {
        UUID videoId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        String rawKey = videoService.buildRawKey(videoId, "../My Trip 2026!.mp4");

        assertThat(rawKey).isEqualTo("raw/11111111-1111-1111-1111-111111111111/My_Trip_2026_.mp4");
    }

    @Test
    void buildsProcessedAndThumbnailKeys() {
        UUID videoId = UUID.fromString("22222222-2222-2222-2222-222222222222");

        assertThat(videoService.buildProcessedKey(videoId, "720p"))
                .isEqualTo("processed/22222222-2222-2222-2222-222222222222/720p.mp4");
        assertThat(videoService.buildThumbnailKey(videoId))
                .isEqualTo("processed/22222222-2222-2222-2222-222222222222/thumbnail.jpg");
    }
}
