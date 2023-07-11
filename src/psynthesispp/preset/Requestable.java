package psynthesispp.preset;


public interface Requestable {
    Move request(MoveType type) throws Exception;
}
