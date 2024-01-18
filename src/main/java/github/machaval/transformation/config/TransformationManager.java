package github.machaval.transformation.config;

import org.mule.weave.v2.runtime.DataWeaveScriptingEngine;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransformationManager {

    static Logger LOG = Logger.getLogger(TransformationManager.class.getSimpleName());
    private List<TransformationScript> configList;

    public TransformationManager(List<TransformationScript> configList) {
        this.configList = configList;
    }

    public Optional<TransformationScript> lookupScript(String path) {
        return configList.stream().filter((sc) ->
                sc.getPath().equalsIgnoreCase(path)
        ).findFirst();
    }


    public static TransformationManager loadFrom(String path) {
        LOG.log(Level.INFO, "Loading from transformations " + path);
        final DataWeaveScriptingEngine dataWeaveScriptingEngine = new DataWeaveScriptingEngine();
        List<TransformationScript> scripts = new ArrayList<>();
        final File configDirectory = new File(path);
        File[] list = configDirectory.listFiles((dir, name) -> name.endsWith(".yaml"));
        if (list != null) {
            final Yaml yaml = new Yaml(new Constructor(TransformationConfig.class, new LoaderOptions()));

            Arrays.stream(list).forEach((f) -> {
                LOG.log(Level.INFO, "Configuration found: " + f);
                try (FileInputStream configFileStream = new FileInputStream(f)) {
                    for (Object configObject : yaml.loadAll(configFileStream)) {
                        final TransformationConfig tc = (TransformationConfig) configObject;
                        final TransformationScript transformationScript = new TransformationScript(tc.getPath(), dataWeaveScriptingEngine.compile(tc.getScript()));
                        scripts.add(transformationScript);
                    }
                } catch (IOException io) {
                    throw new RuntimeException(io);
                }
            });


        }

        return new TransformationManager(scripts);

    }
}
