package com.localblox.ashfaq.filewatcher.action;

/**
 * Used for processing events.
 *
 * Event examples could be:
 *
 * <br/> - new file was completely loaded to in folder
 *
 * <br/> - new file was completely loaded to out folder
 */
public interface Action<T> {

    /**
     * Process event.
     * @param event - event to process
     */
    void doIt(T event);

}
