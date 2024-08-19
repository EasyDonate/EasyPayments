package easypayments.gradle.task;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class SpecialSourceTask extends JavaExec {

    public SpecialSourceTask() {
        getMainClass().set("net.md_5.specialsource.SpecialSource");
        setErrorOutput(OutputStream.nullOutputStream());
        setStandardOutput(OutputStream.nullOutputStream());
    }

    @TaskAction
    @Override
    public void exec() {
        List<String> args = new ArrayList<>();
        args.add("--live");

        args.add("-i");
        args.add(getInputJar().get().getAsFile().getAbsolutePath());

        args.add("-o");
        args.add(getOutputJar().get().getAsFile().getAbsolutePath());

        args.add("-m");
        args.add(getMappingsPath().get().getAsFile().getAbsolutePath());

        if (getReverseFlag().getOrElse(false))
            args.add("--reverse");

        setArgs(args);
        super.exec();
    }

    @InputFile
    public abstract RegularFileProperty getInputJar();

    @OutputFile
    public abstract RegularFileProperty getOutputJar();

    @InputFile
    public abstract RegularFileProperty getMappingsPath();

    @Input @Optional
    public abstract Property<Boolean> getReverseFlag();

}
