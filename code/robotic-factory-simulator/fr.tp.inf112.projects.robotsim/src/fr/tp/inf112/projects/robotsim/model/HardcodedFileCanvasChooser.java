package fr.tp.inf112.projects.robotsim.model;

import java.awt.Component;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import javax.swing.JOptionPane;

import fr.tp.inf112.projects.canvas.model.CanvasChooser;

/**
 * Non-UI CanvasChooser that saves/loads from a fixed directory and does not show system files.
 */
public class HardcodedFileCanvasChooser implements CanvasChooser {

    private final File baseDir;
    private final String fileExtension;
    private final String documentTypeLabel;
    private Component viewer;

    public HardcodedFileCanvasChooser(final String baseDirectory,
                                      final String fileExtension,
                                      final String documentTypeLabel) {
        if (baseDirectory == null || fileExtension == null) {
            throw new IllegalArgumentException("baseDirectory and fileExtension cannot be null");
        }
        this.baseDir = new File(baseDirectory);
        this.baseDir.mkdirs();
        this.fileExtension = fileExtension.startsWith(".") ? fileExtension.substring(1) : fileExtension;
        this.documentTypeLabel = documentTypeLabel == null ? "" : documentTypeLabel;
    }

    @Override
    public String choseCanvas() throws IOException {
        final File[] matches = baseDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return name.endsWith("." + fileExtension);
            }
        });
        if (matches == null || matches.length == 0) {
            return null;
        }
        // return the first match (no UI shown). Change strategy if you need different behavior.
        final String path = matches[0].getPath();
        // Inform the user that a canvas was chosen (this indicates the path to load).
        showInfo("Canvas selected", "Canvas will be loaded from:\n" + path);
        return path;
    }

    @Override
    public String newCanvasId() throws IOException {
        final String safeLabel = documentTypeLabel.replaceAll("\\s+", "_");
        final String filename = safeLabel + "_" + System.currentTimeMillis() + "." + fileExtension;
        final File f = new File(baseDir, filename);
        // ensure parent dir exists (should already)
        final File parent = f.getParentFile();
        if (parent != null) parent.mkdirs();
        final String path = f.getPath();
        // Inform the user about the new canvas path (note: this only creates a path; saving must still occur).
        showInfo("New canvas created", "New canvas path:\n" + path);
        return path;
    }

    /**
     * Call this from your save routine to confirm result to the user.
     * @param success true if save succeeded
     * @param path the path that was saved to
     */
    public void confirmSaved(final boolean success, final String path) {
        if (success) {
            showInfo("Save successful", "Canvas saved to:\n" + path);
        } else {
            showError("Save failed", "Failed to save canvas to:\n" + path);
        }
    }

    /**
     * Call this from your load routine to confirm result to the user.
     * @param success true if load succeeded
     * @param path the path that was loaded from
     */
    public void confirmLoaded(final boolean success, final String path) {
        if (success) {
            showInfo("Load successful", "Canvas loaded from:\n" + path);
        } else {
            showError("Load failed", "Failed to load canvas from:\n" + path);
        }
    }

    private void showInfo(final String title, final String message) {
        JOptionPane.showMessageDialog(viewer, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(final String title, final String message) {
        JOptionPane.showMessageDialog(viewer, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public Component getViewer() {
        return viewer;
    }

    public void setViewer(final Component viewer) {
        this.viewer = viewer;
    }
}