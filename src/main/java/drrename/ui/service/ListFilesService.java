package drrename.ui.service;

import drrename.model.RenamingEntry;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@org.springframework.stereotype.Service
public class ListFilesService extends Service<List<RenamingEntry>> {

    private Collection<Path> files;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    protected Task<List<RenamingEntry>> createTask(){
        // If 'files' is one entry only, and it's a directory, use ListDirectoryTask, otherwise use ListFilesTask.
        if(files != null && files.size() == 1 && Files.isDirectory(files.iterator().next())){
            return new ListDirectoryTask(files.iterator().next(), eventPublisher);
        }
        return new ListFilesTask(files, eventPublisher);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " file cnt: " + (files == null ? 0 : files.size());
    }

    // Getter / Setter //

    public Collection<Path> getFiles() {
        return files;
    }

    public void setFiles(final Collection<Path> files) {
        this.files = files;
    }

    @Override
    public void reset() {
        super.reset();
        this.files = null;
    }
}

