package app;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        System.setProperty("sun.net.httpserver.maxReqTime", "5");
        System.setProperty("sun.net.httpserver.maxRspTime", "5");
        System.setProperty("jdk.httpserver.maxConnections", "32");

        RentalService service = new RentalService();
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/ping", new PingHandler());
        server.createContext("/bikes", new ApiHandler(service));
        server.createContext("/customers", new ApiHandler(service));
        server.start();
        System.out.println("Сервер запущен: http://localhost:8080/ping");
    }
}
