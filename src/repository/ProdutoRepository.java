package repository;
import model.Produto;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProdutoRepository {
    private String caminho = "src/data/produtos.txt";

    public  List<Produto> listarProdutos(){
        List<Produto> produtos = new ArrayList<>();

        try(BufferedReader bf = new BufferedReader(new FileReader(caminho))) {
            String linha;
            while ((linha = bf.readLine()) != null){
                String[] partes = linha.split(";");
                int id = Integer.parseInt(partes[0]);
                String nome = partes[1];
                double preco = Double.parseDouble(partes[2]);

                Produto produto = new Produto(id,nome,preco);
                produtos.add(produto);
            }

        } catch (IOException e) {
            System.out.println("Erro ao ler produtos: " + e.getMessage());
        }

        return produtos;
    }
}
