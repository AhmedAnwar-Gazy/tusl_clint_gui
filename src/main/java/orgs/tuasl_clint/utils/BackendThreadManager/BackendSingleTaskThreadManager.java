package orgs.tuasl_clint.utils.BackendThreadManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

public class BackendSingleTaskThreadManager {
    private static final ConcurrentMap<String, BackendSingleTaskThreadManager.ManagedTask> tasks = new ConcurrentHashMap<>();

    public static synchronized void add(Runnable task, String taskName) {
        if (tasks.containsKey(taskName)) {
            throw new IllegalArgumentException("Task name already exists: " + taskName);
        }
        BackendSingleTaskThreadManager.ManagedTask managedTask = new BackendSingleTaskThreadManager.ManagedTask(task, taskName);
        tasks.put(taskName, managedTask);
    }
    public static boolean startTask(String name){
        if(tasks.containsKey(name)){
            tasks.get(name).start();
            return true;
        }
        return false;
    }

    public static synchronized boolean addCompletionListener(String taskName, Consumer<String> onComplete) {
        BackendSingleTaskThreadManager.ManagedTask task = tasks.get(taskName);
        if (task != null) {
            task.setOnComplete(onComplete);
            return true;
        }
        return false;
    }


    public static synchronized boolean addFailureListener(String taskName, Consumer<Exception> onFailure) {
        BackendSingleTaskThreadManager.ManagedTask task = tasks.get(taskName);
        if (task != null) {
            task.setOnFailure(onFailure);
            return true;
        }
        return false;
    }

    public static synchronized boolean clearListeners(String taskName) {
        BackendSingleTaskThreadManager.ManagedTask task = tasks.get(taskName);
        if (task != null) {
            task.clearListeners();
            return true;
        }
        return false;
    }

    public static synchronized Thread getTask(String taskName) {
        BackendSingleTaskThreadManager.ManagedTask task = tasks.get(taskName);
        return (task != null) ? task.getThread() : null;
    }

    public static synchronized boolean hasTaskName(String name) {
        return tasks.containsKey(name);
    }

    private static class ManagedTask {
        private final Runnable task;
        private final String name;
        private final Thread thread;

        private Consumer<String> onComplete;
        private Consumer<Exception> onFailure;

        public ManagedTask(Runnable task, String name) {
            this.task = new BackendSingleTaskThreadManager.ManagedTask.TaskWrapper(task);
            this.name = name;
            this.thread = new Thread(this.task, name);
        }

        public void setOnComplete(Consumer<String> listener) {
            this.onComplete = listener;
        }

        public void setOnFailure(Consumer<Exception> listener) {
            this.onFailure = listener;
        }

        public void clearListeners() {
            this.onComplete = null;
            this.onFailure = null;
        }

        public Thread getThread() {
            return thread;
        }

        public void start() {
            thread.start();
        }

        private class TaskWrapper implements Runnable {
            private final Runnable originalTask;

            public TaskWrapper(Runnable originalTask) {
                this.originalTask = originalTask;
            }

            @Override
            public void run() {
                try {
                    originalTask.run();
                    notifyTaskCompleted();
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

        private void notifyTaskFailed(Exception e) {
            if (onFailure != null) {
                onFailure.accept(e);
            }
        }
    }
}
