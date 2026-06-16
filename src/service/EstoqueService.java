package service;

import model.Produto;
import repository.ProdutoRepository;

import java.util.List;

public class EstoqueService {


    private ProdutoRepository repo;


    public EstoqueService() {

        repo = new ProdutoRepository();

    }



    public void baixarEstoque(int idProduto) {


        List<Produto> produtos =
                repo.listarProdutos();


        for (Produto p : produtos) {


            if (p.getId() == idProduto) {


                if (p.getEstoque() > 0) {

                    p.setEstoque(
                            p.getEstoque() - 1
                    );

                }

            }

        }


        repo.salvarListaProdutos(produtos);

    }




    public void devolverEstoque(List<Integer> idsProdutos) {


        List<Produto> produtos =
                repo.listarProdutos();



        for (int id : idsProdutos) {


            for (Produto p : produtos) {


                if (p.getId() == id) {


                    p.setEstoque(
                            p.getEstoque() + 1
                    );


                }

            }

        }



        repo.salvarListaProdutos(produtos);

    }
    public void adicionarEstoque(int idProduto, int quantidade){

        ProdutoRepository repo = new ProdutoRepository();

        List<Produto> produtos = repo.listarProdutos();


        for(Produto p : produtos){

            if(p.getId() == idProduto){

                p.setEstoque(
                        p.getEstoque() + quantidade
                );

                break;
            }
        }


        repo.salvarListaProdutos(produtos);

    }

}