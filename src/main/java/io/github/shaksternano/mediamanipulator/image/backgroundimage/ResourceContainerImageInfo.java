package io.github.shaksternano.mediamanipulator.image.backgroundimage;

import io.github.shaksternano.mediamanipulator.Main;
import io.github.shaksternano.mediamanipulator.graphics.Position;
import io.github.shaksternano.mediamanipulator.graphics.drawable.Drawable;
import io.github.shaksternano.mediamanipulator.image.imagemedia.ImageMedia;
import io.github.shaksternano.mediamanipulator.image.util.ImageUtil;
import io.github.shaksternano.mediamanipulator.io.FileUtil;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

public enum ResourceContainerImageInfo implements ContainerImageInfo {

    SONIC_SAYS(
            "image/containerimage/sonic_says.jpg",
            "sonic_says",
            345,
            35,
            630,
            490,
            60,
            Position.CENTRE,
            true,
            null,
            "Bitstream Vera Sans",
            Color.WHITE,
            100,
            null
    ),

    SOYJAK_POINTING(
            "image/containerimage/soyjak_pointing.png",
            "soyjak_pointing",
            0,
            0,
            1024,
            810,
            0,
            Position.CENTRE,
            250,
            150,
            500,
            300,
            0,
            Position.CENTRE,
            false,
            null,
            "Futura-CondensedExtraBold",
            Color.BLACK,
            100,
            null
    );

    private final String IMAGE_PATH_FROM_ROOT_PACKAGE;
    private final String RESULT_NAME;
    private final int IMAGE_CONTENT_X;
    private final int IMAGE_CONTENT_Y;
    private final int IMAGE_CONTENT_WIDTH;
    private final int IMAGE_CONTENT_HEIGHT;
    private final Position IMAGE_CONTENT_POSITION;
    private final int TEXT_CONTENT_X;
    private final int TEXT_CONTENT_Y;
    private final int TEXT_CONTENT_WIDTH;
    private final int TEXT_CONTENT_HEIGHT;
    private final Position TEXT_CONTENT_POSITION;
    private final boolean IS_BACKGROUND;
    @Nullable
    private final Color FILL;
    private final Font FONT;
    private final Color TEXT_COLOR;
    @Nullable
    private final Function<String, Drawable> CUSTOM_TEXT_DRAWABLE_FACTORY;

    ResourceContainerImageInfo(
            String imagePathFromRootPackage,
            String resultName,
            int imageContainerX,
            int imageContainerY,
            int imageContainerWidth,
            int imageContainerHeight,
            int imageContainerPadding,
            Position imageContentPosition,
            int textContainerX,
            int textContainerY,
            int textContainerWidth,
            int textContainerHeight,
            int textContainerPadding,
            Position textContentPosition,
            boolean isBackground,
            @Nullable Color fill,
            String fontName,
            Color textColor,
            int maxFontSize,
            @Nullable Function<String, Drawable> customTextDrawableFactory
    ) {
        IMAGE_PATH_FROM_ROOT_PACKAGE = imagePathFromRootPackage;
        RESULT_NAME = resultName;
        IMAGE_CONTENT_X = imageContainerX + imageContainerPadding;
        IMAGE_CONTENT_Y = imageContainerY + imageContainerPadding;
        TEXT_CONTENT_X = textContainerX + textContainerPadding;
        TEXT_CONTENT_Y = textContainerY + textContainerPadding;
        int doubleImagePadding = imageContainerPadding * 2;
        int doubleTextPadding = textContainerPadding * 2;
        IMAGE_CONTENT_WIDTH = imageContainerWidth - doubleImagePadding;
        IMAGE_CONTENT_HEIGHT = imageContainerHeight - doubleImagePadding;
        IMAGE_CONTENT_POSITION = imageContentPosition;
        TEXT_CONTENT_WIDTH = textContainerWidth - doubleTextPadding;
        TEXT_CONTENT_HEIGHT = textContainerHeight - doubleTextPadding;
        TEXT_CONTENT_POSITION = textContentPosition;
        IS_BACKGROUND = isBackground;
        FILL = fill;
        FONT = new Font(fontName, Font.PLAIN, maxFontSize);
        TEXT_COLOR = textColor;
        CUSTOM_TEXT_DRAWABLE_FACTORY = customTextDrawableFactory;
    }

    ResourceContainerImageInfo(
            String imagePathFromRootPackage,
            String resultName,
            int contentContainerX,
            int contentContainerY,
            int contentContainerWidth,
            int contentContainerHeight,
            int contentContainerPadding,
            Position contentPosition,
            boolean isBackground,
            @Nullable Color fill,
            String fontName,
            Color textColor,
            int maxFontSize,
            @Nullable Function<String, Drawable> customTextDrawableFactory
    ) {
        this(
                imagePathFromRootPackage,
                resultName,
                contentContainerX,
                contentContainerY,
                contentContainerWidth,
                contentContainerHeight,
                contentContainerPadding,
                contentPosition,
                contentContainerX,
                contentContainerY,
                contentContainerWidth,
                contentContainerHeight,
                contentContainerPadding,
                contentPosition,
                isBackground,
                fill,
                fontName,
                textColor,
                maxFontSize,
                customTextDrawableFactory
        );
    }

    @Override
    public ImageMedia getImage() throws IOException {
        return ImageUtil.getImageResourceInRootPackage(IMAGE_PATH_FROM_ROOT_PACKAGE);
    }

    @Override
    public String getResultName() {
        return RESULT_NAME;
    }

    @Override
    public int getImageContentX() {
        return IMAGE_CONTENT_X;
    }

    @Override
    public int getImageContentY() {
        return IMAGE_CONTENT_Y;
    }

    @Override
    public int getImageContentWidth() {
        return IMAGE_CONTENT_WIDTH;
    }

    @Override
    public int getImageContentHeight() {
        return IMAGE_CONTENT_HEIGHT;
    }

    @Override
    public Position getImageContentPosition() {
        return IMAGE_CONTENT_POSITION;
    }

    @Override
    public int getTextContentX() {
        return TEXT_CONTENT_X;
    }

    @Override
    public int getTextContentY() {
        return TEXT_CONTENT_Y;
    }

    @Override
    public int getTextContentWidth() {
        return TEXT_CONTENT_WIDTH;
    }

    @Override
    public int getTextContentHeight() {
        return TEXT_CONTENT_HEIGHT;
    }

    @Override
    public Position getTextContentPosition() {
        return TEXT_CONTENT_POSITION;
    }

    @Override
    public boolean isBackground() {
        return IS_BACKGROUND;
    }

    @Override
    public Optional<Color> getFill() {
        return Optional.ofNullable(FILL);
    }

    @Override
    public Font getFont() {
        return FONT;
    }

    @Override
    public Color getTextColor() {
        return TEXT_COLOR;
    }

    @Override
    public Optional<Function<String, Drawable>> getCustomTextDrawableFactory() {
        return Optional.ofNullable(CUSTOM_TEXT_DRAWABLE_FACTORY);
    }

    public static void validateFilePaths() {
        for (ResourceContainerImageInfo backgroundImage : ResourceContainerImageInfo.values()) {
            try {
                FileUtil.validateResourcePathInRootPackage(backgroundImage.IMAGE_PATH_FROM_ROOT_PACKAGE);
            } catch (Throwable t) {
                Main.getLogger().error("Error with " + backgroundImage + "'s file path " + backgroundImage.IMAGE_PATH_FROM_ROOT_PACKAGE, t);
                Main.shutdown(1);
            }
        }
    }
}
