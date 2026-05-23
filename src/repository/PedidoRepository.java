package repository;

import model.Pedido;
import model.Produto;
import model.StatusPedido;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PedidoRepository {
    private String caminho = "src/data/pedidos.txt";

    public void salvarPedido(Pedido pedido) {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(caminho, true))) {

            String produtos = "";

            for (int id : pedido.getIdsProdutos()) {
                produtos += id + ",";
            }


            produtos = produtos.substring(0, produtos.length() - 1);

            String salvar = pedido.getId() + ";"
                    + pedido.getCodigoVerificacao() + ";"
                    + pedido.getCriadoPor() + ";"
                    + pedido.getStatus() + ";"
                    + produtos;

            bw.write(salvar);
            bw.newLine();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Erro ao salvar pedido");
        }
    }



        public  List<Pedido> listarPedidos(){
            List<Pedido> pedidos = new ArrayList<>();

            try(BufferedReader bf = new BufferedReader(new FileReader(caminho))) {
                String linha;
                while ((linha = bf.readLine()) != null){
                    if (linha.trim().isEmpty()) continue;
                    String[] partes = linha.split(";");
                    int id = Integer.parseInt(partes[0]);
                    int CodigoVerificacao = Integer.parseInt(partes[1]);
                    String criadoPor = partes[2];
                    StatusPedido status = StatusPedido.valueOf(partes[3]);
                    String[] produtosStr = partes[4].split(",");

                    List<Integer> idsProdutos = new ArrayList<>();

                    for (String s : produtosStr) {
                        idsProdutos.add(Integer.parseInt(s));
                    }


                    Pedido pedido = new Pedido(id,CodigoVerificacao,criadoPor,status,idsProdutos);
                    pedidos.add(pedido);
                }

            } catch (IOException e) {
                System.out.println("Erro ao ler pedidos: " + e.getMessage());
            }

            return pedidos;
        }

    public int gerarNovoId() {

        List<Pedido> pedidos = listarPedidos();

        int maiorId = 0;

        for (Pedido p : pedidos) {
            if (p.getId() > maiorId) {
                maiorId = p.getId();
            }
        }

        return maiorId + 1;
    }

    public void salvarListaPedidos(List<Pedido> pedidos) {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(caminho))) {

            for (Pedido pedido : pedidos) {

                String produtos = "";

                for (int id : pedido.getIdsProdutos()) {
                    produtos += id + ",";
                }

                // remove última vírgula
                produtos = produtos.substring(0, produtos.length() - 1);

                String linha = pedido.getId() + ";"
                        + pedido.getCodigoVerificacao() + ";"
                        + pedido.getCriadoPor() + ";"
                        + pedido.getStatus() + ";"
                        + produtos;

                bw.write(linha);
                bw.newLine();
            }

        } catch (IOException e) {
            System.out.println("Erro ao salvar pedidos: " + e.getMessage());
        }
    }




    }



