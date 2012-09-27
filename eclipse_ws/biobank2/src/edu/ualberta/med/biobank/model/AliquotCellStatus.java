package edu.ualberta.med.biobank.model;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

import edu.ualberta.med.biobank.BioBankPlugin;

public enum AliquotCellStatus {
    EMPTY(SWT.COLOR_WHITE, "Empty"), FILLED(SWT.COLOR_DARK_GRAY, "Filled"), NEW(
        SWT.COLOR_DARK_GREEN, "New"), MOVED(217, 161, 65, "Moved"), MISSING(
        SWT.COLOR_CYAN, "Missing"), ERROR(SWT.COLOR_RED, "Error"), NO_TYPE(
        SWT.COLOR_DARK_GREEN, "No type"), TYPE(SWT.COLOR_DARK_GRAY, "Type");

    private Color color;
    private String legend;

    private AliquotCellStatus(int color, String legend) {
        this.color = BioBankPlugin.getDefault().getWorkbench().getDisplay()
            .getSystemColor(color);
        this.legend = legend;
    }

    private AliquotCellStatus(int red, int green, int blue, String legend) {
        this.color = new Color(BioBankPlugin.getDefault().getWorkbench()
            .getDisplay(), red, green, blue);
        this.legend = legend;
    }

    public Color getColor() {
        return color;
    }

    public String getLegend() {
        return legend;
    }

    public AliquotCellStatus mergeWith(AliquotCellStatus newStatus) {
        switch (this) {
        case EMPTY:
            return newStatus;
        case FILLED:
        case MOVED:
            if (newStatus == MISSING || newStatus == ERROR) {
                return newStatus;
            }
            return this;
        case ERROR:
            return ERROR;
        case MISSING:
            if (newStatus == ERROR) {
                return ERROR;
            }
            return MISSING;
        default:
            break;
        }
        return AliquotCellStatus.EMPTY;
    }
}