
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;

public class Server {
    private static final int PORT = 12345;
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private static final List<User> userList = new ArrayList<>();

    public static void main(String[] args) {

        setupLogger();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Servidor iniciado na porta " + PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                logger.info("Cliente conectado: " + socket.getRemoteSocketAddress());
                new Thread(() -> handleClient(socket)).start();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Erro no servidor", e);
        }
    }

    private static void handleClient(Socket socket) {
        try (
                ObjectOutputStream saida = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream())
        ) {
            User user = (User) entrada.readObject();
            boolean exists = userList.stream().anyMatch(u -> u.getName().equals(user.getName()));
            if (exists) {
                boolean valid = userList.stream()
                        .anyMatch(u -> u.getName().equals(user.getName()) && u.getPassword().equals(user.getPassword()));
                if (valid) {
                    saida.writeObject("Login válido");
                    logger.info("Login válido: " + user.getName());
                } else {
                    saida.writeObject("Credenciais inválidas. Conexão encerrada.");
                    logger.warning("Falha de login para usuário existente: " + user.getName());
                    saida.flush();
                    socket.close();
                    return;
                }
            } else {
                userList.add(user);
                saida.writeObject("Novo usuário cadastrado: " + user.getName());
                logger.info("Usuário adicionado: " + user.getName());
            }
            saida.flush();
            createDirectories(user.getName());
            Manager manager = new Manager(user);

            while (true) {
                String comando = (String) entrada.readObject();
                switch (comando) {
                    case "LIST":
                        Map<String, List<String>> arquivos = manager.listarArquivos();
                        saida.writeObject(arquivos); saida.flush();
                        break;
                    case "UPLOAD":
                        String nomeArquivo = (String) entrada.readObject();
                        byte[] conteudo = (byte[]) entrada.readObject();
                        if (!manager.extensaoValida(nomeArquivo)) {
                            saida.writeObject("Extensão não permitida");
                        } else if (conteudo.length > 10 * 1024 * 1024) {
                            saida.writeObject("Arquivo excede tamanho máximo (10MB)");
                        } else {
                            manager.enviarArquivo(nomeArquivo, conteudo);
                            saida.writeObject("Upload de '" + nomeArquivo + "' concluído");
                            logger.info("Upload: " + user.getName() + "/" + nomeArquivo);
                        }
                        saida.flush();
                        break;
                    case "DOWNLOAD":
                        String toDownload = (String) entrada.readObject();
                        byte[] data = manager.baixarArquivo(toDownload);
                        saida.writeObject(data); saida.flush();
                        logger.info(data != null ? "Download servido: " + toDownload : "Arquivo não encontrado: " + toDownload);
                        break;
                    case "EXIT":
                        socket.close();
                        logger.info("Cliente desconectado: " + user.getName());
                        return;
                    default:
                        saida.writeObject("Comando desconhecido"); saida.flush();
                }
            }
        } catch (EOFException eof) {
            logger.info("Conexão encerrada pelo cliente");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro ao tratar cliente", e);
        }
    }

    private static void setupLogger() {
        try {
            Handler fileHandler = new FileHandler("server.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setLevel(Level.INFO);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createDirectories(String userName) throws IOException {
        for (String t : List.of("PDF", "JPG", "TXT")) {
            Files.createDirectories(Paths.get("storage", userName, t));
        }
    }
}
