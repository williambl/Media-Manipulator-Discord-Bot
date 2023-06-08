package io.github.shaksternano.borgar.command;

import com.google.common.collect.ListMultimap;
import io.github.shaksternano.borgar.exception.InvalidArgumentException;
import io.github.shaksternano.borgar.exception.MissingArgumentException;
import io.github.shaksternano.borgar.io.NamedFile;
import io.github.shaksternano.borgar.media.ImageUtil;
import io.github.shaksternano.borgar.media.MediaUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Resizes an image by a certain amount.
 */
public class ResizeCommand extends FileCommand {

    private final boolean RAW;

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public ResizeCommand(String name, String description, boolean raw) {
        super(name, description);
        RAW = raw;
    }

    /**
     * Resizes an image by the amount specified in the first argument.
     * Equivalent to stretching an image with the width and height multipliers set to the same amount.
     *
     * @param file           The media file to apply the operation to.
     * @param fileName
     * @param fileFormat     The file format of the media file.
     * @param arguments      The arguments of the command.
     * @param extraArguments A multimap mapping the additional parameter names to a list of the arguments.
     * @param event          The {@link MessageReceivedEvent} that triggered the command.
     * @param maxFileSize    The maximum file size of the output file.
     * @return The edited media file.
     * @throws IOException              If an error occurs while applying the operation.
     * @throws IllegalArgumentException If an argument is invalid.
     * @throws MissingArgumentException If the operation requires an argument but none was provided.
     */
    @Override
    protected NamedFile modifyFile(File file, String fileName, String fileFormat, List<String> arguments, ListMultimap<String, String> extraArguments, MessageReceivedEvent event, long maxFileSize) throws IOException {
        if (arguments.size() > 0) {
            try {
                float resizeMultiplier = Float.parseFloat(arguments.get(0));
                return new NamedFile(
                    MediaUtil.processMedia(
                        file,
                        fileFormat,
                        "resize",
                        image -> ImageUtil.resize(image, resizeMultiplier, RAW),
                        maxFileSize
                    ),
                    "resized",
                    fileFormat
                );
            } catch (NumberFormatException e) {
                throw new InvalidArgumentException("Scale multiplier \"" + arguments.get(0) + "\" is not a number!");
            }
        } else {
            throw new MissingArgumentException("Please specify a scale multiplier!");
        }
    }
}