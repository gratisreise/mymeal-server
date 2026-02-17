package com.mymealserver.common.test.fixtures;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

/**
 * Test fixture for MultipartFile
 * Provides reusable MultipartFile instances for testing file uploads
 */
public class MultipartFileFixture {

    /**
     * Creates a valid JPEG photo
     */
    public static MockMultipartFile createValidJpegPhoto() {
        return new MockMultipartFile(
                "photo",
                "meal.jpg",
                "image/jpeg",
                "fake-image-content".getBytes()
        );
    }

    /**
     * Creates a valid PNG photo
     */
    public static MockMultipartFile createValidPngPhoto() {
        return new MockMultipartFile(
                "photo",
                "meal.png",
                "image/png",
                "fake-image-content".getBytes()
        );
    }

    /**
     * Creates an empty photo file
     */
    public static MockMultipartFile createEmptyPhoto() {
        return new MockMultipartFile(
                "photo",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );
    }

    /**
     * Creates a text file (not an image)
     */
    public static MockMultipartFile createTextFile() {
        return new MockMultipartFile(
                "photo",
                "not-an-image.txt",
                "text/plain",
                "This is not an image".getBytes()
        );
    }

    /**
     * Creates a large photo file (simulating size limit test)
     */
    public static MockMultipartFile createLargePhoto() {
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
        return new MockMultipartFile(
                "photo",
                "large-meal.jpg",
                "image/jpeg",
                largeContent
        );
    }

    /**
     * Creates a photo with custom name and content
     */
    public static MockMultipartFile createCustomPhoto(String filename, String contentType, String content) {
        return new MockMultipartFile(
                "photo",
                filename,
                contentType,
                content.getBytes()
        );
    }
}
