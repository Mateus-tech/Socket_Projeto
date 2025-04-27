
import java.io.Serializable;
import java.util.Scanner;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String password;

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public User() {}

    public String getName() { return name; }
    public String getPassword() { return password; }

    public User solicitarCredenciais() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Digite seu nome: ");
        String nome = scanner.nextLine();
        System.out.print("Digite sua senha: ");
        String senha = scanner.nextLine();
        return new User(nome, senha);
    }
}
