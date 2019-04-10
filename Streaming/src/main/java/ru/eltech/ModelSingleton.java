package ru.eltech;

import org.apache.spark.ml.PipelineModel;

import java.nio.file.Paths;

public class ModelSingleton {
    private static volatile PipelineModel model;
    private static volatile Watcher watcher;

    private ModelSingleton() {
    }

    public static PipelineModel getModel(String path) {
        synchronized (ModelSingleton.class) {
            if (model == null) {
                watcher = WatcherSingleton.getWatcher(Paths.get(path));
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