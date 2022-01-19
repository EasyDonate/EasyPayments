package ru.easydonate.easypayments.cache;

import org.bukkit.plugin.Plugin;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.EventUpdateReports;

import java.io.*;
import java.util.LinkedHashSet;
import java.util.Set;

public final class ReportCache {

    private final Plugin plugin;
    private final File cacheFile;
    private final Set<EventUpdateReports> storage;

    public ReportCache(Plugin plugin) {
        this.plugin = plugin;
        this.cacheFile = new File(plugin.getDataFolder(), ".cached.reports");
        this.storage = new LinkedHashSet<>();
    }

    @SuppressWarnings("unchecked")
    public void loadReports() {
        storage.clear();

        if(!cacheFile.isFile())
            return;

        try {
            InputStream inputStream = new FileInputStream(cacheFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            Object deserializedObject = objectInputStream.readObject();
            objectInputStream.close();

            Set<EventUpdateReports> reportsSet = (Set<EventUpdateReports>) deserializedObject;
            if(reportsSet != null && !reportsSet.isEmpty())
                storage.addAll(reportsSet);

            if(!storage.isEmpty()) {
                long amount = storage.stream().mapToLong(EventUpdateReports::size).sum();
                plugin.getLogger().info(amount + " report(s) has been loaded from the local cache file.");
            }
        } catch (IOException | ClassNotFoundException | ClassCastException ignored) {}
    }

    public void addToCache(EventUpdateReports reports) {
        synchronized (this) {
            storage.add(reports);
            saveReports();
        }
    }

    public void clear() {
        synchronized (this) {
            storage.clear();
            deleteFile();
        }
    }

    public void unloadReports(EventUpdateReports reports) {
        synchronized (this) {
            storage.remove(reports);
            saveReports();
        }
    }

    private void saveReports() {
        deleteFile();

        if(storage.isEmpty())
            return;

        try {
            cacheFile.createNewFile();

            OutputStream outputStream = new FileOutputStream(cacheFile);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(storage);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (IOException ignored) {}
    }

    private void deleteFile() {
        cacheFile.delete();
    }

}
