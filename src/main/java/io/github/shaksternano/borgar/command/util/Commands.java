package io.github.shaksternano.borgar.command.util;

import io.github.shaksternano.borgar.command.*;
import io.github.shaksternano.borgar.media.template.ResourceTemplate;
import net.dv8tion.jda.api.entities.Message;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains registered {@link Command}s.
 */
@SuppressWarnings("unused")
public class Commands {

    private static final List<Command<?>> commandsToRegister = new ArrayList<>();
    private static final DecimalFormat FORMAT = new DecimalFormat("0.####");

    /**
     * The caption {@link Command}.
     */
    public static final Command<?> CAPTION = addCommandToRegister(new CaptionCommand(
        "caption",
        "Captions a media file. Optional arguments: [Caption text]",
        false
    ));

    public static final Command<?> CAPTION_2 = addCommandToRegister(new CaptionCommand(
        "caption2",
        "Captions a media file. Optional arguments: [Caption text]",
        true
    ));

    public static final Command<?> DEMOTIVATE = addCommandToRegister(new DemotivateCommand(
        "demotiv",
        "Puts image in demotivate meme. Optional arguments: [Meme text. To specify sub text, add \"-sub\" before the text.]"
    ));

    public static final Command<?> MEME = addCommandToRegister(new MemeCommand(
        "meme",
        "Adds impact font text to a media file. Required arguments: [The text to be drawn. By default, the text is drawn at the top. To specify text drawn at the bottom, add \"-bottom\" before the text.]"
    ));

    public static final Command<?> SONIC_SAYS = addCommandToRegister(new TemplateCommand(
        "sonic",
        "Sonic says. Optional arguments: [What sonic says]",
        ResourceTemplate.SONIC_SAYS
    ));

    public static final Command<?> SOYJAK_POINTING = addCommandToRegister(new TemplateCommand(
        "soy",
        "Soyjak pointing. Optional arguments: [What is being pointed at]",
        ResourceTemplate.SOYJAK_POINTING
    ));

    public static final Command<?> MUTA_SOY = addCommandToRegister(new TemplateCommand(
        "mutasoy",
        "Mutahar soyjak pointing. Optional arguments: [What is being pointed at]",
        ResourceTemplate.MUTA_SOY
    ));

    public static final Command<?> WALMART_WANTED = addCommandToRegister(new TemplateCommand(
        "wanted",
        "Walmart wanted. Optional arguments: [What is wanted]",
        ResourceTemplate.WALMART_WANTED
    ));

    public static final Command<?> OH_MY_GOODNESS_GRACIOUS = addCommandToRegister(new TemplateCommand(
        "omgg",
        "Oh my goodness gracious. Optional arguments: [What is wanted]",
        ResourceTemplate.OH_MY_GOODNESS_GRACIOUS
    ));

    public static final Command<?> THINKING_BUBBLE = addCommandToRegister(new TemplateCommand(
        "think",
        "Puts text or an image in a thinking bubble. Optional arguments: [Thinking bubble text]",
        ResourceTemplate.THINKING_BUBBLE
    ));

    public static final Command<?> LIVING_IN_1984 = addCommandToRegister(new TemplateCommand(
        "1984",
        "Puts image into the 'living in 1984' meme. Optional arguments: [Speech bubble text]",
        ResourceTemplate.LIVING_IN_1984
    ));

    public static final Command<?> WHO_DID_THIS = addCommandToRegister(new TemplateCommand(
        "wdt",
        "Puts image into the 'who did this' meme. Optional arguments: [meme text]",
        ResourceTemplate.WHO_DID_THIS
    ));

    public static final Command<?> LIVE_REACTION = addCommandToRegister(LiveReactionCommand.INSTANCE);

    public static final Command<?> CREATE_TEMPLATE = addCommandToRegister(CreateTemplateCommand.INSTANCE);

