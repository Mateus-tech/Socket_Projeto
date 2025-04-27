import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.util.*;

public class Client {
    public static void main(String[] args) {
        try (Socket socket = new Socket("127.0.0.1", 12345);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Conectado ao servidor.");
            User user = new User().solicitarCredenciais();
            out.writeObject(user);
            out.flush();
            System.out.println((String) in.readObject());

            while (true) {
                System.out.println("\n1-LIST  2-UPLOAD  3-DOWNLOAD  4-EXIT");
                String opcao = scanner.nextLine();
                switch (opcao) {
                    case "1":
                        out.writeObject("LIST");
                        out.flush();
                        Map<String, List<String>> mapas = (Map<String, List<String>>) in.readObject();
                        mapas.forEach((tipo, lista) -> System.out.println(tipo + ": " + lista));
                        break;
                    case "2":
                        out.writeObject("UPLOAD");
                        out.flush();
                        System.out.print("Arquivo local: ");
                        String arquivo = scanner.nextLine();
                        Path path = Paths.get(arquivo);
                        // tenta pasta src caso não ache na raiz
                        if (!Files.exists(path)) {
                            path = Paths.get("src", arquivo);
                        }
                        if (!Files.exists(path)) {
                            System.out.println("Arquivo não encontrado localmente: " + arquivo);
                            break;
                        }
                        byte[] dados = Files.readAllBytes(path);
                        out.writeObject(path.getFileName().toString());
                        out.writeObject(dados);
                        out.flush();
                        System.out.println((String) in.readObject());
                        break;
                    case "3":
                        out.writeObject("DOWNLOAD");
                        out.flush();
                        System.out.print("Arquivo para download: ");
                        String nome = scanner.nextLine();
                        out.writeObject(nome);
                        out.flush();
                        byte[] resposta = (byte[]) in.readObject();
                        if (resposta != null) {
                            Files.write(Paths.get("download_" + nome), resposta);
                            System.out.println("Download salvo como: download_" + nome);
                        } else {
                            System.out.println("Arquivo não encontrado no servidor.");
                        }
                        break;
                    case "4":
                        out.writeObject("EXIT");
                        out.flush();
                        return;
                    default:
                        System.out.println("Opção inválida.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
