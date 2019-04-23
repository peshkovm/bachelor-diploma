package ru.eltech;

import org.apache.spark.ml.PipelineModel;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Model {
    private PipelineModel model = null;
    private Watcher watcher = null;
    private String path;

    public Model(String path) throws IOException {
        watcher = new Watcher(Paths.get(path).getParent());
        this.path = path;
    }

    public PipelineModel getModel() {
        if (model == null || watcher.check(Paths.get(path).getFileName().toString())) {
            model = PipelineModel.load(path);
        }

        return model;
    }
}