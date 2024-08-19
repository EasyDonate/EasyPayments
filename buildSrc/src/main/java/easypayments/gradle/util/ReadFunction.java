package easypayments.gradle.util;

import java.io.IOException;
import java.io.InputStream;

@FunctionalInterface
public interface ReadFunction<T> {

    T read(InputStream dataStream) throws IOException;

}
