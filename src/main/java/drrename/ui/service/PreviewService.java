package drrename.ui.service;

import drrename.model.RenamingEntry;
import javafx.concurrent.Task;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Executor;

@Service
public class PreviewService extends StrategyService<List<RenamingEntry>> {

    private final ApplicationEventPublisher applicationEventPublisher;

    public PreviewService(final Executor taskExecutor, ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
        setExecutor(taskExecutor);
    }

    @Override
    protected Task<List<RenamingEntry>> createTask() {

        return new PreviewTask(getRenamingEntries(), getRenamingStrategy(), applicationEventPublisher);
    }
}
