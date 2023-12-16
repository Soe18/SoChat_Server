package Server;

public class ServerMain {
    public static void main(String[] args) {
        ServerSkeleton serverSkeleton = new ServerSkeleton();

        serverSkeleton.exec(8080);
    }
}
