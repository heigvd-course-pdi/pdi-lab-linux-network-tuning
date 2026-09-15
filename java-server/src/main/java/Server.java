import io.javalin.Javalin;
import io.javalin.http.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.*;
import java.util.Arrays;

public class Server {
    private static final String POSTGRES_HOST = System.getenv("POSTGRES_HOST");
    private static final String POSTGRES_PORT = System.getenv("POSTGRES_PORT");
    private static final String POSTGRES_DB = System.getenv("POSTGRES_DB");
    private static final String POSTGRES_USER = System.getenv("POSTGRES_USER");
    private static final String POSTGRES_PASSWORD = System.getenv("POSTGRES_PASSWORD");
    private static final String DB_URL = "jdbc:postgresql://" + POSTGRES_HOST + ":" + POSTGRES_PORT + "/" + POSTGRES_DB;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static HikariDataSource dataSource;

    public static void main(String[] args) {
        initializeConnectionPool();

        var app = Javalin.create(config -> {
            config.useVirtualThreads = true;
        }).start(8080);

        app.get("/", Server::handleGet);

        System.out.println("Server started on port 8080 using virtual threads");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (dataSource != null) {
                dataSource.close();
            }
        }));
    }

    private static void initializeConnectionPool() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DB_URL);
        config.setUsername(POSTGRES_USER);
        config.setPassword(POSTGRES_PASSWORD);
	config.setMaximumPoolSize(20);

        dataSource = new HikariDataSource(config);
    }

    private static void handleGet(Context ctx) {
        try {
            int[] dbResult = queryDb();
            String json = objectMapper.writeValueAsString(dbResult);
            ctx.contentType("application/json").result(json);
        } catch (Exception e) {
            ctx.status(500).result("Error querying database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static int[] queryDb() throws SQLException {
        int[] randomNums = new int[20000];

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT (RANDOM() * 1000000)::INTEGER as random_num";

            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int randomNum = resultSet.getInt("random_num");
                    Arrays.fill(randomNums, randomNum);
                }
            }
        }
        return randomNums;
    }
}
