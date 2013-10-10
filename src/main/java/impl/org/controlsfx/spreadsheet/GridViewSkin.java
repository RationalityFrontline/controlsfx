/**
 * Copyright (c) 2013, ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package impl.org.controlsfx.spreadsheet;

import java.lang.reflect.Field;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Skin;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableFocusModel;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetColumn;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

import com.sun.javafx.scene.control.skin.TableCellSkin;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import com.sun.javafx.scene.control.skin.VirtualScrollBar;

/**
 * Despite the name, this skin is actually the skin of the TableView contained
 * within the SpreadsheetView. The skin for the SpreadsheetView itself currently
 * resides inside the SpreadsheetView constructor!
 * 
 */
public class GridViewSkin extends TableViewSkin<ObservableList<SpreadsheetCell>> {
    /** Default height of a cell. */

    static final double DEFAULT_CELL_HEIGHT;
    static {
        double cell_size = 24.0;
        try {
            Class<?> clazz = com.sun.javafx.scene.control.skin.CellSkinBase.class;
            Field f = clazz.getDeclaredField("DEFAULT_CELL_SIZE");
            f.setAccessible(true);
            cell_size = f.getDouble(null);
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        DEFAULT_CELL_HEIGHT = cell_size;
    }
    /** Default with of a "row header?". */
    private final double DEFAULT_ROWHEADER_WIDTH = 50.0;
    /** The editor. */
    private GridCellEditor spreadsheetCellEditorImpl;

    
    private final double rowHeaderWidth = DEFAULT_ROWHEADER_WIDTH;
    
    //@Override
    public static Skin<?> createCellSkin(CellView cell) {
        return new TableCellSkin<>(cell);
    }
    
    public double getRowHeaderWidth() {
        return rowHeaderWidth;
    }

    protected final SpreadsheetHandle handle;
    protected SpreadsheetView spreadsheetView;
    protected VerticalHeader rowHeader;

    public GridViewSkin(final SpreadsheetHandle handle) {
        super(handle.getGridView());
        this.handle = handle;
        this.spreadsheetView = handle.getView();
        spreadsheetCellEditorImpl = new GridCellEditor(handle);
        TableView<ObservableList<SpreadsheetCell>> tableView = handle.getGridView();
        tableView.setEditable(true);

        // Do nothing basically but give access to the Hover Property.
        tableView.setRowFactory(new Callback<TableView<ObservableList<SpreadsheetCell>>, TableRow<ObservableList<SpreadsheetCell>>>() {
            @Override public TableRow<ObservableList<SpreadsheetCell>> call(TableView<ObservableList<SpreadsheetCell>> p) {
                return new GridRow(handle);
            }
        });

        tableView.setFixedCellSize(getDefaultCellSize());

        tableView.getStyleClass().add("cell-spreadsheet");

        /*****************************************************************
         * MODIFIED BY NELLARMONIA
         *****************************************************************/
        spreadsheetView.getFixedRows().addListener(fixedRowsListener);
        spreadsheetView.getFixedColumns().addListener(fixedColumnsListener);
//        spreadsheetView.setHbar(getFlow().getHorizontalBar());
//        spreadsheetView.setVbar(getFlow().getVerticalBar());
        /*****************************************************************
         * END MODIFIED BY NELLARMONIA
         *****************************************************************/
        init();
    }

	protected void init() {
        getFlow().getVerticalBar().valueProperty()
                .addListener(vbarValueListener);
        rowHeader = new VerticalHeader(handle, rowHeaderWidth);
        getChildren().addAll(rowHeader);

        rowHeader.init(this);
        ((HorizontalHeader) getTableHeaderRow()).init();
        getFlow().init(spreadsheetView);
    }

    @Override
    protected void layoutChildren(double x, double y, double w, final double h) {
        if (spreadsheetView == null) { return; }
        if (spreadsheetView.showRowHeaderProperty().get()) {
            x += rowHeaderWidth;
            w -= rowHeaderWidth;
        }

        super.layoutChildren(x, y, w, h);

        final double baselineOffset = getSkinnable().getLayoutBounds()
                .getHeight() / 2;
        double tableHeaderRowHeight = 0;

        if (spreadsheetView.showColumnHeaderProperty().get()) {
            // position the table header
            tableHeaderRowHeight = getTableHeaderRow().prefHeight(-1);
            layoutInArea(getTableHeaderRow(), x, y, w, tableHeaderRowHeight,
                    baselineOffset, HPos.CENTER, VPos.CENTER);

            y += tableHeaderRowHeight;
        } else {
            // FIXME try to hide the columnHeader
        	// FIXME tweak open in RT-32673
        }

        if (spreadsheetView.showRowHeaderProperty().get()) {
            layoutInArea(rowHeader, x - rowHeaderWidth, y
                    - tableHeaderRowHeight, w, h, baselineOffset, HPos.CENTER,
                    VPos.CENTER);
        }
    }
    final InvalidationListener vbarValueListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable valueModel) {
            verticalScroll();
        }
    };

    protected void verticalScroll() {
        rowHeader.updateScrollY();
    }

    @Override
    protected void onFocusPreviousCell() {
        final TableFocusModel<?, ?> fm = getFocusModel();
        if (fm == null) { return; }
        /*****************************************************************
         * MODIFIED BY NELLARMONIA
         *****************************************************************/
        final int row = fm.getFocusedIndex();
        // We try to make visible the rows that may be hiden by Fixed rows
        if (!getFlow().getCells().isEmpty()
                && getFlow().getCells().get(getFlow().getFixedRows().size())
                        .getIndex() > row
                && !getFlow().getFixedRows().contains(row)) {
            flow.scrollTo(row);
        } else {
            flow.show(row);
        }
        scrollHorizontally();
        /*****************************************************************
         * END OF MODIFIED BY NELLARMONIA
         *****************************************************************/
    }

    @Override
    protected void onFocusNextCell() {
        final TableFocusModel<?, ?> fm = getFocusModel();
        if (fm == null) { return; }
        /*****************************************************************
         * MODIFIED BY NELLARMONIA
         *****************************************************************/
        final int row = fm.getFocusedIndex();
        // We try to make visible the rows that may be hiden by Fixed rows
        if (!getFlow().getCells().isEmpty()
                && getFlow().getCells().get(getFlow().getFixedRows().size())
                        .getIndex() > row
                && !getFlow().getFixedRows().contains(row)) {
            flow.scrollTo(row);
        } else {
            flow.show(row);
        }
        scrollHorizontally();
        /*****************************************************************
         * END OF MODIFIED BY NELLARMONIA
         *****************************************************************/
    }
    @Override
    protected void onSelectPreviousCell() {
        super.onSelectPreviousCell();
        scrollHorizontally();
    }

    @Override
    protected void onSelectNextCell() {
        super.onSelectNextCell();
        scrollHorizontally();
    }
    
    /**
     * @return the defaultCellSize
     */
    public double getDefaultCellSize() {
        return DEFAULT_CELL_HEIGHT;
    }

    /**
     * We listen on the FixedRows in order to do the modification in the
     * VirtualFlow
     */
    private final ListChangeListener<Integer> fixedRowsListener = new ListChangeListener<Integer>() {
        @Override
        public void onChanged(Change<? extends Integer> c) {
           /* while (c.next()) {
                for (final Integer remitem : c.getRemoved()) {
                    getFlow().getFixedRows().remove(remitem);
                }
                for (final Integer additem : c.getAddedSubList()) {
                    getFlow().getFixedRows().add(additem);
                }
            }*/
            // requestLayout() not responding immediately..
            getFlow().layoutTotal();
        }

    };

    /**
     * We listen on the FixedColumns in order to do the modification in the
     * VirtualFlow
     */
    private final ListChangeListener<SpreadsheetColumn<?>> fixedColumnsListener = new ListChangeListener<SpreadsheetColumn<?>>() {
        @Override
        public void onChanged(Change<? extends SpreadsheetColumn<?>> c) {
            if (spreadsheetView.getFixedColumns().size() > c.getList().size()) {
                for (int i = 0; i < getFlow().getCells().size(); ++i) {
                    ((GridRow) getFlow().getCells().get(i))
                            .putFixedColumnToBack();
                }
            }

            /*while (c.next()) {
                for (final Integer remitem : c.getRemoved()) {
                    getFlow().getFixedColumns().remove(remitem);
                }
                for (final Integer additem : c.getAddedSubList()) {
                    getFlow().getFixedColumns().add(additem);
                }
            }*/
            // requestLayout() not responding immediately..
            getFlow().layoutTotal();
        }

    };

    @Override
    protected VirtualFlow<TableRow<ObservableList<SpreadsheetCell>>> createVirtualFlow() {
        return new GridVirtualFlow<TableRow<ObservableList<SpreadsheetCell>>>();
    }

    protected TableHeaderRow createTableHeaderRow() {
        return new HorizontalHeader(this);
    }

    BooleanProperty getTableMenuButtonVisibleProperty() {
        return tableMenuButtonVisibleProperty();
    }

    @Override
    protected void scrollHorizontally(TableColumn<ObservableList<SpreadsheetCell>, ?> col) {

        if (col == null || !col.isVisible()) { return; }

        // work out where this column header is, and it's width (start -> end)
        double start = 0;// scrollX;
        for (TableColumnBase<?, ?> c : getVisibleLeafColumns()) {
            if (c.equals(col)) break;
            start += c.getWidth();
        }

        /*****************************************************************
         * MODIFIED BY NELLARMONIA We modifed this function so that we ensure
         * that any selected cells will not be below a fixed column. Because
         * when there's some fixed columns, the "left border" is not the table
         * anymore, but the right side of the last fixed columns.
         *****************************************************************/
        // We add the fixed columns width
        final double fixedColumnWidth = getFixedColumnWidth();

        /*****************************************************************
         * END OF MODIFIED BY NELLARMONIA
         *****************************************************************/
        final double end = start + col.getWidth();

        // determine the visible width of the table
        final double headerWidth = getSkinnable().getWidth()
                - snappedLeftInset() - snappedRightInset();

        // determine by how much we need to translate the table to ensure that
        // the start position of this column lines up with the left edge of the
        // tableview, and also that the columns don't become detached from the
        // right edge of the table
        final double pos = getFlow().getHorizontalBar().getValue();
        final double max = getFlow().getHorizontalBar().getMax();
        double newPos;

        /*****************************************************************
         * MODIFIED BY NELLARMONIA
         *****************************************************************/
        if (start < pos + fixedColumnWidth && start >= 0
                && start >= fixedColumnWidth) {
            newPos = start - fixedColumnWidth < 0 ? start : start
                    - fixedColumnWidth;
        } else {
            final double delta = start < 0 || end > headerWidth ? start - pos
                    - fixedColumnWidth : 0;
            newPos = pos + delta > max ? max : pos + delta;
        }

        /*****************************************************************
         * END OF MODIFIED BY NELLARMONIA
         *****************************************************************/

        // FIXME we should add API in VirtualFlow so we don't end up going
        // direct to the hbar.
        // actually shift the flow - this will result in the header moving
        // as well
        getFlow().getHorizontalBar().setValue(newPos);
    }

    /**
     * Calc the width of the fixed columns in order not to select cells that are
     * hidden by the fixed columns
     * 
     * @return
     */
    private double getFixedColumnWidth() {
        double fixedColumnWidth = 0;
        if (!spreadsheetView.getFixedColumns().isEmpty()) {
            for (int i = 0, max = spreadsheetView.getFixedColumns().size(); i < max; ++i) {
                final TableColumnBase<ObservableList<SpreadsheetCell>, ?> c = getVisibleLeafColumn(i);
                fixedColumnWidth += c.getWidth();
            }
        }
        return fixedColumnWidth;
    }

    public GridVirtualFlow<?> getFlow() {
        return (GridVirtualFlow<?>) flow;
    }

    public GridRow getCell(int index) {
        return (GridRow) getFlow().getCells().get(index);
    }
    
    public int getCellsSize() {
        return getFlow().getCells().size();
    }
    
    public VirtualScrollBar getHBar() {
        return getFlow().getHorizontalBar();
    }
    
    public VirtualScrollBar getVBar() {
        return getFlow().getVerticalBar();
    }

    public GridRow getRow(int index) {
        return handle.getCellsViewSkin().getCell(index);
    }
    
    /**
     * A list of Integer with the current selected Rows. This is useful for columnheader and
     * RowHeader because they need to highligh when a selection is made.
     */
    private final ObservableList<Integer> selectedRows = FXCollections.observableArrayList();
    public ObservableList<Integer> getSelectedRows() {
        return selectedRows;
    }

    /**
     * A list of Integer with the current selected Columns. This is useful for columnheader and
     * RowHeader because they need to highlight when a selection is made.
     */
    private final ObservableList<Integer> selectedColumns= FXCollections.observableArrayList();
    public ObservableList<Integer> getSelectedColumns() {
        return selectedColumns;
    }

	public GridCellEditor getSpreadsheetCellEditorImpl() {
		return spreadsheetCellEditorImpl;
	}
	
	/**
	 * Used in the HorizontalColumnHeader when we need to resize
	 * in double click.
	 * @param tc
	 */
	void resize(TableColumnBase<?, ?> tc){
		resizeColumnToFitContent(getColumns().get(getColumns().indexOf(tc)), -1);
	}

}