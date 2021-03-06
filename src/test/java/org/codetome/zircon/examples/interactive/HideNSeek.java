package org.codetome.zircon.examples.interactive;

import org.codetome.zircon.api.Position;
import org.codetome.zircon.api.Size;
import org.codetome.zircon.api.Symbols;
import org.codetome.zircon.api.builder.LayerBuilder;
import org.codetome.zircon.api.builder.TerminalBuilder;
import org.codetome.zircon.api.builder.TextCharacterBuilder;
import org.codetome.zircon.api.color.TextColorFactory;
import org.codetome.zircon.api.font.Font;
import org.codetome.zircon.api.resource.CP437TilesetResource;
import org.codetome.zircon.api.shape.FilledRectangleFactory;
import org.codetome.zircon.api.color.TextColor;
import org.codetome.zircon.api.color.ANSITextColor;
import org.codetome.zircon.api.graphics.Layer;
import org.codetome.zircon.api.input.InputType;
import org.codetome.zircon.api.screen.Screen;
import org.codetome.zircon.api.terminal.Terminal;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class HideNSeek {

    private static final List<InputType> EXIT_CONDITIONS = new ArrayList<>();
    private static final int TERMINAL_WIDTH = 16;
    private static final int TERMINAL_HEIGHT = 16;
    private static final Size SIZE = Size.of(TERMINAL_WIDTH, TERMINAL_HEIGHT);
    private static final Font<BufferedImage> FONT = CP437TilesetResource.TAFFER_20X20.toFont();
    private static boolean headless = false;

    static {
        EXIT_CONDITIONS.add(InputType.Escape);
        EXIT_CONDITIONS.add(InputType.EOF);
    }

    @Test
    public void checkSetup() {
        main(new String[]{"test"});
    }

    public static void main(String[] args) {
        // for this example we only need a default terminal (no extra config)
        final Terminal terminal = TerminalBuilder.newBuilder()
                .font(FONT)
                .initialTerminalSize(SIZE)
                .buildTerminal(args.length > 0);
        if(args.length > 0) {
            headless = true;
        }
        final Screen screen = TerminalBuilder.createScreenFor(terminal);
        Size size = screen.getBoundableSize();
        screen.setCursorVisibility(false); // we don't want the cursor right now
        FilledRectangleFactory
                .buildFilledRectangle(Position.TOP_LEFT_CORNER, screen.getBoundableSize())
                .toTextImage(TextCharacterBuilder.newBuilder()
                        .backgroundColor(TextColorFactory.fromString("#665233"))
                        .character(' ')
                        .build())
                .drawOnto(screen, Position.TOP_LEFT_CORNER);

        final Layer player = new LayerBuilder()
                .filler(TextCharacterBuilder.newBuilder()
                        .character('@')
                        .backgroundColor(TextColorFactory.fromRGB(0, 0, 0, 0))
                        .foregroundColor(ANSITextColor.WHITE)
                        .build())
                .offset(Position.of(size.getColumns() / 2, size.getRows() / 2))
                .size(Size.ONE)
                .build();

        screen.pushLayer(player);
        screen.display();
        drawBuilding(screen, Position.of(5, 10));
        enableMovement(screen, player);
        screen.display();
    }

    private static void drawBuilding(Screen screen, Position position) {
        Layer building = new LayerBuilder()
                .filler(TextCharacterBuilder.DEFAULT_CHARACTER)
                .offset(position)
                .size(Size.of(4, 4))
                .build();

        TextColor windowColor = TextColorFactory.fromString("#808080");
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 5; x++) {
                building.setCharacterAt(Position.of(x, y).plus(position), TextCharacterBuilder.newBuilder()
                        .backgroundColor(windowColor)
                        .character(' ')
                        .build());
            }
        }

        drawCharAt(building, Position.of(0, 2).plus(position), Symbols.SINGLE_LINE_TOP_LEFT_CORNER);
        drawCharAt(building, Position.of(1, 2).plus(position), Symbols.SINGLE_LINE_T_DOWN);
        drawCharAt(building, Position.of(2, 2).plus(position), Symbols.SINGLE_LINE_T_DOWN);
        drawCharAt(building, Position.of(3, 2).plus(position), Symbols.SINGLE_LINE_TOP_RIGHT_CORNER);
        drawCharAt(building, Position.of(0, 3).plus(position), Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER);
        drawCharAt(building, Position.of(1, 3).plus(position), Symbols.SINGLE_LINE_T_UP);
        drawCharAt(building, Position.of(2, 3).plus(position), Symbols.SINGLE_LINE_T_UP);
        drawCharAt(building, Position.of(3, 3).plus(position), Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER);
        screen.pushLayer(building);
    }

    private static void drawCharAt(Layer building, Position position, char c) {
        TextColor wallColor = TextColorFactory.fromString("#333333");
        TextColor windowColor = TextColorFactory.fromString("#808080");
        building.setCharacterAt(position, TextCharacterBuilder.newBuilder()
                .backgroundColor(wallColor)
                .foregroundColor(windowColor)
                .character(c)
                .build());
    }

    private static void enableMovement(Screen screen, Layer player) {
        screen.onInput((input) -> {
            if (EXIT_CONDITIONS.contains(input.getInputType()) && !headless) {
                System.exit(0);
            } else {
                if (InputType.ArrowUp == input.getInputType()) {
                    player.moveTo(player.getPosition().withRelativeRow(-1));
                }
                if (InputType.ArrowDown == input.getInputType()) {
                    player.moveTo(player.getPosition().withRelativeRow(1));
                }
                if (InputType.ArrowLeft == input.getInputType()) {
                    player.moveTo(player.getPosition().withRelativeColumn(-1));
                }
                if (InputType.ArrowRight == input.getInputType()) {
                    player.moveTo(player.getPosition().withRelativeColumn(1));
                }
                screen.display();
            }
        });
    }
}
