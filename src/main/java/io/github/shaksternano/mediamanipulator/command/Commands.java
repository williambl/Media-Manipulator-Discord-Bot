package io.github.shaksternano.mediamanipulator.command;

/**
 * Contains a method to register all the {@link Command}s.
 */
public class Commands {

    /**
     * The caption {@link Command}.
     */
    public static final Command CAPTION = new CaptionCommand(
            "caption",
            "Captions a media file."
    );

    /**
     * The stretch {@link Command}.
     */
    public static final Command STRETCH = new StretchCommand(
            "stretch",
            "Stretches media. Optional parameters: [width stretch multiplier, default value is " + StretchCommand.DEFAULT_WIDTH_MULTIPLIER + "], [height stretch multiplier, default value is " + StretchCommand.DEFAULT_HEIGHT_MULTIPLIER + "]"
    );

    /**
     * The to-gif {@link Command}.
     */
    public static final Command TO_GIF = new ToGifCommand(
            "gif",
            "Turns media into a GIF."
    );

    /**
     * The shut-down {@link Command}.
     */
    public static final Command SHUT_DOWN = new ShutDownCommand(
            "shutdown",
            "Shuts down the bot. Only the owner of the bot can use this command."
    );

    /**
     * The help {@link Command}.
     */
    public static final Command HELP = new HelpCommand(
            "help",
            "Lists all commands."
    );

    /**
     * Registers all the {@link Command}s.
     */
    public static void registerCommands() {
        CommandRegistry.register(
                CAPTION,
                STRETCH,
                TO_GIF,
                SHUT_DOWN,
                HELP
        );
    }
}
