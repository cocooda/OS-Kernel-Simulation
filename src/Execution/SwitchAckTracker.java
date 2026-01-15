package Execution;

import java.util.concurrent.atomic.AtomicReference;

public final class SwitchAckTracker {
    private static final AtomicReference<String> LAST_THREAD = new AtomicReference<>("");

    private SwitchAckTracker() {
    }

    public static void recordAndReack(Thread currentThread) {
        String currentThreadInfo = currentThread.currentThread().getName();
        String prev = LAST_THREAD.getAndSet(currentThreadInfo);
        if (!currentThreadInfo.equals(prev)) {
            System.out.println("[ReAck]: Current thread is " + currentThreadInfo);
        }
    }
}
