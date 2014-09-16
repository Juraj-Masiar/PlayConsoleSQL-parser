package sk.jm.consoleparser;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by Juraj on 7.9.2014.
 */
public class ConsoleMonitor {
    private Thread threadMonitor;
    private Thread threadPrint;
    private final Object lock = new Object();
    private Deque<String> consoleOut = new ArrayDeque<>();
    private boolean isPaused = false;
    private List<String> vmsListBefore = new ArrayList<>();
    private Process process = null;
    private TextProcessor textProcessor;

    public ConsoleMonitor(TextProcessor textProcessor) {
        this.textProcessor = textProcessor;
    }

    private int getUnixPID(Process process) throws Exception {
        System.out.println(process.getClass().getName());
        if (process.getClass().getName().equals("java.lang.UNIXProcess"))
        {
            Class cl = process.getClass();
            Field field = cl.getDeclaredField("pid");
            field.setAccessible(true);
            Object pidObject = field.get(process);
            return (Integer) pidObject;
        } else {
            throw new IllegalArgumentException("Needs to be a UNIXProcess");
        }
    }

    private int killUnixProcess(Process process) throws Exception
    {
        int pid = getUnixPID(process);
        return Runtime.getRuntime().exec("kill -SIGINT " + pid).waitFor();
    }

    private String killWindowsProcess() {
        List<String> listOfVMS = getListOfVMS();
        List<String> subtract = ListUtils.subtract(listOfVMS, vmsListBefore);
        Optional<String> linePID = subtract.stream().filter(x -> x.contains("activator")).findFirst();
        if (!linePID.isPresent()) {
            return "NOT FOUND, activator process is not running (yet?), processes started meanwhile:\n" + StringUtils.join(subtract, "\n") + "\n*END*";
        }
        String processPID = Utils.substring(linePID.get(), null, " ");

        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/C", "taskkill -f /pid " + processPID);
        try (BufferedReader r = new BufferedReader(new InputStreamReader(builder.start().getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Failded to stop process";
    }

    private List<String> getListOfVMS() {
        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/C", "jps");
        List<String> result = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(builder.start().getInputStream()))) {
            String line;
            while ((line = r.readLine()) != null) {
                result.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void stopMonitor() {
        String result = killWindowsProcess();
        textProcessor.appendText(result);
        if (false) {
            try {
                killUnixProcess(process);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//        System.out.println(process.isAlive());
//        process.destroy();
//        process.destroyForcibly();
//        System.out.println(process.isAlive());

//        OutputStream outputStream = process.getOutputStream();
//
//        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
//
//
//
//        try {
//            writer.write("run\r\n");
//            writer.flush();
//            writer.close();
//
////            outputStream.flush();
////            outputStream.write("run\r\n".getBytes(Charset.forName("ascii")));
////            outputStream.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


//        process.destroy();
//        if (process != null && process.isAlive()) {
//            process.destroy();
////            process.destroyForcibly();
//
//        }
    }

    public void startMonitor(String command, String dirPath, String ignoredList) {
        if (threadMonitor != null && threadMonitor.isAlive())
            return;
//        ProcessBuilder builder = new ProcessBuilder("activator");
        textProcessor.setIgnoredList(ignoredList);
        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/C", command);
//                    ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "act");
        builder.directory(new File(dirPath));
        builder.redirectErrorStream(true);
        vmsListBefore = getListOfVMS();
        try {
            process = builder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        threadMonitor = new Thread(runnableMonitor);
        threadMonitor.start();
        threadPrint = new Thread(runnablePrint);
        threadPrint.start();
    }

    public boolean pause() {
        return isPaused = !isPaused;
    }

    Runnable runnableMonitor = () -> {
        BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = null;
        while (true) {
            try {
                line = r.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (line == null) { break; }
            if (line.startsWith("2014-"))
                line =  line.substring(22);
            synchronized (lock) {
                consoleOut.offer(line);
            }
            textProcessor.processLine(line);
//            System.out.println(line);
        }
    };

    Runnable runnablePrint = () -> {
      while (true) {
          Utils.sleep(300);
          while (isPaused)
              Utils.sleep(200);
          ArrayList<String> lines;
          synchronized (lock) {
              lines = new ArrayList<>(consoleOut);
              consoleOut.clear();
          }
          textProcessor.appendText(lines, false);
      }
    };

    public boolean isRunning() {
        return process != null && process.isAlive();
    }
}
