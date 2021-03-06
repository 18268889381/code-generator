package cn.iba8.module.generator.common.jsontojava.file;

import cn.iba8.module.generator.common.jsontojava.exception.JsonToJavaException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonFileReader implements FileReader{

    @Override
    public String readJsonFromFile(String file) {
        try {
            return String.join(System.lineSeparator(), Files.readAllLines(Paths.get(file), Charset.forName(StandardCharsets.UTF_8.name())));
        } catch (IOException ioException) {
            throw new JsonToJavaException("IO error while accessing file", ioException);
        } catch (RuntimeException runtimeException) {
            throw new JsonToJavaException("Unexpected error while accessing file", runtimeException);
        }
    }
}
