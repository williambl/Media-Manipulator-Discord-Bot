package io.github.shaksternano.borgar.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.borgar.command.util.CommandParser;
import io.github.shaksternano.borgar.command.util.CommandResponse;
import io.github.shaksternano.borgar.command.util.Commands;
import io.github.shaksternano.borgar.io.FileUtil;
import io.github.shaksternano.borgar.io.NamedFile;
import io.github.shaksternano.borgar.media.MediaUtil;
import io.github.shaksternano.borgar.util.DiscordUtil;
import io.github.shaksternano.borgar.util.MessageUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.checkerframework.common.returnsreceiver.qual.This;

import javax.annotation.meta.When;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class BenchmarkCommand extends BaseCommand<Void> {
    private static final int RUNS = 5;

    public BenchmarkCommand(String name, String description) {
        super(name, description);
    }

    @Override
    public CompletableFuture<CommandResponse<Void>> execute(List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event) {
        return MessageUtil.downloadFile(event.getMessage())
                .thenApply(fileOpt -> fileOpt.<CommandResponse<Void>>map(file -> {
                    int runs = CommandParser.parseIntegerArgument(
                            arguments,
                            0,
                            RUNS,
                            null,
                            event.getChannel(),
                            (argument, defaultValue) -> "Runs value \"" + argument + "\" is not a whole number, choosing default value [10]."
                    );
                    var maxFileSize = DiscordUtil.getMaxUploadSize(event);
                    var fileFormat = FileUtil.getFileFormat(file.file());

                    try {
                        long[] millisPerAutocrop = new long[runs];
                        for (int i = 0; i < runs; i++) {
                            long startTime = System.currentTimeMillis();
                            ((FileCommand) Commands.AUTO_CROP).modifyFile(file.file(), file.name(), fileFormat, arguments, extraArguments, event, maxFileSize);
                            long endTime = System.currentTimeMillis();
                            millisPerAutocrop[i] = endTime - startTime;
                        }

                        long[] millisPerCaption = new long[runs];
                        for (int i = 0; i < runs; i++) {
                            long startTime = System.currentTimeMillis();
                            ((FileCommand) Commands.CAPTION).modifyFile(file.file(), file.name(), fileFormat, List.of("Test Caption"), extraArguments, event, maxFileSize);
                            long endTime = System.currentTimeMillis();
                            millisPerCaption[i] = endTime - startTime;
                        }

                        return new CommandResponse<>("""
                                        ```
                                        avg. time per autocrop: %d ms
                                                        [range: %d ms]
                                        avg. time per caption:  %d ms
                                                        [range: %d ms]
                                        ```
                                        """.formatted(
                                (long) Arrays.stream(millisPerAutocrop).average().orElse(0.0),
                                Arrays.stream(millisPerAutocrop).max().orElse(Long.MAX_VALUE) - Arrays.stream(millisPerAutocrop).min().orElse(0),
                                (long) Arrays.stream(millisPerCaption).average().orElse(0.0),
                                Arrays.stream(millisPerCaption).max().orElse(Long.MAX_VALUE) - Arrays.stream(millisPerCaption).min().orElse(0)));
                    } catch (IOException e) {
                        return new CommandResponse<>("Error running benchmark: %s".formatted(e));
                    }
                }).orElseGet(() -> new CommandResponse<>("No file given!")));
    }
}
