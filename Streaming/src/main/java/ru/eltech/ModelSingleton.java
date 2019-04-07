package ru.eltech;

import org.apache.spark.ml.PipelineModel;

import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;

public class ModelSingleton {
    private static volatile PipelineModel model;
    private static final Watcher watcher = WatcherFactory.getWatcher(Paths.get("models/"));

    private ModelSingleton() {
    }

    public static PipelineModel getModel(String path) {
        synchronized (ModelSingleton.class) {
            if (model == null) {
                PipelineModel tmp = PipelineModel.load(path);
                model = tmp;
            }
            if (watcher.check()) {
                model = PipelineModel.load(path);
            }
        }
        return model;
    }
}