package orgs.tuasl_clint.utils.BackendThreadManager;

import javafx.concurrent.Task;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Executor {
    private static final ExecutorService  executorService = Executors.newFixedThreadPool(1);

    public static synchronized ExecutorService getExecutor(){
        return executorService;
    }
    public static synchronized void  close(){
        executorService.close();
    }
    public  static synchronized Future<?> submit(@NotNull Task<?> t){
        return executorService.submit(t);
    }
    public static synchronized Future<?> submit(@NotNull Runnable r){
        return executorService.submit(r);
    }
    public static void execute(Runnable r){
        executorService.execute(r);
    }

}
