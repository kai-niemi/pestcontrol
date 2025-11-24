package io.cockroachdb.pest.shell.support;

import java.util.ArrayList;
import java.util.List;

import org.springframework.shell.table.TableModel;

public class ListTableModel<T> extends TableModel {
    private final List<T> data;

    private final List<Object> headerRow;

    private final ValueProvider<T> provider;

    public ListTableModel(Iterable<T> list,
                          List<Object> header,
                          ValueProvider<T> provider) {
        this.data = new ArrayList<>();
        list.forEach(data::add);
        this.headerRow = new ArrayList<>(header);
        this.provider = provider;
    }

    @Override
    public int getRowCount() {
        return 1 + data.size();
    }

    @Override
    public int getColumnCount() {
        return headerRow.size();
    }

    @Override
    public Object getValue(int row, int column) {
        if (headerRow != null && row == 0) {
            return headerRow.get(column);
        }
        int rowToUse = headerRow == null ? row : row - 1;
        return provider.getValue(data.get(rowToUse), column);
    }
}
