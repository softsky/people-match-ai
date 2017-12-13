package com.localblox.ashfaq.filewatcher;

import com.localblox.ashfaq.filewatcher.action.impl.NewFileInFolderAction;
import com.localblox.ashfaq.filewatcher.action.impl.NewFileOutFolderAction;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.DFSInotifyEventInputStream;
import org.apache.hadoop.hdfs.client.HdfsAdmin;
import org.apache.hadoop.hdfs.inotify.Event;
import org.apache.hadoop.hdfs.inotify.Event.RenameEvent;
import org.apache.hadoop.hdfs.inotify.EventBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * HDFS file watcher to react on file system events and process them.
 *
 * <br/> Inspired by https://stackoverflow.com/questions/29960186/hdfs-file-watcher
 *
 * <br/> Note that files in IN folder should match files in OUT folder by name. Each file should have unique name.
 *
 * <p/>
 * To prevent processing of partially uploaded file use inFilePattern and outFilePattern that must exclude uncomplete
 * file.
 *
 * <p/>
 * It was agreed that uncomplete file will have '.' (dot) before the name. Basic file handling flow should be as
 * follow:
 *
 * <br/> 1. Create file in folder IN or OUT prepended with dot. for example 'in/.aaaaaa-bbbb-1234-5678.csv'
 *
 * <br/> 2. Write content to the file 'in/.aaaaaa-bbbb-1234-5678.csv'
 *
 * <br/> 3. rename file 'in/.aaaaaa-bbbb-1234-5678.csv' to 'in/aaaaaa-bbbb-1234-5678.csv' that it will match with
 * pattern and will be processed.
 */
public class HdfsFileWatcher {

    private static final Logger log = LoggerFactory.getLogger(HdfsFileWatcher.class);

    private String hdfsAdminUri;

    // TODO check pattern for GIUD
    // default file name as GUID
    private String inFilePattern = "in/[a-fA-F0-9\\-].csv";

    // TODO check pattern for GIUD
    // default file name as GUID
    private String outFilePattern = "out/[a-fA-F0-9\\-].csv";

    /**
     * Creates HdfsFileWatcher instance.
     *
     * @param hdfsAdminUri - HDFS admin URI
     */
    public HdfsFileWatcher(final String hdfsAdminUri) {
        this.hdfsAdminUri = hdfsAdminUri;
    }

    /**
     * Creates HdfsFileWatcher instance.
     *
     * @param hdfsAdminUri   - HDFS admin URI
     * @param inFilePattern  - input file pattern for ready to process files
     * @param outFilePattern - output file pattern for ready to process file
     */
    public HdfsFileWatcher(final String hdfsAdminUri, final String inFilePattern, final String outFilePattern) {
        this.hdfsAdminUri = hdfsAdminUri;
        this.inFilePattern = inFilePattern;
        this.outFilePattern = outFilePattern;
    }

    // TODO check if processing will be in multiple thread. If so - use AtomicBoolean
    private boolean proceed;

    /**
     * Start the watcher to process file.
     *
     * This method will block until processing is stopped.
     */
    //TODO - think about execution context inside Spark and blocking. May be run in separate thread if need.
    //TODO - if there should be more than one file watcher - resolve multiple file processing issue (one file
    // processed twice).
    public void start() {

        proceed = true;

        DFSInotifyEventInputStream eventStream = getDfsInotifyEventInputStream();
        while (proceed) {
            EventBatch events = null;
            log.info("proceed event listening: {}", proceed);
            try {
                // TODO - move to configuration.
                events = eventStream.poll(5L, TimeUnit.SECONDS);

                // TODO this is draft logic, need to be reviewed according to requirements
                if (events != null) {
                    for (Event event : events.getEvents()) {
                        log.info("event type: {}", event.getEventType());
                        switch (event.getEventType()) {
                            case RENAME:
                                processRenameEvent((RenameEvent) event);
                                break;
                            default:
                                //TODO - process default behaviour
                                break;
                        }
                    }
                }

            } catch (Exception e) {
                // TODO - handle exception.
                log.error("error while events watching: {}", e.getMessage(), e);
            }
        }

        log.info("shut down...");

    }

    /**
     * Process rename file event.
     *
     * @param renameEvent rename event
     */
    void processRenameEvent(final RenameEvent renameEvent) {

        // TODO - we need to handle situation when there will be more that one file watcher.
        // And it should not take one file for processing more that once. So maube we need to rename file inprocess
        // (like locking)
        try {
            String dstFile = renameEvent.getDstPath();

            if (dstFile.matches(inFilePattern)) {
                new NewFileInFolderAction().doIt(dstFile);
            } else if (dstFile.matches(outFilePattern)) {
                new NewFileOutFolderAction().doIt(dstFile);
            } else {
                log.info("skip file rename: src = {} dst = {}", renameEvent.getSrcPath(),
                         renameEvent.getDstPath());
            }
        } catch (Exception e) {
            // TODO - handle file processing exception
        }

    }

    /**
     * Stop file event processing.
     *
     * Switches proceed flag to false. Actual stopping will be done on next cycle iteration.
     */
    public void stop() {
        proceed = false;
    }

    /**
     * Obtains {@link DFSInotifyEventInputStream} instance using hdfsAdminUri and empty {@link Configuration}.
     */
    DFSInotifyEventInputStream getDfsInotifyEventInputStream() {
        try {
            HdfsAdmin admin = new HdfsAdmin(URI.create(hdfsAdminUri), new Configuration());
            return admin.getInotifyEventStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
