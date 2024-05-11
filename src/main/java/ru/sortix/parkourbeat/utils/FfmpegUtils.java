package ru.sortix.parkourbeat.utils;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

@UtilityClass
public class FfmpegUtils {
    private final boolean REDIRECT_OUTPUT = false;

    public void splitAllTacksWithPieces(@NonNull File ffmpeg, @NonNull File allTracksDir) {
        File[] files = allTracksDir.listFiles();
        if (files == null) {
            throw new IllegalArgumentException("Unable to read dir content");
        }

        System.out.println("Started splitting all tracks (" + files.length + " files)");
        int filesComplete = 0;
        for (File dir : files) {
            File input = new File(dir, "track.ogg");
            if (splitTrackWithPieces(ffmpeg, input, dir, 1, 2)) {
                System.out.println("Complete " + dir.getName() + " (" + ++filesComplete + "/" + files.length + ")");
            } else {
                System.out.println("Failed to complete " + dir.getName() + " (" + ++filesComplete + "/" + files.length + ")");
            }
        }
        System.out.println("Finished splitting " + filesComplete + "/" + files.length + " tracks");
    }

    public boolean splitTrackWithPieces(@NonNull File ffmpeg,
                                        @NonNull File input, @NonNull File outputDir,
                                        int periodSeconds, int durationSeconds
    ) {
        long totalDurationSeconds = FfmpegUtils.getDurationSeconds(ffmpeg, input);

        int fileNumber = 0;
        int startSeconds = 0;
        while (startSeconds < totalDurationSeconds) {
            File output = new File(outputDir, ++fileNumber + ".ogg");
            boolean success = FfmpegUtils.trimTrack(
                ffmpeg,
                input, output,
                Duration.ofSeconds(startSeconds), Duration.ofSeconds(startSeconds + durationSeconds)
            );
            startSeconds += periodSeconds;
            if (!success) return false;
        }
        return true;
    }

    public boolean trimTrack(@NonNull File ffmpeg,
                             @NonNull File input, @NonNull File output,
                             @NonNull Duration startAt, @NonNull Duration endAt
    ) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            if (REDIRECT_OUTPUT) {
                processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
            }
            processBuilder.command(
                ffmpeg.getAbsolutePath(),
                "-i", "\"" + input.getAbsolutePath() + "\"",
                "-ss", format(startAt),
                "-to", format(endAt),
                "-c", "copy", "\"" + output.getAbsolutePath() + "\""
            );
            Process process = processBuilder.start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public long getDurationSeconds(@NonNull File ffmpeg, @NonNull File input) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            if (REDIRECT_OUTPUT) {
                processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
            }
            processBuilder.command(
                ffmpeg.getAbsolutePath(),
                "-i", "\"" + input.getAbsolutePath() + "\""
            );

            Process process = processBuilder.start();
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;

            Long durationSeconds = null;
            while ((line = in.readLine()) != null) {
                int splitter = line.indexOf("Duration: ");
                if (splitter < 0) continue;
                line = line.substring(splitter + "Duration: ".length());
                line = line.substring(0, line.indexOf(','));
                String[] args = line.split("[:.]");
                int hours = Integer.parseInt(args[0]);
                int minutes = Integer.parseInt(args[1]);
                int seconds = Integer.parseInt(args[2]);
                int mills = Integer.parseInt(args[3]);
                durationSeconds = hours * 3600L + minutes * 60L + seconds + (mills == 0 ? 0 : 1);
                break;
            }
            process.waitFor();

            if (durationSeconds == null) {
                throw new IllegalArgumentException("Duration line not found not found");
            }
            return durationSeconds;
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to find duration of track " + input.getAbsolutePath(), e);
        }
    }

    @NonNull
    private final DateTimeFormatter DURATION_FORMAT = new DateTimeFormatterBuilder()
        .appendValue(ChronoField.HOUR_OF_AMPM).appendLiteral(":")
        .appendValue(ChronoField.MINUTE_OF_HOUR).appendLiteral(":")
        .appendValue(ChronoField.SECOND_OF_MINUTE)
        .toFormatter();

    @NonNull
    private String format(@NonNull Duration duration) {
        return DURATION_FORMAT.format(LocalTime.MIDNIGHT.plus(duration));
    }
}
