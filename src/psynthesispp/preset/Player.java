package psynthesispp.preset;


import java.rmi.*;

public interface Player extends Remote {
    
    Move request() throws Exception, RemoteException;
    
    void confirm(Status status) throws Exception, RemoteException;
    
    void update(Move opponentMove, Status status) throws Exception, RemoteException;
    
    void init(int boardSize, PlayerColor color) throws Exception, RemoteException;
}
