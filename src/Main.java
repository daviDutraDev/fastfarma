import model.*;
import repository.*;

import javax.swing.*;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        ProdutoRepository repo = new ProdutoRepository();
        PedidoRepository pedidoRepo = new PedidoRepository();
        UsuarioRepository usuarioRepo = new UsuarioRepository();

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
                                mensagem += p.getId() + " - " + p.getNome() + " - R$" + p.getPreco() + "\n";
                            }

                            JOptionPane.showMessageDialog(null, mensagem);

                        }

                        else if (escolhaCliente == 2) {

                            List<Produto> produtos = repo.listarProdutos();
                            String mensagem = "";

                            for (Produto p : produtos) {
                                mensagem += p.getId() + " - " + p.getNome() + " - R$" + p.getPreco() + "\n";
                            }

                            int escolhaProduto = Integer.parseInt(
                                    JOptionPane.showInputDialog("Escolha o ID do produto:\n" + mensagem)
                            );

                            String nome = usuarioLogado.getNome();

                            int codigo = 1000 + new java.util.Random().nextInt(9000);
                            int idPedido = pedidoRepo.gerarNovoId();

                            Pedido pedido = new Pedido(
                                    idPedido,
                                    codigo,
                                    nome,
                                    StatusPedido.PENDENTE,
                                    escolhaProduto
                            );

                            pedidoRepo.salvarPedido(pedido);

                            JOptionPane.showMessageDialog(null, "Pedido criado!");

                        }

                        else if (escolhaCliente == 3) {

                            List<Pedido> pedidos = pedidoRepo.listarPedidos();
                            String mensagem = "Meus pedidos:\n";

                            for (Pedido p : pedidos) {
                                if (p.getCriadoPor().equals(usuarioLogado.getNome())) {
                                    mensagem += p.getId() + " - " + p.getStatus() + "\n";
                                }
                            }

                            JOptionPane.showMessageDialog(null, mensagem);

                        }

                        else if (escolhaCliente == 4) {
                            break;
                        }
                    }

                }

                // ================= FUNCIONÁRIO =================
                else {

                    while (true) {

                        String menuFuncionario = "FastFarma - Funcionário \n\n" +
                                "1 - Analisar pedidos\n" +
                                "2 - Ver Pedidos\n" +
                                "3 - Listar usuários\n" +
                                "4 - Excluir usuário\n" +
                                "5 - Voltar";

                        int escolhaFuncionario = Integer.parseInt(
                                JOptionPane.showInputDialog(menuFuncionario)
                        );

                        if (escolhaFuncionario == 1) {

                            List<Pedido> pedidos = pedidoRepo.listarPedidos();

                            StringBuilder lista = new StringBuilder("Pedidos:\n");

                            for (Pedido p : pedidos) {
                                if (p.getStatus() != StatusPedido.PRONTO) {
                                    lista.append(p.getId())
                                            .append(" - ")
                                            .append(p.getCriadoPor())
                                            .append(" - ")
                                            .append(p.getStatus())
                                            .append("\n");
                                }
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
                                selecionado.setStatus(StatusPedido.REJEITADO);
                            } else if (acao == 3) {
                                selecionado.setStatus(StatusPedido.PRONTO);
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
                                                .append("Produto ID: ").append(p.getIdProduto()).append("\n")
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
                                                    .append("Código: ").append(p.getCodigoVerificacao()).append("\n")
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
                                                    .append("Código: ").append(p.getCodigoVerificacao()).append("\n")
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
                                                    .append("Código: ").append(p.getCodigoVerificacao()).append("\n")
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

                        else if (escolhaFuncionario == 5) {
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