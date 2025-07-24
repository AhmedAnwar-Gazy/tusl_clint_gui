package orgs.tuasl_clint.utils.BackendThreadManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public  class BackendThreadManager{
    private static final ConcurrentMap<String, ManagedTask> tasks = new ConcurrentHashMap<>();

    public static synchronized void add(Runnable task, String taskName) {
        if (tasks.containsKey(taskName)) {
            throw new IllegalArgumentException("Task name already exists: " + taskName);
        }
        ManagedTask managedTask = new ManagedTask(task, taskName);
        tasks.put(taskName, managedTask);
        managedTask.start();
    }

    public static synchronized boolean addCompletionListener(String taskName, Consumer<String> onComplete) {
        ManagedTask task = tasks.get(taskName);
        if (task != null) {
            task.setOnComplete(onComplete);
            return true;
        }
        return false;
    }

    public static synchronized boolean addStopListener(String taskName, Consumer<String> onStop) {
        ManagedTask task = tasks.get(taskName);
        if (task != null) {
            task.setOnStop(onStop);
            return true;
        }
        return false;
    }

    public static synchronized boolean addFailureListener(String taskName, Consumer<Exception> onFailure) {
        ManagedTask task = tasks.get(taskName);
        if (task != null) {
            task.setOnFailure(onFailure);
            return true;
        }
        return false;
    }

    public static synchronized boolean clearListeners(String taskName) {
        ManagedTask task = tasks.get(taskName);
        if (task != null) {
            task.clearListeners();
            return true;
        }
        return false;
    }

    public static synchronized Thread getTask(String taskName) {
        ManagedTask task = tasks.get(taskName);
        return (task != null) ? task.getThread() : null;
    }

    public static synchronized boolean stopTask(String taskName, String reason) {
        ManagedTask task = tasks.get(taskName);
        if (task != null) {
            task.stop(reason);
            return true;
        }
        return false;
    }
    public static synchronized boolean hasTaskName(String name){
        return tasks.containsKey(name);
    }

    private static class ManagedTask {
        private final Runnable task;
        private final String name;
        private final Thread thread;
        private final AtomicBoolean paused = new AtomicBoolean(false);
        private final AtomicBoolean running = new AtomicBoolean(true);

        private Consumer<String> onComplete;
        private Consumer<String> onStop;
        private Consumer<Exception> onFailure;

        public ManagedTask(Runnable task, String name) {
            this.task = new TaskWrapper(task);
            this.name = name;
            this.thread = new Thread(this.task, name);
        }

        public void setOnComplete(Consumer<String> listener) {
            this.onComplete = listener;
        }

        public void setOnStop(Consumer<String> listener) {
            this.onStop = listener;
        }

        public void setOnFailure(Consumer<Exception> listener) {
            this.onFailure = listener;
        }

        public void clearListeners() {
            this.onComplete = null;
            this.onStop = null;
            this.onFailure = null;
        }

        public Thread getThread() {
            return thread;
        }

        public void start() {
            thread.start();
        }

        public boolean pause() {
            return paused.compareAndSet(false, true);
        }

        public boolean resume() {
            return paused.compareAndSet(true, false);
        }

        public void stop(String reason) {
            running.set(false);
            thread.interrupt();
            notifyTaskStopped(reason);
            tasks.remove(name);
        }
        private class TaskWrapper implements Runnable {
            private final Runnable originalTask;

            public TaskWrapper(Runnable originalTask) {
                this.originalTask = originalTask;
            }

            @Override
            public void run() {
                try {
                    while (running.get()) {
                        if (paused.get()) {
                            Thread.sleep(100);
                            continue;
                        }
                        originalTask.run();
                    }
                    notifyTaskCompleted();
                } catch (InterruptedException e) {
                    notifyTaskFailed(e);
                } catch (Exception e) {
                    notifyTaskFailed(e);
                } finally {
                    tasks.remove(name);
                }
            }
        }

        private void notifyTaskCompleted() {
            if (onComplete != null) {
                onComplete.accept(name);
            }
        }

        private void notifyTaskStopped(String reason) {
            if (onStop != null) {
                onStop.accept(reason);
            }
        }

        private void notifyTaskFailed(Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
        }
    }
}