    public static final Command<?> DELETE_TEMPLATE = addCommandToRegister(DeleteTemplateCommand.INSTANCE);

    public static final Command<?> SUBWAY_SURFERS = addCommandToRegister(SubwaySurfersCommand.INSTANCE);

    public static final Command<?> SPIN = addCommandToRegister(new SpinCommand(
        "spin",
        "Spins a media file. Optional arguments: [Spin speed, default value is " + FORMAT.format(SpinCommand.DEFAULT_SPIN_SPEED) + "], [Background hex RGB colour, by default it is transparent]"
    ));

    public static final Command<?> UNCAPTION_COLOR = addCommandToRegister(new UncaptionCommand(
        "uncaption",
        "Uncaptions media that has color in the caption, for example captions that contain emojis. Won't word on images that are surrounded by white.",
        true
    ));

    public static final Command<?> UNCAPTION_GREYSCALE = addCommandToRegister(new UncaptionCommand(
        "uncaption2",
        "Uncaptions media that doesn't have color in the caption, for example captions that don't contain emojis.",
        false
    ));

    /**
     * The stretch {@link Command}.
     */
    public static final Command<?> STRETCH = addCommandToRegister(new StretchCommand(
        "stretch",
        "Stretches media with extra processing to smoothen the resulting image. Optional arguments: [Width stretch multiplier, default value is " + FORMAT.format(StretchCommand.DEFAULT_WIDTH_MULTIPLIER) + "], [Height stretch multiplier, default value is " + FORMAT.format(StretchCommand.DEFAULT_HEIGHT_MULTIPLIER) + "]",
        false
    ));

    public static final Command<?> STRETCH_RAW = addCommandToRegister(new StretchCommand(
        "stretchraw",
        "Stretches media without extra processing. Optional arguments: [Width stretch multiplier, default value is " + FORMAT.format(StretchCommand.DEFAULT_WIDTH_MULTIPLIER) + "], [Height stretch multiplier, default value is " + FORMAT.format(StretchCommand.DEFAULT_HEIGHT_MULTIPLIER) + "]",
        true
    ));

    public static final Command<?> RESIZE = addCommandToRegister(new ResizeCommand(
        "resize",
        "Resizes media with extra processing to smoothen the resulting image. Equivalent to " + STRETCH.nameWithPrefix() + " x x. Required arguments: [Resize multiplier]",
        false
    ));

    public static final Command<?> RESIZE_RAW = addCommandToRegister(new ResizeCommand(
        "resizeraw",
        "Resizes media without extra processing. Equivalent to " + STRETCH_RAW.nameWithPrefix() + " x x. Required arguments: [Resize multiplier]",
        true
    ));

    public static final Command<?> CROP = addCommandToRegister(new CropCommand(
        "crop",
        "Crops media. Required arguments: [Crop ratio. To which side, add \"-top\", \"-right\", \"-bottom\" or \"-left\" before the crop ratio to specify which side to crop.]"
    ));

    public static final Command<?> AUTO_CROP = addCommandToRegister(new AutoCropCommand(
        "autocrop",
        "Automatically crops out background color. Optional arguments: [Background crop hex RGB colour, by default it is the color of the top left pixel], [Background crop colour tolerance, default value is " + FORMAT.format(AutoCropCommand.DEFAULT_COLOR_TOLERANCE) + "]"
    ));

    public static final Command<?> FLIP = addCommandToRegister(new FlipCommand(
        "flip",
        "Flips media horizontally. Use -" + FlipCommand.VERTICAL_FLAG + " to flip vertically."
    ));

    public static final Command<?> SPEED = addCommandToRegister(new SpeedCommand(
        "speed",
        "Speeds up or slows down animated media. Optional arguments: [Speed multiplier, default value is " + FORMAT.format(SpeedCommand.DEFAULT_SPEED_MULTIPLIER) + "]"
    ));

