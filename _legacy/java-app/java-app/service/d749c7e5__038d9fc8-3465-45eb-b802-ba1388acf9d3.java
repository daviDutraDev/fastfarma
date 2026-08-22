package service;


import model.*;
import repository.*;



public class PedidoService {


    private PedidoRepository pedidoRepo;

    private EstoqueService estoqueService;

    private UsuarioRepository usuarioRepo;

    private EmailService emailService;



    public PedidoService() {


        pedidoRepo = new PedidoRepository();

        estoqueService = new EstoqueService();

        usuarioRepo = new UsuarioRepository();

        emailService = new EmailService();

    }




    public void aprovarPedido(Pedido pedido) {


        pedido.setStatus(
                StatusPedido.APROVADO
        );




    }




    public void rejeitarPedido(Pedido pedido) {


        pedido.setStatus(
                StatusPedido.REJEITADO
        );


        estoqueService.devolverEstoque(
                pedido.getIdsProdutos()
        );




    }




    public boolean marcarComoPronto(Pedido pedido) {


        pedido.setStatus(
                StatusPedido.PRONTO
        );


        Usuario usuario =
                usuarioRepo.buscarPorNome(
                        pedido.getCriadoPor()
                );


        boolean enviado = false;



        if(usuario != null) {


            String mensagem =
                    "Olá, " + usuario.getNome() + "!\n\n"

                            + "Seu pedido na FastFarma já está pronto "
                            + "para retirada.\n\n"

                            + "Código de verificação: "
                            + pedido.getCodigoVerificacao()
                            + "\n\n"

                            + "Apresente este código no momento "
                            + "da retirada do pedido.\n\n"

                            + "Agradecemos pela preferência!\n\n"

                            + "Equipe FastFarma\n\n"

                            + "--------------------------------------------------\n"

                            + "Caso você tenha recebido este email "
                            + "por engano, desconsidere esta mensagem.";



            enviado =
                    emailService.enviarEmail(
                            usuario.getEmail(),
                            "Pedido pronto - FastFarma",
                            mensagem
                    );

        }






        return enviado;

    }






}