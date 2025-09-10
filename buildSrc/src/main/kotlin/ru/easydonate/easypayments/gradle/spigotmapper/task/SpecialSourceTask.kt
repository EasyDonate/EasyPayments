package ru.easydonate.easypayments.gradle.spigotmapper.task

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.OutputStream

abstract class SpecialSourceTask : JavaExec() {

    init {
        mainClass.set("net.md_5.specialsource.SpecialSource")
        errorOutput = OutputStream.nullOutputStream()
        standardOutput = OutputStream.nullOutputStream()
    }

    override fun exec() {
        args = constructArgs()
        super.exec()
    }

    private fun constructArgs(): List<String> = buildList {
        add("--live")

        addAll(arrayOf("-i", inputFile.get().asFile.absolutePath))
        addAll(arrayOf("-m", mappingsFile.get().asFile.absolutePath))
        addAll(arrayOf("-o", outputFile.get().asFile.absolutePath))

        if (reverseFlag.getOrElse(false)) {
            add("--reverse")
        }
    }

    @get:InputFile
    abstract val inputFile: RegularFileProperty

    @get:InputFile
    abstract val mappingsFile: RegularFileProperty

    @get:Input @get:Optional
    abstract val reverseFlag: Property<Boolean>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

}