    public static final Command<?> REVERSE = addCommandToRegister(new ReverseCommand(
        "reverse",
        "Reverses animate media."
    ));

    public static final Command<?> PIXELATE = addCommandToRegister(new PixelateCommand(
        "pixel",
        "Pixelates media. Equivalent to " + RESIZE_RAW.nameWithPrefix() + " 1/x followed by " + Command.PREFIX + RESIZE_RAW.name() + " x Optional arguments: [Pixelation multiplier, default value is " + FORMAT.format(PixelateCommand.DEFAULT_PIXELATION_MULTIPLIER) + "]"
    ));

    public static final Command<?> REDUCE_FPS = addCommandToRegister(new ReduceFpsCommand(
        "redfps",
        "Reduces the FPS of a media file. Optional arguments: [FPS reduction multiplier, default value is " + FORMAT.format(ReduceFpsCommand.DEFAULT_FPS_REDUCTION_MULTIPLIER) + "]"
    ));

    /**
     * The speech bubble {@link Command}.
     */
    public static final Command<?> SPEECH_BUBBLE = addCommandToRegister(new SpeechBubbleCommand(
        "sb",
        "Overlays a speech bubble over media. Use -" + SpeechBubbleCommand.FLIP_FLAG + " to flip the speech bubble.",
        false
    ));

    public static final Command<?> INVERTED_SPEECH_BUBBLE = addCommandToRegister(new SpeechBubbleCommand(
        "sbi",
        "Cuts out a speech bubble from media (Inverted speech bubble). Use -" + SpeechBubbleCommand.FLIP_FLAG + " to flip the speech bubble. Use -" + SpeechBubbleCommand.OPAQUE_FLAG + " to make the speech bubble opaque.",
        true
    ));

    public static final Command<?> ROTATE = addCommandToRegister(new RotateCommand(
        "rotate",
        "Rotates media. Optional arguments: [Rotation amount, default value is " + FORMAT.format(RotateCommand.DEFAULT_ROTATION) + "], [Background hex RGB colour, by default it is transparent]."
    ));

    /**
     * Turns media into a GIF file.
     */
    public static final Command<?> TO_GIF = addCommandToRegister(new TranscodeCommand("gif"));

    public static final Command<?> TO_GIF_2 = addCommandToRegister(new ChangeExtensionCommand("gif"));

    public static final Command<?> TO_PNG = addCommandToRegister(new TranscodeCommand("png"));

    public static final Command<?> TO_JPG = addCommandToRegister(new TranscodeCommand("jpg"));

    public static final Command<?> TO_ICO = addCommandToRegister(new TranscodeCommand("ico"));

    public static final Command<?> TO_MP4 = addCommandToRegister(new TranscodeCommand("mp4"));

    public static final Command<?> LOOP = addCommandToRegister(GifLoopCommand.INSTANCE);

    public static final Command<?> SERVER_ICON = addCommandToRegister(new ServerIconCommand(
        "servericon",
        "Gets the icon of the server."
    ));

    public static final Command<?> SERVER_BANNER = addCommandToRegister(new ServerBannerCommand(
        "serverbanner",
        "Gets the image of the server banner."
    ));

    public static final Command<?> SERVER_SPLASH = addCommandToRegister(new ServerSplashCommand(
        "serversplash",
        "Gets the image of the server invite background."
    ));

    public static final Command<?> USER_AVATAR = addCommandToRegister(new UserAvatarCommand(
        "avatar",
        "Gets the avatar of a user. Optional arguments: [User mention]"
    ));

    public static final Command<?> USER_BANNER = addCommandToRegister(new UserBannerCommand(
        "banner",
        "Gets the banner of a user. Optional arguments: [User mention]"
    ));

    public static final Command<?> EMOJI_IMAGE = addCommandToRegister(new EmojiImageCommand(
        "emoji",
        "Gets the image of an emoji."
    ));

