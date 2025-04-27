
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class Manager {
    private final String basePath = "storage";
    private final String user;

    public Manager(User user) {
        this.user = user.getName();
    }

    /**
     * Verifica se extensão é PDF, JPG ou TXT.
     */
    public boolean extensaoValida(String nomeArquivo) {
        String ext = nomeArquivo.substring(nomeArquivo.lastIndexOf('.') + 1).toUpperCase();
        return List.of("PDF", "JPG", "TXT").contains(ext);
    }

    /**
     * Salva bytes em storage/usuário/extensão/nomeArquivo.
     */
    public void enviarArquivo(String nomeArquivo, byte[] conteudo) throws IOException {
        String ext = nomeArquivo.substring(nomeArquivo.lastIndexOf('.') + 1).toUpperCase();
        Path destino = Paths.get(basePath, user, ext, nomeArquivo);
        Files.write(destino, conteudo);
    }

    /**
     * Busca arquivo nos três tipos e retorna bytes, ou null se não existir.
     */
    public byte[] baixarArquivo(String nomeArquivo) throws IOException {
        for (String tipo : List.of("PDF", "JPG", "TXT")) {
            Path path = Paths.get(basePath, user, tipo, nomeArquivo);
            if (Files.exists(path)) {
                return Files.readAllBytes(path);
            }
        }
        return null;
    }

    /**
     * Gera mapa Tipo->[arquivos] para cada pasta do usuário.
     */
    public Map<String, List<String>> listarArquivos() throws IOException {
        Map<String, List<String>> mapa = new HashMap<>();
        for (String tipo : List.of("PDF", "JPG", "TXT")) {
            Path dir = Paths.get(basePath, user, tipo);
            List<String> lista = new ArrayList<>();
            if (Files.exists(dir)) {
                try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
                    for (Path p : ds) lista.add(p.getFileName().toString());
                }
            }
            mapa.put(tipo, lista);
        }
        return mapa;
    }
}
