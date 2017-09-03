package ua.adeptius.asterisk.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

public class AudioConverter {

    public static int convert(String inputFile, String outputFile, int duration) throws Exception{
        String command = "ffmpeg" +
                " -i " + inputFile +
                " -t "+duration +
                " -vn" +
                " -ab 32000" +
                " -ac 1" +
                " -ar 16000" +
                " -y " + outputFile;

        Process process = Runtime.getRuntime().exec(command);
        BufferedReader errorInput = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        List<String> collect = errorInput.lines()
                .filter(s -> !s.startsWith("invalid new backstep"))
                .collect(Collectors.toList());

        for (String s : collect) {
            if (s.contains("no such file or directory")){
                return -1;
            }
            if (s.contains("video:0kB audio:")){
                s = s.substring(16);
                s = s.substring(0, s.indexOf("kB"));
                return Integer.parseInt(s);
            }
            System.out.println(s);
        }
        return -1;
    }
}
