package com.malt.places.loader;

import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class FileManager {


    public void checkFileExistOrDownloadIt(String tempDir, String fileName) throws IOException {
        String txtFile = tempDir + File.separator + fileName + ".txt";
        Path txt = Paths.get(txtFile);
        if (Files.notExists(txt)) {
            log.info("file {} do not exist. Downloading file....", fileName + ".txt");

            FileUtils.copyURLToFile(
                    new URL("http://download.geonames.org/export/dump/"+fileName+".txt"),
                    new File(txtFile));
        }
    }

    public void checkFileExistOrDownloadItAndUnzipIt(String tempDir, String fileName) throws IOException, ZipException {
        String txtFile = tempDir + File.separator + fileName + ".txt";
        String zipFile = tempDir + File.separator + fileName + ".zip";
        Path txt = Paths.get(txtFile);
        Path zip = Paths.get(zipFile);
        if (Files.notExists(zip) && Files.notExists(txt)) {
            log.info("file {} do not exist. Downloading zip file....", fileName + ".txt");

            FileUtils.copyURLToFile(
                    new URL("http://download.geonames.org/export/dump/"+fileName+".zip"),
                    new File(zipFile));
        }
        if (Files.exists(zip) && Files.notExists(txt)) {
            log.info("Extracting file to temp directory...");
            ZipFile archive = new ZipFile(zipFile);
            archive.extractAll(tempDir);
        }
    }
}
