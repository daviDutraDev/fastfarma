package service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;

import model.Pedido;
import model.Produto;
import repository.PedidoRepository;
import repository.ProdutoRepository;

import javax.swing.*;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RelatorioService {

    public static void gerarRelatorio() {

        try {

            PedidoRepository pedidoRepo =
                    new PedidoRepository();

            ProdutoRepository produtoRepo =
                    new ProdutoRepository();

            List<Pedido> pedidos =
                    pedidoRepo.listarPedidos();

            List<Produto> produtos =
                    produtoRepo.listarProdutos();

            Document documento =
                    new Document();

            PdfWriter.getInstance(
                    documento,
                    new FileOutputStream(
                            "relatorio_fastfarma.pdf"
                    )
            );

            documento.open();

            // TÍTULO
            Font tituloFonte =
                    new Font(
                            Font.FontFamily.HELVETICA,
                            22,
                            Font.BOLD
                    );

            Paragraph titulo =
                    new Paragraph(
                            "RELATÓRIO FASTFARMA",
                            tituloFonte
                    );

            titulo.setSpacingAfter(20);

            documento.add(titulo);

            // ================= RECEITA =================

            double receitaTotal = 0;

            Map<Integer, Integer> vendas =
                    new HashMap<>();

            for (Pedido pedido : pedidos) {

                for (int idProduto :
                        pedido.getIdsProdutos()) {

                    for (Produto produto :
                            produtos) {

                        if (produto.getId()
                                == idProduto) {

                            receitaTotal +=
                                    produto.getPreco();

                            vendas.put(
                                    idProduto,
                                    vendas.getOrDefault(
                                            idProduto,
                                            0
                                    ) + 1
                            );
                        }
                    }
                }
            }

            documento.add(
                    new Paragraph(
                            "Receita Total: R$ "
                                    + receitaTotal
                    )
            );

            documento.add(
                    new Paragraph(
                            "Quantidade de Pedidos: "
                                    + pedidos.size()
                    )
            );

            // ================= TICKET MÉDIO =================

            double ticketMedio = 0;

            if (!pedidos.isEmpty()) {

                ticketMedio =
                        receitaTotal / pedidos.size();
            }

            documento.add(
                    new Paragraph(
                            "Ticket Médio: R$ "
                                    + String.format(
                                    "%.2f",
                                    ticketMedio
                            )
                    )
            );

            documento.add(
                    new Paragraph(" ")
            );

            // ================= PRODUTOS MAIS VENDIDOS =================

            Font subtituloFonte =
                    new Font(
                            Font.FontFamily.HELVETICA,
                            18,
                            Font.BOLD
                    );

            Paragraph subtitulo =
                    new Paragraph(
                            "Produtos Mais Vendidos",
                            subtituloFonte
                    );

            subtitulo.setSpacingAfter(10);

            documento.add(subtitulo);

            for (Map.Entry<Integer, Integer> venda :
                    vendas.entrySet()) {

                Produto produtoEncontrado = null;

                for (Produto p : produtos) {

                    if (p.getId()
                            == venda.getKey()) {

                        produtoEncontrado = p;

                        break;
                    }
                }

                if (produtoEncontrado != null) {

                    documento.add(
                            new Paragraph(
                                    produtoEncontrado.getNome()
                                            + " -> "
                                            + venda.getValue()
                                            + " vendas"
                            )
                    );
                }
            }

            documento.add(
                    new Paragraph(" ")
            );

            // ================= ESTOQUE BAIXO =================

            Paragraph estoqueTitulo =
                    new Paragraph(
                            "Produtos Com Estoque Baixo",
                            subtituloFonte
                    );

            estoqueTitulo.setSpacingBefore(15);

            estoqueTitulo.setSpacingAfter(10);

            documento.add(estoqueTitulo);

            for (Produto p : produtos) {

                if (p.getEstoque() <= 5) {

                    documento.add(
                            new Paragraph(
                                    p.getNome()
                                            + " -> "
                                            + p.getEstoque()
                                            + " unidades"
                            )
                    );
                }
            }

            documento.close();

            JOptionPane.showMessageDialog(
                    null,
                    "Relatório PDF gerado com sucesso!"
            );

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    null,
                    "Erro ao gerar relatório: "
                            + e.getMessage()
            );
        }
    }
}