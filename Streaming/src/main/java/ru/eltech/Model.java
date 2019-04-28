package ru.eltech;

import org.apache.spark.ml.PipelineModel;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;

public class Model {
    private PipelineModel model = null;
    private Watcher watcher = null;
    private String path;

    public Model(String path) throws IOException {
        watcher = new Watcher(Paths.get(path).getParent());
        this.path = path;
    }

    public PipelineModel getModel() {
        if (model == null || watcher.check(StandardWatchEventKinds.ENTRY_CREATE, Paths.get(path).getFileName().toString())) {
            model = PipelineModel.load(path);
            System.out.println("loaded model");
        }

        return model;
    }
}