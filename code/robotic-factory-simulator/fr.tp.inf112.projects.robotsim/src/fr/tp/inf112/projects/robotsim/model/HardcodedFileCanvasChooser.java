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

    private final String fileExtension;
    private final String documentTypeLabel;
    private Component viewer;
    private String lastFilename;

    public HardcodedFileCanvasChooser(final String fileExtension,
                                      final String documentTypeLabel) {
        if (fileExtension == null) {
            throw new IllegalArgumentException("fileExtension cannot be null");
        }
        this.fileExtension     = fileExtension.startsWith(".") ? fileExtension.substring(1) : fileExtension;
        this.documentTypeLabel = documentTypeLabel == null ? "" : documentTypeLabel;
        this.lastFilename      = " ";
    }

    /* TODO: Have a way of retrieving the files from the server */
    @Override
    public String choseCanvas() throws IOException {
        // Search local persistence dir for the most recent file with the expected extension
        final File dir = new File("code/robotic-factory-simulator/robotsim.persistance");
        if (!dir.isDirectory()) {
            showError("Load failed", "Directory not found:\n" + dir.getPath());
            return lastFilename;
        }
        final String suffix = "." + fileExtension;
        File[] candidates = dir.listFiles((d, name) -> name.endsWith(suffix));
        if (candidates == null || candidates.length == 0) {
            showError("Load failed", "No *" + suffix + " files found in:\n" + dir.getPath());
            return lastFilename;
        }
        // Pick the newest by lastModified
        File newest = java.util.Arrays.stream(candidates)
                .max(java.util.Comparator.comparingLong(File::lastModified))
                .orElse(candidates[0]);
        lastFilename = newest.getName(); // keep only file name (e.g., Puck_Factory_*.factory)
        showInfo("Canvas selected", "Most recent canvas:\n" + lastFilename);
        return lastFilename;
    }

    @Override
    public String newCanvasId() throws IOException {
        final String safeLabel = documentTypeLabel.replaceAll("\\s+", "_");
        final String filename = safeLabel + "_" + System.currentTimeMillis() + "." + fileExtension;

        // Inform the user about the new canvas id created.
        showInfo("New canvas created with id:\n", filename);
        lastFilename = filename;
        return filename;
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