package mods.utils;

public abstract class FileProcessor {

  protected String  filename;
  protected String  dir;
  protected boolean debug;
    protected String outdir;

    public FileProcessor() {
    }

    protected abstract void readResource(String dir, String filename);

    public void processFile(String dir, String filename) {
    this.dir = dir;
    this.filename = filename;
    readResource(dir, filename);
  }
}
