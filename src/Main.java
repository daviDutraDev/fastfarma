import model.*;
import repository.*;
import service.EmailService;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import service.NotificacaoService;

public class Main {

    public static void main(String[] args) {

        ProdutoRepository repo = new ProdutoRepository();
        PedidoRepository pedidoRepo = new PedidoRepository();
        UsuarioRepository usuarioRepo = new UsuarioRepository();
        EmailService emailService = new EmailService();

        // cria admin automaticamente
        usuarioRepo.criarAdminSeNaoExistir();

        while (true) {

            String menu = "FastFarma \n\n" +
                    "1 - Cadastrar\n" +
                    "2 - Login\n" +
                    "3 - Sair";

            int escolha;

            try {
                escolha = Integer.parseInt(JOptionPane.showInputDialog(menu));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Digite um valor válido!");
                continue;
            }

            // ================= CADASTRO =================
            if (escolha == 1) {

                String nome = JOptionPane.showInputDialog("Nome:");
                Usuario existente = usuarioRepo.buscarPorNome(nome);
                if (existente != null) {

                    JOptionPane.showMessageDialog(
                            null,
                            "Esse nome de usuário já existe!"
                    );

                    continue;
                }
                String email = JOptionPane.showInputDialog("Email:");
                String senha = JOptionPane.showInputDialog("Senha:");

                int id = usuarioRepo.gerarNovoId();

                Usuario novo = new Usuario(id, nome, email, senha, TipoUsuario.CLIENTE);

                usuarioRepo.salvarUsuario(novo);

                JOptionPane.showMessageDialog(null, "Usuário cadastrado!");

            }

            // ================= LOGIN =================
            else if (escolha == 2) {

                String email = JOptionPane.showInputDialog("Email:");
                String senha = JOptionPane.showInputDialog("Senha:");

                Usuario usuarioLogado = usuarioRepo.login(email, senha);

                if (usuarioLogado == null) {
                    JOptionPane.showMessageDialog(null, "Login inválido!");
                    continue;
                }

                // ================= CLIENTE =================
                if (usuarioLogado.getTipo() == TipoUsuario.CLIENTE) {

                    while (true) {

                        String menuCliente = "FastFarma - Cliente \n\n" +
                                "1 - Ver produtos\n" +
                                "2 - Fazer pedido\n" +
                                "3 - Ver meus pedidos\n" +
                                "4 - Voltar";

                        int escolhaCliente = Integer.parseInt(JOptionPane.showInputDialog(menuCliente));

                        if (escolhaCliente == 1) {

                            List<Produto> produtos = repo.listarProdutos();
                            String mensagem = "";

                            for (Produto p : produtos) {
                                mensagem += p.getId() + " - " + p.getNome() + " - R$" + p.getPreco()  + p.getEstoque()  + "\n";
                            }

                            JOptionPane.showMessageDialog(null, mensagem);

                        }

                        else if (escolhaCliente == 2) {

                            List<Produto> produtos = repo.listarProdutos();

                            // LISTA DOS PRODUTOS ESCOLHIDOS
                            List<Integer> produtosEscolhidos = new ArrayList<>();

                            while (true) {

                                // RECRIAR A MENSAGEM A CADA LOOP
                                StringBuilder mensagem = new StringBuilder();

                                mensagem.append("========== FASTFARMA ==========\n");
                                mensagem.append("Produtos disponíveis:\n\n");

                                for (Produto p : produtos) {

                                    mensagem.append("ID: ")
                                            .append(p.getId())
                                            .append(" | ")
                                            .append(p.getNome())
                                            .append(" | R$")
                                            .append(p.getPreco());

                                    // MOSTRAR ESTOQUE
                                    if (p.getEstoque() > 0) {

                                        mensagem.append(" | Estoque: ")
                                                .append(p.getEstoque());

                                    } else {

                                        mensagem.append(" | ESGOTADO");
                                    }

                                    mensagem.append("\n");
                                }

                                String input = JOptionPane.showInputDialog(
                                        mensagem +
                                                "\nDigite o ID do produto" +
                                                "\nDigite 0 para finalizar"
                                );

                                // CANCELAR
                                if (input == null) {
                                    break;
                                }

                                int idProduto;

                                try {

                                    idProduto = Integer.parseInt(input);

                                } catch (Exception e) {

                                    JOptionPane.showMessageDialog(
                                            null,
                                            "Digite um número válido!"
                                    );

                                    continue;
                                }

                                // FINALIZAR PEDIDO
                                if (idProduto == 0) {
                                    break;
                                }

                                Produto produtoEscolhido = null;

                                // PROCURAR PRODUTO
                                for (Produto p : produtos) {

                                    if (p.getId() == idProduto) {

                                        produtoEscolhido = p;
                                        break;
                                    }
                                }

                                // PRODUTO NÃO EXISTE
                                if (produtoEscolhido == null) {

                                    JOptionPane.showMessageDialog(
                                            null,
                                            "Produto não encontrado!"
                                    );

                                    continue;
                                }

                                // SEM ESTOQUE
                                if (produtoEscolhido.getEstoque() <= 0) {

                                    JOptionPane.showMessageDialog(
                                            null,
                                            "Produto sem estoque!"
                                    );

                                    continue;
                                }

                                // ADICIONAR AO PEDIDO
                                produtosEscolhidos.add(idProduto);

                                // BAIXAR ESTOQUE
                                produtoEscolhido.setEstoque(
                                        produtoEscolhido.getEstoque() - 1
                                );

                                // SALVAR ESTOQUE
                                repo.salvarListaProdutos(produtos);

                                JOptionPane.showMessageDialog(
                                        null,
                                        produtoEscolhido.getNome() +
                                                " adicionado ao pedido!"
                                );
                            }

                            // NENHUM PRODUTO ESCOLHIDO
                            if (produtosEscolhidos.isEmpty()) {

                                JOptionPane.showMessageDialog(
                                        null,
                                        "Nenhum produto selecionado!"
                                );

                                continue;
                            }

                            String nome = usuarioLogado.getNome();

                            int codigo = 1000 + new java.util.Random().nextInt(9000);

                            int idPedido = pedidoRepo.gerarNovoId();

                            Pedido pedido = new Pedido(
                                    idPedido,
                                    codigo,
                                    nome,
                                    StatusPedido.PENDENTE,
                                    produtosEscolhidos
                            );

                            // SALVAR PEDIDO
                            pedidoRepo.salvarPedido(pedido);

                            JOptionPane.showMessageDialog(
                                    null,
                                    "========== PEDIDO CRIADO ==========\n\n" +
                                            "Pedido Nº: " + idPedido +
                                            "\nCódigo: " + codigo +
                                            "\nCliente: " + nome +
                                            "\nQuantidade de produtos: " + produtosEscolhidos.size()
                            );
                        }

                        else if (escolhaCliente == 3) {
                            StringBuilder VerPedidos = new StringBuilder();

                            List<Pedido> pedidos = pedidoRepo.listarPedidos();
                            String mensagem = "Meus pedidos:\n";

                            for (Pedido p : pedidos) {
                                if (p.getCriadoPor().equals(usuarioLogado.getNome())) {
                                    VerPedidos.append("ID: ").append(p.getId()).append("\n")
                                            .append("Cliente: ").append(p.getCriadoPor()).append("\n")
                                            .append("Produto ID: ").append(p.getIdsProdutos()).append("\n")
                                            .append("Código: ").append(p.getCodigoVerificacao()).append("\n")
                                            .append("Status: ").append(p.getStatus()).append("\n")
                                            .append("----------------------\n");
                                }


                            }

                            if (VerPedidos.isEmpty()) {
                                JOptionPane.showMessageDialog(null, "Você não possui pedidos.");
                            } else {
                                JOptionPane.showMessageDialog(null, VerPedidos.toString());
                            }



                        }

                        else if (escolhaCliente == 4) {
                            break;
                        }
                    }

                }

                // ================= FUNCIONÁRIO =================
                else {


                    // VERIFICAR ESTOQUE
                    List<Produto> produtosNotificacao = repo.listarProdutos();

                    for (Produto p : produtosNotificacao) {

                        if (p.getEstoque() <= 5) {

                            NotificacaoService.mostrarNotificacao(
                                    "⚠ Estoque Baixo",
                                    p.getNome()
                                            + " possui apenas "
                                            + p.getEstoque()
                                            + " unidades"
                            );
                        }
                    }


                    while (true) {

                        String menuFuncionario = "FastFarma - Funcionário \n\n" +
                                "1 - Analisar Pedidos\n" +
                                "2 - Ver Pedidos\n" +
                                "3 - Listar Usuários\n" +
                                "4 - Excluir Usuário\n" +
                                "5 - Gerenciar Estoque\n" +
                                "6 - Voltar";

                        int escolhaFuncionario = Integer.parseInt(
                                JOptionPane.showInputDialog(menuFuncionario)
                        );

                        if (escolhaFuncionario == 1) {

                            List<Pedido> pedidos = pedidoRepo.listarPedidos();

                            StringBuilder lista = new StringBuilder("Pedidos:\n");

                            for (Pedido p : pedidos) {

                                lista.append("ID: ").append(p.getId()).append("\n")
                                        .append("Cliente: ").append(p.getCriadoPor()).append("\n")
                                        .append("Produto ID: ").append(p.getIdsProdutos()).append("\n")
                                        .append("Código: ").append(p.getCodigoVerificacao()).append("\n")
                                        .append("Status: ").append(p.getStatus()).append("\n")
                                        .append("----------------------\n");

                            }

                            String inputId = JOptionPane.showInputDialog(lista + "\nDigite o ID:");
                            int idEscolhido = Integer.parseInt(inputId);

                            Pedido selecionado = null;

                            for (Pedido p : pedidos) {
                                if (p.getId() == idEscolhido) {
                                    selecionado = p;
                                    break;
                                }
                            }

                            if (selecionado == null) {
                                JOptionPane.showMessageDialog(null, "Pedido não encontrado!");
                                continue;
                            }

                            int acao = Integer.parseInt(
                                    JOptionPane.showInputDialog(
                                            "1 - Aprovar\n2 - Rejeitar\n3 - Marcar como pronto"
                                    )
                            );

                            if (acao == 1) {
                                selecionado.setStatus(StatusPedido.APROVADO);
                            } else if (acao == 2) {
                                if (selecionado.getStatus() == StatusPedido.REJEITADO) {

                                    JOptionPane.showMessageDialog(
                                            null,
                                            "Pedido já está rejeitado!"
                                    );

                                    continue;
                                }
                                selecionado.setStatus(StatusPedido.REJEITADO);
                                List<Produto> produtos = repo.listarProdutos();

                                 // DEVOLVER ESTOQUE
                                for (int idProduto : selecionado.getIdsProdutos()) {

                                    for (Produto produto : produtos) {

                                        if (produto.getId() == idProduto) {

                                            produto.setEstoque(
                                                    produto.getEstoque() + 1
                                            );
                                        }
                                    }
                                }


                                repo.salvarListaProdutos(produtos);
                            } else if (acao == 3) {
                                selecionado.setStatus(StatusPedido.PRONTO);

                                // BUSCAR USUÁRIO DO PEDIDO
                                Usuario usuarioPedido = usuarioRepo.buscarPorNome(
                                        selecionado.getCriadoPor()
                                );

                                // VERIFICA SE ENCONTROU
                                if (usuarioPedido != null) {

                                    String mensagemEmail =
                                            "Olá, " + usuarioPedido.getNome() + "!\n\n"

                                                    + "Seu pedido na FastFarma já está pronto "
                                                    + "para retirada.\n\n"

                                                    + "Código de verificação: "
                                                    + selecionado.getCodigoVerificacao()
                                                    + "\n\n"

                                                    + "Apresente este código no momento "
                                                    + "da retirada do pedido.\n\n"

                                                    + "Agradecemos pela preferência!\n\n"

                                                    + "Equipe FastFarma\n\n"

                                                    + "--------------------------------------------------\n"

                                                    + "Caso você tenha recebido este email "
                                                    + "por engano, desconsidere esta mensagem.";

                                    // ENVIAR EMAIL
                                    boolean enviado = emailService.enviarEmail(
                                            usuarioPedido.getEmail(),
                                            "Pedido pronto - FastFarma",
                                            mensagemEmail
                                    );

                                    if (enviado) {

                                        JOptionPane.showMessageDialog(
                                                null,
                                                "Pedido marcado como PRONTO!\n" +
                                                        "Email enviado!"
                                        );

                                    } else {

                                        JOptionPane.showMessageDialog(
                                                null,
                                                "Pedido marcado como PRONTO!\n" +
                                                        "Email não Enviado."
                                        );
                                    }

                                } else {

                                    JOptionPane.showMessageDialog(
                                            null,
                                            "Usuário do pedido não encontrado!"
                                    );
                                }
                            } else {
                                JOptionPane.showMessageDialog(null, "Opção inválida!");
                                continue;
                            }

                            pedidoRepo.salvarListaPedidos(pedidos);

                            JOptionPane.showMessageDialog(null, "Pedido atualizado!");

                        }

                        else if (escolhaFuncionario == 2) {

                            while (true) {

                                String menuPedidos = "VER PEDIDOS\n\n" +
                                        "1 - Todos\n" +
                                        "2 - Pendentes\n" +
                                        "3 - Aprovados\n" +
                                        "4 - Prontos\n" +
                                        "5 - Voltar";

                                int op = Integer.parseInt(JOptionPane.showInputDialog(menuPedidos));

                                List<Pedido> pedidos = pedidoRepo.listarPedidos();
                                StringBuilder lista = new StringBuilder();

                                if (op == 1) {
                                    lista.append("TODOS OS PEDIDOS:\n\n");

                                    for (Pedido p : pedidos) {
                                        lista.append("ID: ").append(p.getId()).append("\n")
                                                .append("Cliente: ").append(p.getCriadoPor()).append("\n")
                                                .append("Produto ID: ").append(p.getIdsProdutos()).append("\n")
                                                .append("Código: ").append(p.getCodigoVerificacao()).append("\n")
                                                .append("Status: ").append(p.getStatus()).append("\n")
                                                .append("----------------------\n");
                                    }
                                }

                                else if (op == 2) {
                                    lista.append("PENDENTES:\n\n");

                                    for (Pedido p : pedidos) {
                                        if (p.getStatus() == StatusPedido.PENDENTE) {
                                            lista.append("ID: ").append(p.getId()).append("\n")
                                                    .append("Cliente: ").append(p.getCriadoPor()).append("\n")
                                                    .append("Produto ID: ").append(p.getIdsProdutos()).append("\n")
                                                    .append("Código: ").append(p.getCodigoVerificacao()).append("\n")
                                                    .append("Status: ").append(p.getStatus()).append("\n")
                                                    .append("----------------------\n");
                                        }
                                    }
                                }

                                else if (op == 3) {
                                    lista.append("APROVADOS:\n\n");

                                    for (Pedido p : pedidos) {
                                        if (p.getStatus() == StatusPedido.APROVADO) {
                                            lista.append("ID: ").append(p.getId()).append("\n")
                                                    .append("Cliente: ").append(p.getCriadoPor()).append("\n")
                                                    .append("Produto ID: ").append(p.getIdsProdutos()).append("\n")
                                                    .append("Código: ").append(p.getCodigoVerificacao()).append("\n")
                                                    .append("Status: ").append(p.getStatus()).append("\n")
                                                    .append("----------------------\n");
                                        }
                                    }
                                }

                                else if (op == 4) {
                                    lista.append("PRONTOS:\n\n");

                                    for (Pedido p : pedidos) {
                                        if (p.getStatus() == StatusPedido.PRONTO) {
                                            lista.append("ID: ").append(p.getId()).append("\n")
                                                    .append("Cliente: ").append(p.getCriadoPor()).append("\n")
                                                    .append("Produto ID: ").append(p.getIdsProdutos()).append("\n")
                                                    .append("Código: ").append(p.getCodigoVerificacao()).append("\n")
                                                    .append("Status: ").append(p.getStatus()).append("\n")
                                                    .append("----------------------\n");
                                        }
                                    }
                                }

                                else if (op == 5) {
                                    break;
                                }

                                else {
                                    JOptionPane.showMessageDialog(null, "Opção inválida!");
                                    continue;
                                }

                                if (lista.toString().trim().isEmpty()) {
                                    JOptionPane.showMessageDialog(null, "Nenhum pedido encontrado!");
                                } else {
                                    JOptionPane.showMessageDialog(null, lista.toString());
                                }
                            }
                        }




                        else if (escolhaFuncionario == 3) {

                            String lista = usuarioRepo.listarUsuariosFormatado();

                            JOptionPane.showMessageDialog(null, lista);
                        }

                        else if (escolhaFuncionario == 4) {

                            String lista = usuarioRepo.listarUsuariosFormatado();

                            int id = Integer.parseInt(
                                    JOptionPane.showInputDialog("Usuários:\n" + lista + "\nDigite o ID para excluir:")
                            );

                            if (id == 1) {
                                JOptionPane.showMessageDialog(null, "Não é possível excluir o admin!");
                                continue;
                            }

                            usuarioRepo.excluirUsuario(id);

                            JOptionPane.showMessageDialog(null, "Usuário excluído!");
                        }

                        // ================= GERENCIAR ESTOQUE =================

                        else if (escolhaFuncionario == 5) {


                                while (true) {

                                    String menuEstoque =
                                            "========= GERENCIAR ESTOQUE =========\n\n" +
                                                    "1 - Listar Produtos\n" +
                                                    "2 - Exluir Produto\n" +
                                                    "3 - Cadastrar Produto\n" +
                                                    "4 - Repor Estoque\n" +
                                                    "5 - Voltar\n\n" +
                                                    "Digite uma opção:";

                                    int escolhaEstoque = Integer.parseInt(
                                            JOptionPane.showInputDialog(menuEstoque)
                                    );

                                    // ================= LISTAR PRODUTOS =================

                                    if (escolhaEstoque == 1) {

                                        List<Produto> produtos = repo.listarProdutos();

                                        StringBuilder lista = new StringBuilder();

                                        lista.append("========= ESTOQUE =========\n\n");

                                        for (Produto p : produtos) {

                                            lista.append("ID: ")
                                                    .append(p.getId())
                                                    .append(" | ")
                                                    .append(p.getNome())
                                                    .append(" | R$")
                                                    .append(p.getPreco())
                                                    .append(" | Estoque: ")
                                                    .append(p.getEstoque())
                                                    .append("\n");
                                        }

                                        JOptionPane.showMessageDialog(null, lista);
                                    }

                                    // ================= EXCLUIR PRODUTO =================
                                    else if (escolhaEstoque == 2) {

                                        List<Produto> produtos = repo.listarProdutos();

                                        StringBuilder lista = new StringBuilder();

                                        lista.append("========= PRODUTOS =========\n\n");

                                        for (Produto p : produtos) {

                                            lista.append("ID: ")
                                                    .append(p.getId())
                                                    .append(" | ")
                                                    .append(p.getNome())
                                                    .append(" | Estoque: ")
                                                    .append(p.getEstoque())
                                                    .append("\n");
                                        }

                                        int idExcluir = Integer.parseInt(
                                                JOptionPane.showInputDialog(
                                                        lista +
                                                                "\nDigite o ID do produto para excluir:"
                                                )
                                        );

                                        repo.excluirProduto(idExcluir);

                                        JOptionPane.showMessageDialog(
                                                null,
                                                "Produto removido com sucesso!"
                                        );
                                    }

                                    // ================= CADASTRAR PRODUTO =================

                                    else if (escolhaEstoque == 3) {

                                        List<Produto> produtos = repo.listarProdutos();

                                        int novoId = produtos.size() + 1;

                                        String nome = JOptionPane.showInputDialog(
                                                "Digite o nome do produto:"
                                        );

                                        double preco = Double.parseDouble(
                                                JOptionPane.showInputDialog(
                                                        "Digite o preço:"
                                                )
                                        );

                                        int estoque = Integer.parseInt(
                                                JOptionPane.showInputDialog(
                                                        "Digite a quantidade em estoque:"
                                                )
                                        );

                                        Produto novoProduto = new Produto(
                                                novoId,
                                                nome,
                                                preco,
                                                estoque
                                        );

                                        produtos.add(novoProduto);

                                        repo.salvarListaProdutos(produtos);

                                        JOptionPane.showMessageDialog(
                                                null,
                                                "Produto cadastrado com sucesso!"
                                        );
                                    }

                                    // ================= REPOR ESTOQUE =================

                                    else if (escolhaEstoque == 4) {

                                        List<Produto> produtos = repo.listarProdutos();

                                        StringBuilder lista = new StringBuilder();

                                        lista.append("========= PRODUTOS =========\n\n");

                                        for (Produto p : produtos) {

                                            lista.append("ID: ")
                                                    .append(p.getId())
                                                    .append(" | ")
                                                    .append(p.getNome())
                                                    .append(" | Estoque: ")
                                                    .append(p.getEstoque())
                                                    .append("\n");
                                        }

                                        int idProduto = Integer.parseInt(
                                                JOptionPane.showInputDialog(
                                                        lista +
                                                                "\nDigite o ID do produto:"
                                                )
                                        );

                                        Produto produtoSelecionado = null;

                                        for (Produto p : produtos) {

                                            if (p.getId() == idProduto) {
                                                produtoSelecionado = p;
                                                break;
                                            }
                                        }

                                        if (produtoSelecionado == null) {

                                            JOptionPane.showMessageDialog(
                                                    null,
                                                    "Produto não encontrado!"
                                            );

                                            continue;
                                        }

                                        int quantidade = Integer.parseInt(
                                                JOptionPane.showInputDialog(
                                                        "Quantidade para adicionar:"
                                                )
                                        );

                                        produtoSelecionado.setEstoque(
                                                produtoSelecionado.getEstoque() + quantidade
                                        );

                                        repo.salvarListaProdutos(produtos);

                                        JOptionPane.showMessageDialog(
                                                null,
                                                "Estoque atualizado com sucesso!"
                                        );
                                    }

                                    // ================= VOLTAR =================

                                    else if (escolhaEstoque == 5) {
                                        break;
                                    }

                                    else {

                                        JOptionPane.showMessageDialog(
                                                null,
                                                "Opção inválida!"
                                        );
                                    }
                                }
                            }


                        else if (escolhaFuncionario == 6){
                            break;
                        }
                    }
                }
            }

            // ================= SAIR =================
            else if (escolha == 3) {
                JOptionPane.showMessageDialog(null, "Saindo...");
                break;
            }
        }
    }
}