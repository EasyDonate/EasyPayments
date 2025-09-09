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

    private fun constructArgs(): MutableList<String> = mutableListOf<String>().apply {
        this += "--live"
        this += arrayOf("-i", inputFile.asFile.map { it.absolutePath }.get())
        this += arrayOf("-m", mappingsFile.asFile.map { it.absolutePath }.get())
        this += arrayOf("-o", outputFile.asFile.map { it.absolutePath }.get())
        this += if (reverseFlag.getOrElse(false)) arrayOf("--reverse") else arrayOf<String>()
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