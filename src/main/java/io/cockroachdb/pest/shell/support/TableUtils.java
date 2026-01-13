package io.cockroachdb.pest.shell.support;


import org.springframework.shell.jline.tui.table.BorderStyle;
import org.springframework.shell.jline.tui.table.TableBuilder;
import org.springframework.shell.jline.tui.table.TableModel;

public abstract class TableUtils {
    private TableUtils() {
    }

    public static String prettyPrint(TableModel model) {
        TableBuilder tableBuilder = new TableBuilder(model);
        tableBuilder.addInnerBorder(BorderStyle.fancy_light);
        tableBuilder.addHeaderBorder(BorderStyle.fancy_double);
        tableBuilder.addOutlineBorder(BorderStyle.fancy_light);
        return tableBuilder.build().render(120);
    }
}
