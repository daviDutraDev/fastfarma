package service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import model.Pedido;
import model.Produto;
import model.StatusPedido;
import model.Usuario;

import repository.PedidoRepository;
import repository.ProdutoRepository;
import repository.UsuarioRepository;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class RelatorioService {


    public static void gerarRelatorio() {


        try {


            PedidoRepository pedidoRepo = new PedidoRepository();
            ProdutoRepository produtoRepo = new ProdutoRepository();
            UsuarioRepository usuarioRepo = new UsuarioRepository();



            List<Pedido> pedidos =
                    pedidoRepo.listarPedidos();


            List<Produto> produtos =
                    produtoRepo.listarProdutos();


            List<Usuario> usuarios =
                    usuarioRepo.listarUsuarios();



            File pasta = new File("relatorios");

            if(!pasta.exists()){
                pasta.mkdir();
            }


            File arquivo =
                    new File(
                            pasta,
                            "relatorio_fastfarma.pdf"
                    );



            Document doc =
                    new Document(PageSize.A4);



            PdfWriter.getInstance(
                    doc,
                    new FileOutputStream(arquivo)
            );



            doc.open();



            // ================= CABEÇALHO =================


            Font titulo =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            22,
                            BaseColor.DARK_GRAY
                    );


            Paragraph cabecalho =
                    new Paragraph(
                            "FASTFARMA\nRELATÓRIO GERENCIAL\n",
                            titulo
                    );


            cabecalho.setAlignment(Element.ALIGN_CENTER);

            doc.add(cabecalho);


            doc.add(
                    new Paragraph(
                            "Data: "
                                    +
                                    new SimpleDateFormat(
                                            "dd/MM/yyyy HH:mm"
                                    ).format(new Date())
                    )
            );


            doc.add(new Paragraph(" "));



            // ================= RESUMO =================



            doc.add(
                    tituloSecao(
                            "Resumo Geral"
                    )
            );


            doc.add(
                    new Paragraph(
                            "Clientes cadastrados: "
                                    + usuarios.size()
                    )
            );


            doc.add(
                    new Paragraph(
                            "Total de pedidos: "
                                    + pedidos.size()
                    )
            );



            double receita = 0;


            Map<String,Integer> produtosVendidos =
                    new HashMap<>();


            int pendentes=0;
            int aprovados=0;
            int prontos=0;
            int rejeitados=0;




            for(Pedido p : pedidos){



                switch(p.getStatus()){

                    case PENDENTE ->
                            pendentes++;

                    case APROVADO ->
                            aprovados++;

                    case PRONTO ->
                            prontos++;

                    case REJEITADO ->
                            rejeitados++;

                }



                if(p.getStatus()
                        == StatusPedido.REJEITADO)
                    continue;



                for(Integer id :
                        p.getIdsProdutos()){


                    for(Produto prod :
                            produtos){


                        if(prod.getId()==id){


                            receita +=
                                    prod.getPreco();


                            produtosVendidos.put(

                                    prod.getNome(),

                                    produtosVendidos.getOrDefault(
                                            prod.getNome(),
                                            0
                                    )+1

                            );

                        }

                    }

                }

            }





            doc.add(
                    new Paragraph(
                            "Receita estimada: R$ "
                                    +
                                    String.format(
                                            "%.2f",
                                            receita
                                    )
                    )
            );



            doc.add(
                    new Paragraph(
                            "Ticket médio: R$ "
                                    +
                                    String.format(
                                            "%.2f",
                                            receita / Math.max(
                                                    pedidos.size(),
                                                    1
                                            )
                                    )
                    )
            );





            // ================= STATUS =================



            doc.add(
                    tituloSecao(
                            "Status dos pedidos"
                    )
            );


            doc.add(new Paragraph(
                    "Pendentes: "+pendentes
            ));

            doc.add(new Paragraph(
                    "Aprovados: "+aprovados
            ));

            doc.add(new Paragraph(
                    "Prontos: "+prontos
            ));

            doc.add(new Paragraph(
                    "Rejeitados: "+rejeitados
            ));







            // ================= MAIS VENDIDOS =================


            doc.add(
                    tituloSecao(
                            "Produtos mais vendidos"
                    )
            );



            List<Map.Entry<String,Integer>> ranking =
                    new ArrayList<>(
                            produtosVendidos.entrySet()
                    );


            ranking.sort(
                    Map.Entry.<String,Integer>
                                    comparingByValue()
                            .reversed()
            );




            for(var item : ranking){


                doc.add(
                        new Paragraph(

                                item.getKey()
                                        +
                                        " - "
                                        +
                                        item.getValue()
                                        +
                                        " vendas"

                        )
                );


            }








            // ================= ESTOQUE =================



            doc.add(
                    tituloSecao(
                            "Produtos com estoque baixo"
                    )
            );




            boolean possuiBaixo = false;



            for(Produto p : produtos){


                if(p.getEstoque() <= 5){


                    possuiBaixo = true;


                    doc.add(
                            new Paragraph(

                                    p.getNome()
                                            +
                                            " - "
                                            +
                                            p.getEstoque()
                                            +
                                            " unidades"

                            )
                    );

                }

            }


            if(!possuiBaixo){

                doc.add(
                        new Paragraph(
                                "Nenhum produto com estoque baixo."
                        )
                );

            }





            // ================= TABELA =================



            doc.add(
                    tituloSecao(
                            "Produtos cadastrados"
                    )
            );



            PdfPTable tabela =
                    new PdfPTable(3);


            tabela.setWidthPercentage(100);



            tabela.addCell("Produto");
            tabela.addCell("Preço");
            tabela.addCell("Estoque");



            for(Produto p : produtos){


                tabela.addCell(
                        p.getNome()
                );


                tabela.addCell(
                        "R$ "
                                +
                                p.getPreco()
                );


                tabela.addCell(
                        String.valueOf(
                                p.getEstoque()
                        )
                );


            }



            doc.add(tabela);





            // ================= FINAL =================



            doc.add(
                    new Paragraph(
                            "\nRelatório gerado automaticamente pelo FastFarma."
                    )
            );



            doc.close();



            JOptionPane.showMessageDialog(
                    null,
                    "Relatório atualizado com sucesso!"
            );



        }
        catch(Exception e){


            JOptionPane.showMessageDialog(
                    null,
                    "Erro: "
                            +
                            e.getMessage()
            );


        }


    }




    private static Paragraph tituloSecao(String texto){


        Font fonte =
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        16
                );


        Paragraph p =
                new Paragraph(
                        "\n"+texto,
                        fonte
                );


        return p;

    }



}