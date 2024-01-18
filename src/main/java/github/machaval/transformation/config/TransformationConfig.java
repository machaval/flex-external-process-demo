package github.machaval.transformation.config;

public class TransformationConfig {

    private String path;
    private String script;

    public TransformationConfig(String path, String script) {
        this.path = path;
        this.script = script;
    }

    public TransformationConfig() {

    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }
}
