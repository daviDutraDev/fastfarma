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
        // aqui você vai implementar

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(caminho, true))) {
            String salvar = pedido.getId() + ";"
                    + pedido.getCodigoVerificacao() + ";"
                    + pedido.getCriadoPor() + ";"
                    + pedido.getStatus() + ";"
                    + pedido.getIdProduto();

            bw.write(salvar);
            bw.newLine();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Ocorreu um erro ao Salvar arquivo");
        }
    }



        public  List<Pedido> listarPedidos(){
            List<Pedido> pedidos = new ArrayList<>();

            try(BufferedReader bf = new BufferedReader(new FileReader(caminho))) {
                String linha;
                while ((linha = bf.readLine()) != null){
                    String[] partes = linha.split(";");
                    int id = Integer.parseInt(partes[0]);
                    int CodigoVerificacao = Integer.parseInt(partes[1]);
                    String criadoPor = partes[2];
                    StatusPedido status = StatusPedido.valueOf(partes[3]);
                    int idProduto = Integer.parseInt(partes[4]);


                    Pedido pedido = new Pedido(id,CodigoVerificacao,criadoPor,status,idProduto);
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

                String linha = pedido.getId() + ";"
                        + pedido.getCodigoVerificacao() + ";"
                        + pedido.getCriadoPor() + ";"
                        + pedido.getStatus() + ";"
                        + pedido.getIdProduto();

                bw.write(linha);
                bw.newLine();
            }

        } catch (IOException e) {
            System.out.println("Erro ao salvar pedidos: " + e.getMessage());
        }
    }




    }



