package repository;
import model.Produto;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ProdutoRepository {
    private String caminho = "src/data/produtos.txt";

    public  List<Produto> listarProdutos(){
        List<Produto> produtos = new ArrayList<>();

        try(BufferedReader bf = new BufferedReader(new FileReader(caminho))) {
            String linha;
            while ((linha = bf.readLine()) != null){
                if (linha.trim().isEmpty()) continue;

                String[] partes = linha.split(";");
                int id = Integer.parseInt(partes[0]);
                String nome = partes[1];
                double preco = Double.parseDouble(partes[2]);
                int estoque = Integer.parseInt(partes[3]);

                Produto produto = new Produto(id,nome,preco,estoque);
                produtos.add(produto);
            }

        } catch (IOException e) {
            System.out.println("Erro ao ler produtos: " + e.getMessage());
        }

        return produtos;
    }

    public void salvarListaProdutos(List<Produto> produtos) {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(caminho))) {

            for (Produto p : produtos) {

                String linha = p.getId() + ";"
                        + p.getNome() + ";"
                        + p.getPreco() + ";"
                        + p.getEstoque();

                bw.write(linha);
                bw.newLine();
            }

        } catch (IOException e) {
            System.out.println("Erro ao salvar produtos");
        }
    }

    public void excluirProduto(int idProduto) {

        List<Produto> produtos = listarProdutos();

        Produto produtoRemover = null;

        for (Produto p : produtos) {

            if (p.getId() == idProduto) {

                produtoRemover = p;
                break;
            }
        }

        if (produtoRemover != null) {

            produtos.remove(produtoRemover);

            salvarListaProdutos(produtos);
        }
    }

    public int gerarNovoId() {

        List<Produto> produtos = listarProdutos();

        int maiorId = 0;


        for (Produto p : produtos) {

            if (p.getId() > maiorId) {

                maiorId = p.getId();

            }
        }


        return maiorId + 1;
    }
}