    public static final Command<?> STICKER_IMAGE = addCommandToRegister(new StickerImageCommand(
        "sticker",
        "Gets the image of a sticker."
    ));

    public static final Command<?> HAEMA = addCommandToRegister(new HaemaCommand(
        "haema",
        "Haema"
    ));

    public static final Command<?> TULIN = addCommandToRegister(TulinCommand.INSTANCE);

    public static final Command<?> SERVER_COUNT = addCommandToRegister(new ServerCountCommand(
        "servers",
        "Gets the number of servers that this bot is in."
    ));

    public static final Command<?> FAVOURITE_FILE = addCommandToRegister(new AddFavouriteCommand(
        "fav",
        "Creates an alias GIF for a file, which when sent, will be replaced by that file."
    ));

    public static final Command<?> TENOR_URL = addCommandToRegister(new TenorUrlCommand(
        "tenor",
        "Gets the direct file URL of Tenor media. Optional arguments: [media type, default value is `" + TenorUrlCommand.DEFAULT_MEDIA_TYPE + "`]"
    ));

    public static final Command<?> DOWNLOAD = addCommandToRegister(new DownloadCommand(
        "dl",
        "Downloads a file from a social media website, for example, a video from YouTube. Use -" + DownloadCommand.AUDIO_ONLY_FLAG + " to download audio only."
    ));

    public static final Command<?> CAT = addCommandToRegister(new CatCommand(
        "cat",
        "Sends a random cat image. Use -" + ApiFilesCommand.COUNT_FLAG + " to specify the number of cats.",
        null
    ));

    public static final Command<?> CATS = addCommandToRegister(new CatCommand(
        "cats",
        "Sends a few random cat images.",
        5
    ));

    public static final Command<?> CAT_BOMB = addCommandToRegister(new CatCommand(
        "catbomb",
        "Sends a bunch of random cat images.",
        Message.MAX_FILE_AMOUNT
    ));

    public static final Command<?> PONY = addCommandToRegister(new PonyCommand(
        "pony",
        "Sends a random pony image. Optional arguments: [Comma separated image tags]. Use -" + ApiFilesCommand.COUNT_FLAG + " to specify the number of ponies.",
        null
    ));

    public static final Command<?> PONIES = addCommandToRegister(new PonyCommand(
        "ponies",
        "Sends a few random pony images. Optional arguments: [Comma separated image tags].",
        5
    ));

    public static final Command<?> PONY_BOMB = addCommandToRegister(new PonyCommand(
        "ponybomb",
        "Sends a bunch of random pony images. Optional arguments: [Comma separated image tags].",
        Message.MAX_FILE_AMOUNT
    ));

    public static final Command<?> PING = addCommandToRegister(PingCommand.INSTANCE);

    public static final Command<?> MEMORY_USAGE = addCommandToRegister(new MemoryUsageCommand(
        "memory",
        "Get the memory usage of the bot."
    ));

    public static final Command<?> GARBAGE_COLLECTOR = addCommandToRegister(new GarbageCollectorCommand(
        "gc",
        "Runs the garbage collector."
    ));

    public static final Command<?> BENCHMARK = addCommandToRegister(new BenchmarkCommand(
            "benchmark",
            "Tests performance of the bot."
    ));

    /**
     * The shut-down {@link Command}.
     */
    public static final Command<?> SHUT_DOWN = addCommandToRegister(new ShutDownCommand(
        "shutdown",
        "Shuts down the bot."
    ));

    /**
     * The help {@link Command}.
     */
    public static final Command<?> HELP = addCommandToRegister(new HelpCommand(
        "help",
        "Lists all commands."
    ));

    private static <T extends Command<?>> T addCommandToRegister(T command) {
        commandsToRegister.add(command);
        return command;
    }

    /**
     * Registers all the {@link Command}s.
     */
    public static void registerCommands() {
        CommandRegistry.register(commandsToRegister);
    }
}
