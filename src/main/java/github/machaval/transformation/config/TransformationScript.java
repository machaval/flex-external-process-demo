package github.machaval.transformation.config;

import org.mule.weave.v2.runtime.DataWeaveScript;

public class TransformationScript {
    private DataWeaveScript script;
    private String path;

    public TransformationScript(String path, DataWeaveScript script) {
        this.script = script;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public DataWeaveScript getScript() {
        return script;
    }
}
