package lee.study.down.task;

import java.util.concurrent.TimeUnit;
import lee.study.down.constant.HttpDownStatus;
import lee.study.down.content.ContentManager;
import lee.study.down.model.ChunkInfo;
import lee.study.down.model.TaskInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpDownProgressEventTask extends Thread {

  private final static Logger LOGGER = LoggerFactory.getLogger(HttpDownProgressEventTask.class);

  @Override
  public void run() {

    while (true) {
      try {
        for (TaskInfo taskInfo : ContentManager.DOWN.getStartTasks()) {
          if (taskInfo.getStatus() != HttpDownStatus.DONE
              && taskInfo.getStatus() != HttpDownStatus.FAIL
              && taskInfo.getStatus() != HttpDownStatus.PAUSE
              && taskInfo.getStatus() != HttpDownStatus.MERGE
              && taskInfo.getStatus() != HttpDownStatus.MERGE_CANCEL) {
            taskInfo.setLastTime(System.currentTimeMillis());
            for (ChunkInfo chunkInfo : taskInfo.getChunkInfoList()) {
              if (chunkInfo.getStatus() != HttpDownStatus.DONE
                  && chunkInfo.getStatus() != HttpDownStatus.PAUSE) {
                chunkInfo.setLastTime(System.currentTimeMillis());
              }
            }
            //保存任务进度记录
            synchronized (taskInfo) {
              if (taskInfo.getStatus() != HttpDownStatus.DONE) {
                ContentManager.DOWN.saveTask(taskInfo.getId());
              }
            }
          }
        }
        ContentManager.WS.sendMsg(ContentManager.DOWN.buildDowningWsForm());
        TimeUnit.MILLISECONDS.sleep(1000);
      } catch (Exception e) {
        LOGGER.error("eventTask:", e);
      }
    }
  }
}
