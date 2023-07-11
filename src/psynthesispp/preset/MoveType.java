package psynthesispp.preset;


import java.io.Serializable;

public enum MoveType implements Serializable {
    Prepare,
    Activate,
    Plant,
    Grow,
    Empty,
    Surrender,
    End
}
