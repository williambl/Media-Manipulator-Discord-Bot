package io.github.shaksternano.mediamanipulator.command;

import io.github.shaksternano.mediamanipulator.mediamanipulator.MediaManipulator;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;

public class SpeedCommand extends MediaCommand {

    public static final float DEFAULT_SPEED_MULTIPLIER = 2;

    /**
     * Creates a new command object.
     *
     * @param name        The name of the command. When a user sends a message starting with {@link Command#COMMAND_PREFIX}
     *                    followed by this name, the command will be executed.
     * @param description The description of the command. This is displayed in the help command.
     */
    public SpeedCommand(String name, String description) {
        super(name, description);
    }

    @Override
    public File applyOperation(File media, String[] arguments, MediaManipulator manipulator, MessageReceivedEvent event) throws IOException {
        float speedMultiplier = DEFAULT_SPEED_MULTIPLIER;

        if (arguments.length > 0) {
            try {
                speedMultiplier = Float.parseFloat(arguments[0]);
            } catch (NumberFormatException e) {
                event.getMessage().reply("Speed multiplier \"" + arguments[0] + "\" is not a number. Using default value of " + speedMultiplier + ".").queue();
            }
        }

        return manipulator.speed(media, speedMultiplier);
    }
}