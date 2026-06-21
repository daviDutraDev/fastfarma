package UI;

import model.*;
import repository.*;
import service.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.swing.Timer;
import java.util.stream.Collectors;

public class MainUI {

    private static final Color COR_SIDEBAR       = new Color(26, 31, 46);
    private static final Color COR_SIDEBAR_TEXTO = new Color(200, 205, 220);
    private static final Color COR_ATIVO         = new Color(74, 222, 128);
    private static final Color COR_BG            = new Color(244, 244, 242);
    private static final Color COR_CARD          = Color.WHITE;
    private static final Color COR_BORDA         = new Color(220, 220, 218);
    private static final Color COR_TEXTO         = new Color(25, 25, 25);
    private static final Color COR_MUTED         = new Color(110, 110, 110);
    private static final Color COR_VERDE         = new Color(34, 120, 30);
    private static final Color COR_VERDE_BG      = new Color(220, 245, 220);
    private static final Color COR_LARANJA       = new Color(160, 90, 10);
    private static final Color COR_LARANJA_BG    = new Color(255, 240, 210);
    private static final Color COR_VERMELHO      = new Color(170, 35, 35);
    private static final Color COR_VERMELHO_BG   = new Color(255, 230, 230);
    private static final Color COR_AZUL          = new Color(20, 90, 170);
    private static final Color COR_AZUL_BG       = new Color(220, 235, 255);
    private static final Color COR_BTN_ESCURO    = new Color(26, 31, 46);

    private JFrame frame;
    private JPanel painelConteudo;
    private JLabel lblTopbarTitulo;
    private Usuario usuarioLogado;
    private String abaAtiva = "";
    private final Map<String, JPanel>  itensNav  = new LinkedHashMap<>();
    private final Map<String, Integer> estadoNav = new LinkedHashMap<>();

    private final ProdutoRepository produtoRepo = new ProdutoRepository();
    private final PedidoRepository  pedidoRepo  = new PedidoRepository();
    private final UsuarioRepository usuarioRepo = new UsuarioRepository();
    private final PedidoService     pedidoSvc   = new PedidoService();
    private final EstoqueService    estoqueSvc  = new EstoqueService();

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> { new UsuarioRepository().criarAdminSeNaoExistir(); new MainUI().telaLogin(); });
    }

    // ===================== BOTAO CUSTOM (sem bug de cor branca) =====================
    private JButton criarBtn(String txt, Color bg, Color fg) {
        JButton b = new JButton(txt) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? bg.darker() : getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                FontMetrics fm = g.getFontMetrics();
                g.setColor(fg); g.setFont(getFont());
                g.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setForeground(fg); b.setContentAreaFilled(false); b.setBorderPainted(false);
        b.setFocusPainted(false); b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 18, 8, 18));
        return b;
    }
    private JButton btnPrimario(String t)                    { return criarBtn(t, COR_BTN_ESCURO, Color.WHITE); }
    private JButton btnSecundario(String t)                  { return criarBtn(t, new Color(235,235,233), COR_TEXTO); }
    private JButton btnColorido(String t, Color fg, Color bg){ return criarBtn(t, bg, fg); }

    // ===================== LOGIN =====================
    private void telaLogin() {
        JFrame f = new JFrame("FastFarma"); f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(420, 500); f.setLocationRelativeTo(null); f.setResizable(false);
        JPanel root = new JPanel(new GridBagLayout()); root.setBackground(COR_BG);
        JPanel card = card(); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); card.setBorder(new EmptyBorder(36, 36, 36, 36));
        JLabel logo = new JLabel("FastFarma"); logo.setFont(new Font("Segoe UI", Font.BOLD, 26)); logo.setForeground(COR_BTN_ESCURO); logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sub  = new JLabel("Sistema de Gestao de Farmacia"); sub.setFont(new Font("Segoe UI", Font.PLAIN, 13)); sub.setForeground(COR_MUTED); sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        JTextField cEmail = campo(); JPasswordField cSenha = new JPasswordField(); estilizarCampo(cSenha);
        JButton btnLogin = btnPrimario("Entrar"); btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel lblErro = new JLabel(" "); lblErro.setFont(new Font("Segoe UI", Font.PLAIN, 12)); lblErro.setForeground(COR_VERMELHO); lblErro.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel linkCad = new JLabel("<html><center>Nao tem conta? <a href='#'>Cadastrar</a></center></html>"); linkCad.setFont(new Font("Segoe UI", Font.PLAIN, 13)); linkCad.setForeground(COR_MUTED); linkCad.setAlignmentX(Component.CENTER_ALIGNMENT); linkCad.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.add(logo); card.add(Box.createVerticalStrut(4)); card.add(sub); card.add(Box.createVerticalStrut(28));
        card.add(lbl("Email")); card.add(Box.createVerticalStrut(5)); card.add(cEmail);
        card.add(Box.createVerticalStrut(14)); card.add(lbl("Senha")); card.add(Box.createVerticalStrut(5)); card.add(cSenha);
        card.add(Box.createVerticalStrut(20)); card.add(btnLogin); card.add(Box.createVerticalStrut(10)); card.add(lblErro); card.add(Box.createVerticalStrut(10)); card.add(linkCad);
        root.add(card); f.setContentPane(root);
        ActionListener login = e -> { Usuario u = usuarioRepo.login(cEmail.getText().trim(), new String(cSenha.getPassword())); if (u == null) { lblErro.setText("Email ou senha incorretos."); cSenha.setText(""); } else { usuarioLogado = u; f.dispose(); abrirApp(); } };
        btnLogin.addActionListener(login); cSenha.addActionListener(login);
        linkCad.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { f.dispose(); telaCadastro(); } });
        f.setVisible(true);
    }

    // ===================== CADASTRO =====================
    private void telaCadastro() {
        JFrame f = new JFrame("FastFarma - Cadastro"); f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(420, 510); f.setLocationRelativeTo(null); f.setResizable(false);
        JPanel root = new JPanel(new GridBagLayout()); root.setBackground(COR_BG);
        JPanel card = card(); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); card.setBorder(new EmptyBorder(32, 36, 32, 36));
        JLabel titulo = new JLabel("Criar conta"); titulo.setFont(new Font("Segoe UI", Font.BOLD, 22)); titulo.setForeground(COR_BTN_ESCURO); titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        JTextField cNome = campo(), cEmail = campo(); JPasswordField cSenha = new JPasswordField(); estilizarCampo(cSenha);
        JButton btnCad = btnPrimario("Cadastrar"); btnCad.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel lblErro = new JLabel(" "); lblErro.setFont(new Font("Segoe UI", Font.PLAIN, 12)); lblErro.setForeground(COR_VERMELHO); lblErro.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel voltar = new JLabel("<html><center>Ja tem conta? <a href='#'>Fazer login</a></center></html>"); voltar.setFont(new Font("Segoe UI", Font.PLAIN, 13)); voltar.setForeground(COR_MUTED); voltar.setAlignmentX(Component.CENTER_ALIGNMENT); voltar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.add(titulo); card.add(Box.createVerticalStrut(24));
        card.add(lbl("Nome")); card.add(Box.createVerticalStrut(5)); card.add(cNome);
        card.add(Box.createVerticalStrut(12)); card.add(lbl("Email")); card.add(Box.createVerticalStrut(5)); card.add(cEmail);
        card.add(Box.createVerticalStrut(12)); card.add(lbl("Senha")); card.add(Box.createVerticalStrut(5)); card.add(cSenha);
        card.add(Box.createVerticalStrut(20)); card.add(btnCad); card.add(Box.createVerticalStrut(10)); card.add(lblErro); card.add(Box.createVerticalStrut(8)); card.add(voltar);
        root.add(card); f.setContentPane(root);
        btnCad.addActionListener(e -> { String nome = cNome.getText().trim(), email = cEmail.getText().trim(), senha = new String(cSenha.getPassword()); if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) { lblErro.setText("Preencha todos os campos."); return; } if (usuarioRepo.buscarPorNome(nome) != null) { lblErro.setText("Nome ja existe."); return; } usuarioRepo.salvarUsuario(new Usuario(usuarioRepo.gerarNovoId(), nome, email, senha, TipoUsuario.CLIENTE)); JOptionPane.showMessageDialog(f, "Conta criada!"); f.dispose(); telaLogin(); });
        voltar.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { f.dispose(); telaLogin(); } });
        f.setVisible(true);
    }

    // ===================== APP PRINCIPAL =====================
    private void abrirApp() {
        frame = new JFrame("FastFarma"); frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1140, 700); frame.setMinimumSize(new Dimension(960, 600)); frame.setLocationRelativeTo(null);
        JPanel root = new JPanel(new BorderLayout()); root.setBackground(COR_BG); frame.setContentPane(root);
        root.add(buildSidebar(), BorderLayout.WEST);
        JPanel main = new JPanel(new BorderLayout()); main.setBackground(COR_BG);
        main.add(buildTopbar(), BorderLayout.NORTH);
        painelConteudo = new JPanel(new BorderLayout()); painelConteudo.setBackground(COR_BG);
        main.add(painelConteudo, BorderLayout.CENTER); root.add(main, BorderLayout.CENTER);
        if (usuarioLogado.getTipo() == TipoUsuario.FUNCIONARIO) { ir("Dashboard"); verificarEstoqueBaixo(); } else { ir("Produtos"); }
        frame.setVisible(true);
    }

    // ===================== SIDEBAR =====================
    private JPanel buildSidebar() {
        JPanel sb = new JPanel(); sb.setBackground(COR_SIDEBAR); sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS)); sb.setPreferredSize(new Dimension(215, 0));
        JPanel logoP = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 16)); logoP.setBackground(COR_SIDEBAR); logoP.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        JPanel ponto = new JPanel(); ponto.setBackground(COR_ATIVO); ponto.setPreferredSize(new Dimension(8, 8));
        JLabel logoLabel = new JLabel("FastFarma"); logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 18)); logoLabel.setForeground(Color.WHITE);
        logoP.add(ponto); logoP.add(logoLabel); sb.add(logoP); sb.add(sep());
        JPanel userP = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10)); userP.setBackground(COR_SIDEBAR); userP.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        String ini = usuarioLogado.getNome().length() >= 2 ? usuarioLogado.getNome().substring(0, 2).toUpperCase() : usuarioLogado.getNome().toUpperCase();
        userP.add(avatar(ini, COR_VERDE_BG, COR_VERDE, 32));
        JPanel uInfo = new JPanel(); uInfo.setBackground(COR_SIDEBAR); uInfo.setLayout(new BoxLayout(uInfo, BoxLayout.Y_AXIS));
        JLabel uNome = new JLabel(usuarioLogado.getNome()); uNome.setFont(new Font("Segoe UI", Font.BOLD, 13)); uNome.setForeground(Color.WHITE);
        JLabel uTipo = new JLabel(usuarioLogado.getTipo() == TipoUsuario.FUNCIONARIO ? "Funcionario" : "Cliente"); uTipo.setFont(new Font("Segoe UI", Font.PLAIN, 11)); uTipo.setForeground(new Color(255, 255, 255, 100));
        uInfo.add(uNome); uInfo.add(uTipo); userP.add(uInfo); sb.add(userP); sb.add(sep()); sb.add(Box.createVerticalStrut(6));
        if (usuarioLogado.getTipo() == TipoUsuario.FUNCIONARIO) {
            sb.add(secao("PRINCIPAL")); sb.add(navItem("Dashboard","* ")); sb.add(navItem("Pedidos","= ")); sb.add(navItem("Produtos","o "));
            sb.add(Box.createVerticalStrut(4)); sb.add(secao("GESTAO")); sb.add(navItem("Usuarios","@ ")); sb.add(navItem("Estoque","+ ")); sb.add(navItem("Relatorio","^ "));
            sb.add(Box.createVerticalStrut(4)); sb.add(secao("CONTA")); sb.add(navItem("Meu Perfil","# "));
        } else {
            sb.add(secao("MENU")); sb.add(navItem("Produtos","o ")); sb.add(navItem("Meus Pedidos","= ")); sb.add(navItem("Fazer Pedido","> "));
            sb.add(Box.createVerticalStrut(4)); sb.add(secao("CONTA")); sb.add(navItem("Meu Perfil","# "));
        }
        sb.add(Box.createVerticalGlue()); sb.add(sep());
        JPanel sairP = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 12)); sairP.setBackground(COR_SIDEBAR); sairP.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        JLabel sairBtn = new JLabel("<- Sair"); sairBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13)); sairBtn.setForeground(new Color(255,255,255,80)); sairBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sairBtn.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { frame.dispose(); telaLogin(); } public void mouseEntered(MouseEvent e) { sairBtn.setForeground(new Color(240,100,100)); } public void mouseExited(MouseEvent e) { sairBtn.setForeground(new Color(255,255,255,80)); } });
        sairP.add(sairBtn); sb.add(sairP);
        return sb;
    }

    private JPanel navItem(String nome, String icone) {
        estadoNav.put(nome, 0);
        JPanel item = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int estado = estadoNav.getOrDefault(nome, 0);
                int w = getWidth(), h = getHeight();
                if (estado == 2) { g2.setColor(new Color(74,222,128,35)); g2.fillRect(0,0,w,h); g2.setColor(COR_ATIVO); g2.fillRect(0,0,3,h); }
                else if (estado == 1) { g2.setColor(new Color(255,255,255,18)); g2.fillRect(0,0,w,h); }
                else { g2.setColor(COR_SIDEBAR); g2.fillRect(0,0,w,h); }
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                g2.setColor(estado == 2 ? COR_ATIVO : COR_SIDEBAR_TEXTO);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(icone + nome, 16, (h + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        item.setOpaque(false); item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40)); item.setPreferredSize(new Dimension(215, 40));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        itensNav.put(nome, item);
        item.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { ir(nome); }
            public void mouseEntered(MouseEvent e) { if (!abaAtiva.equals(nome)) { estadoNav.put(nome, 1); item.repaint(); } }
            public void mouseExited(MouseEvent e)  { if (!abaAtiva.equals(nome)) { estadoNav.put(nome, 0); item.repaint(); } }
        });
        return item;
    }

    private JLabel secao(String txt) { JLabel l = new JLabel(txt); l.setFont(new Font("Segoe UI", Font.BOLD, 10)); l.setForeground(new Color(255,255,255,55)); l.setBorder(new EmptyBorder(8,16,4,0)); return l; }
    private JPanel sep() { JPanel p = new JPanel(); p.setBackground(new Color(255,255,255,22)); p.setMaximumSize(new Dimension(Integer.MAX_VALUE,1)); p.setPreferredSize(new Dimension(0,1)); return p; }

    // ===================== TOPBAR =====================
    private JPanel buildTopbar() {
        JPanel bar = new JPanel(new BorderLayout()); bar.setBackground(COR_CARD);
        bar.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,COR_BORDA), new EmptyBorder(0,20,0,16))); bar.setPreferredSize(new Dimension(0,52));
        lblTopbarTitulo = new JLabel("---"); lblTopbarTitulo.setFont(new Font("Segoe UI", Font.BOLD, 15)); lblTopbarTitulo.setForeground(COR_TEXTO);
        bar.add(lblTopbarTitulo, BorderLayout.WEST);
        if (usuarioLogado.getTipo() == TipoUsuario.FUNCIONARIO) { JButton btn = btnPrimario("Gerar Relatorio PDF"); btn.addActionListener(e -> RelatorioService.gerarRelatorio()); bar.add(btn, BorderLayout.EAST); }
        return bar;
    }

    // ===================== NAVEGACAO =====================
    private void ir(String aba) {
        abaAtiva = aba; lblTopbarTitulo.setText(aba);
        for (Map.Entry<String, JPanel> entry : itensNav.entrySet()) { estadoNav.put(entry.getKey(), entry.getKey().equals(aba) ? 2 : 0); entry.getValue().repaint(); }
        painelConteudo.removeAll();
        JPanel conteudo = switch (aba) {
            case "Dashboard"    -> painelDashboard();
            case "Pedidos"      -> painelPedidos();
            case "Produtos"     -> painelProdutos();
            case "Usuarios"     -> painelUsuarios();
            case "Estoque"      -> painelEstoque();
            case "Relatorio"    -> { RelatorioService.gerarRelatorio(); yield painelDashboard(); }
            case "Meus Pedidos" -> painelMeusPedidos();
            case "Fazer Pedido" -> painelFazerPedido();
            case "Meu Perfil"   -> painelMeuPerfil();
            default             -> new JPanel();
        };
        painelConteudo.add(conteudo, BorderLayout.CENTER);
        painelConteudo.revalidate(); painelConteudo.repaint();
    }

    // ===================== DASHBOARD =====================
    private JPanel painelDashboard() {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBackground(COR_BG); p.setBorder(new EmptyBorder(16,16,16,16));
        List<Pedido> pedidos = pedidoRepo.listarPedidos(); List<Produto> produtos = produtoRepo.listarProdutos();
        long pendentes = pedidos.stream().filter(x -> x.getStatus() == StatusPedido.PENDENTE).count();
        long estoqueBaixo = produtos.stream().filter(x -> x.getEstoque() <= 5).count();
        double receita = 0;
        for (Pedido ped : pedidos) if (ped.getStatus() != StatusPedido.REJEITADO) for (int idP : ped.getIdsProdutos()) for (Produto pr : produtos) if (pr.getId() == idP) receita += pr.getPreco();
        JPanel stats = new JPanel(new GridLayout(1,4,10,0)); stats.setBackground(COR_BG); stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));
        stats.add(statCard("Total de pedidos", String.valueOf(pedidos.size()), null, null));
        stats.add(statCard("Pendentes", String.valueOf(pendentes), COR_LARANJA_BG, COR_LARANJA));
        stats.add(statCard("Receita total", moeda(receita), COR_VERDE_BG, COR_VERDE));
        stats.add(statCard("Estoque baixo", String.valueOf(estoqueBaixo), COR_VERMELHO_BG, COR_VERMELHO));
        p.add(stats); p.add(Box.createVerticalStrut(12));
        JPanel cardP = card(); cardP.setLayout(new BorderLayout()); cardP.add(headerCard("Pedidos recentes", pedidos.size() + " total"), BorderLayout.NORTH);
        String[] cols = {"ID","Cliente","Status","Itens","Codigo"};
        Object[][] dados = pedidos.stream().sorted(Comparator.comparingInt(Pedido::getId).reversed()).limit(10)
                .map(ped -> new Object[]{"#"+ped.getId(), ped.getCriadoPor(), ped.getStatus().toString(), ped.getIdsProdutos().size()+" item(s)", ped.getCodigoVerificacao()}).toArray(Object[][]::new);
        JTable t = tabela(dados, cols); t.getColumnModel().getColumn(2).setCellRenderer(new RendererStatus());
        JScrollPane sc = new JScrollPane(t); sc.setBorder(BorderFactory.createEmptyBorder()); cardP.add(sc, BorderLayout.CENTER); p.add(cardP);
        JScrollPane outer = new JScrollPane(p); outer.setBorder(BorderFactory.createEmptyBorder()); outer.getVerticalScrollBar().setUnitIncrement(12);
        JPanel wrap = new JPanel(new BorderLayout()); wrap.setBackground(COR_BG); wrap.add(outer); return wrap;
    }

    // ===================== PEDIDOS =====================
    private JPanel painelPedidos() {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(COR_BG); p.setBorder(new EmptyBorder(16,16,16,16));
        JComboBox<String> combo = comboBox(new String[]{"Todos","PENDENTE","APROVADO","PRONTO","REJEITADO"});
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); topo.setBackground(COR_BG);
        JLabel lf = new JLabel("Filtrar:"); lf.setFont(new Font("Segoe UI", Font.PLAIN, 13)); lf.setForeground(COR_MUTED);
        JLabel dica = new JLabel("  (clique em uma linha para ver os detalhes do pedido)"); dica.setFont(new Font("Segoe UI", Font.PLAIN, 12)); dica.setForeground(COR_MUTED);
        topo.add(lf); topo.add(combo); topo.add(dica); p.add(topo, BorderLayout.NORTH);
        JPanel card = card(); card.setLayout(new BorderLayout());
        String[] cols = {"ID","Cliente","Status","Itens","Codigo","Ver","Analisar"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return c == 5 || c == 6; } };
        JTable t = new JTable(model); estilizarTabela(t);
        t.getColumnModel().getColumn(2).setCellRenderer(new RendererStatus());

        t.getColumnModel().getColumn(5).setCellRenderer((table, value, sel, foc, row, col) -> criarBtn("Ver", COR_VERDE_BG, COR_VERDE));
        t.getColumnModel().getColumn(5).setCellEditor(new EditorBotao("Ver", COR_VERDE_BG, COR_VERDE, () -> {
            int row = t.getSelectedRow(); if (row < 0) return;
            int idPed = Integer.parseInt(t.getValueAt(row, 0).toString().replace("#",""));
            List<Pedido> todos = pedidoRepo.listarPedidos();
            Pedido sel = todos.stream().filter(x -> x.getId() == idPed).findFirst().orElse(null);
            if (sel != null) dialogDetalhesPedido(sel);
        }));

        t.getColumnModel().getColumn(6).setCellRenderer((table, value, sel, foc, row, col) -> criarBtn("Analisar", COR_AZUL_BG, COR_AZUL));
        t.getColumnModel().getColumn(6).setCellEditor(new EditorBotao("Analisar", COR_AZUL_BG, COR_AZUL, () -> {
            int row = t.getSelectedRow(); if (row < 0) return;
            int idPed = Integer.parseInt(t.getValueAt(row, 0).toString().replace("#",""));
            List<Pedido> todos = pedidoRepo.listarPedidos();
            Pedido sel = todos.stream().filter(x -> x.getId() == idPed).findFirst().orElse(null);
            if (sel != null) dialogAnalisar(sel, todos, () -> recarregarPedidos(model, (String) combo.getSelectedItem()));
        }));

        // clicar na linha (fora dos botoes) tambem abre os detalhes
        t.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = t.rowAtPoint(e.getPoint());
                int col = t.columnAtPoint(e.getPoint());
                if (row < 0 || col == 5 || col == 6) return; // ignora cliques nos botoes
                int idPed = Integer.parseInt(t.getValueAt(row, 0).toString().replace("#",""));
                List<Pedido> todos = pedidoRepo.listarPedidos();
                Pedido sel = todos.stream().filter(x -> x.getId() == idPed).findFirst().orElse(null);
                if (sel != null) dialogDetalhesPedido(sel);
            }
        });

        Runnable reload = () -> recarregarPedidos(model, (String) combo.getSelectedItem());
        reload.run(); combo.addActionListener(e -> reload.run());
        t.getColumnModel().getColumn(5).setPreferredWidth(70);
        t.getColumnModel().getColumn(6).setPreferredWidth(100);
        JScrollPane sc = new JScrollPane(t); sc.setBorder(BorderFactory.createEmptyBorder()); card.add(sc, BorderLayout.CENTER); p.add(card, BorderLayout.CENTER);
        return p;
    }

    private void recarregarPedidos(DefaultTableModel m, String filtro) {
        m.setRowCount(0);
        pedidoRepo.listarPedidos().stream().sorted(Comparator.comparingInt(Pedido::getId).reversed())
                .filter(ped -> filtro == null || filtro.equals("Todos") || ped.getStatus().toString().equals(filtro))
                .forEach(ped -> m.addRow(new Object[]{"#"+ped.getId(), ped.getCriadoPor(), ped.getStatus().toString(), ped.getIdsProdutos().size()+" item(s)", ped.getCodigoVerificacao(), "Ver", "Analisar"}));
    }

    // Mostra todos os dados do pedido: produtos, quantidades, precos, total e status
    private void dialogDetalhesPedido(Pedido pedido) {
        JDialog d = new JDialog(frame, "Detalhes do Pedido #" + pedido.getId(), true);
        d.setSize(460, 520); d.setLocationRelativeTo(frame);

        JPanel root = new JPanel(new BorderLayout()); root.setBackground(Color.WHITE);

        JPanel header = new JPanel(); header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Color.WHITE); header.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,COR_BORDA), new EmptyBorder(18,20,16,20)));

        JLabel titulo = new JLabel("Pedido #" + pedido.getId());
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18)); titulo.setForeground(COR_TEXTO);

        JLabel cliente = new JLabel("Cliente: " + pedido.getCriadoPor());
        cliente.setFont(new Font("Segoe UI", Font.PLAIN, 13)); cliente.setForeground(COR_MUTED);

        JLabel codigo = new JLabel("Codigo de verificacao: " + pedido.getCodigoVerificacao());
        codigo.setFont(new Font("Segoe UI", Font.PLAIN, 13)); codigo.setForeground(COR_MUTED);

        JLabel statusLbl = new JLabel("Status: " + pedido.getStatus());
        statusLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        switch (pedido.getStatus().toString()) {
            case "PENDENTE"  -> statusLbl.setForeground(COR_LARANJA);
            case "APROVADO"  -> statusLbl.setForeground(COR_VERDE);
            case "PRONTO"    -> statusLbl.setForeground(COR_AZUL);
            case "REJEITADO" -> statusLbl.setForeground(COR_VERMELHO);
            default          -> statusLbl.setForeground(COR_MUTED);
        }

        header.add(titulo); header.add(Box.createVerticalStrut(6)); header.add(cliente);
        header.add(Box.createVerticalStrut(2)); header.add(codigo); header.add(Box.createVerticalStrut(2)); header.add(statusLbl);
        root.add(header, BorderLayout.NORTH);

        // lista de produtos do pedido com contagem de repetidos
        List<Produto> todosProd = produtoRepo.listarProdutos();
        Map<Integer, Integer> contagem = new LinkedHashMap<>();
        for (int idP : pedido.getIdsProdutos()) contagem.merge(idP, 1, Integer::sum);

        DefaultTableModel modelItens = new DefaultTableModel(new String[]{"Produto","Categoria","Qtd","Preco unit.","Subtotal"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        double total = 0;
        for (Map.Entry<Integer,Integer> ent : contagem.entrySet()) {
            int idP = ent.getKey(); int qtd = ent.getValue();
            Produto pr = todosProd.stream().filter(x -> x.getId() == idP).findFirst().orElse(null);
            String nome = pr != null ? pr.getNome() : ("Produto #" + idP + " (removido)");
            String cat  = pr != null ? categoriaDe(pr.getNome()) : "-";
            double preco = pr != null ? pr.getPreco() : 0;
            double subtotal = preco * qtd;
            total += subtotal;
            modelItens.addRow(new Object[]{nome, cat, qtd, moeda(preco), moeda(subtotal)});
        }

        JTable tItens = new JTable(modelItens); estilizarTabela(tItens);
        JScrollPane scItens = new JScrollPane(tItens); scItens.setBorder(BorderFactory.createEmptyBorder());
        root.add(scItens, BorderLayout.CENTER);

        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setBackground(Color.WHITE); rodape.setBorder(new CompoundBorder(new MatteBorder(1,0,0,0,COR_BORDA), new EmptyBorder(14,20,14,20)));
        JLabel totalLbl = new JLabel("Total do pedido: " + moeda(total));
        totalLbl.setFont(new Font("Segoe UI", Font.BOLD, 15)); totalLbl.setForeground(COR_TEXTO);
        JButton fechar = btnSecundario("Fechar"); fechar.addActionListener(e -> d.dispose());
        rodape.add(totalLbl, BorderLayout.WEST); rodape.add(fechar, BorderLayout.EAST);
        root.add(rodape, BorderLayout.SOUTH);

        d.setContentPane(root); d.setVisible(true);
    }

    private void dialogAnalisar(Pedido pedido, List<Pedido> todos, Runnable aoAtualizar) {
        JDialog d = new JDialog(frame, "Analisar Pedido #"+pedido.getId(), true); d.setSize(400,310); d.setLocationRelativeTo(frame);
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBackground(Color.WHITE); p.setBorder(new EmptyBorder(20,24,20,24));
        JLabel titulo = new JLabel("Pedido #"+pedido.getId()+" - "+pedido.getCriadoPor()); titulo.setFont(new Font("Segoe UI",Font.BOLD,15)); titulo.setForeground(COR_TEXTO);
        JLabel statusA = new JLabel("Status atual: "+pedido.getStatus()); statusA.setFont(new Font("Segoe UI",Font.PLAIN,13)); statusA.setForeground(COR_MUTED);
        JButton bAprovar = btnColorido("Aprovar", COR_VERDE, COR_VERDE_BG), bRejeitar = btnColorido("Rejeitar", COR_VERMELHO, COR_VERMELHO_BG), bPronto = btnColorido("Marcar como Pronto", COR_AZUL, COR_AZUL_BG), bCancelar = btnSecundario("Cancelar");
        for (JButton b : new JButton[]{bAprovar,bRejeitar,bPronto,bCancelar}) b.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));
        bAprovar.addActionListener(e  -> { pedidoSvc.aprovarPedido(pedido); pedidoRepo.salvarListaPedidos(todos); d.dispose(); toast("Pedido #"+pedido.getId()+" aprovado!"); aoAtualizar.run(); });
        bRejeitar.addActionListener(e -> { pedidoSvc.rejeitarPedido(pedido); pedidoRepo.salvarListaPedidos(todos); d.dispose(); toast("Pedido #"+pedido.getId()+" rejeitado."); aoAtualizar.run(); });
        bPronto.addActionListener(e   -> { boolean ok = pedidoSvc.marcarComoPronto(pedido); pedidoRepo.salvarListaPedidos(todos); d.dispose(); toast(ok ? "Pronto! Email enviado." : "Pedido marcado como PRONTO."); aoAtualizar.run(); });
        bCancelar.addActionListener(e -> d.dispose());
        p.add(titulo); p.add(Box.createVerticalStrut(4)); p.add(statusA); p.add(Box.createVerticalStrut(16));
        p.add(lbl("Escolha uma acao:")); p.add(Box.createVerticalStrut(8));
        p.add(bAprovar); p.add(Box.createVerticalStrut(6)); p.add(bRejeitar); p.add(Box.createVerticalStrut(6)); p.add(bPronto); p.add(Box.createVerticalStrut(12)); p.add(bCancelar);
        d.setContentPane(p); d.setVisible(true);
    }

    // ===================== PRODUTOS (com busca + categoria) =====================
    private JPanel painelProdutos() {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(COR_BG); p.setBorder(new EmptyBorder(16,16,16,16));

        // --- barra de busca e filtros ---
        JTextField busca = campoBusca("Buscar produto por nome...");
        JComboBox<String> filtroSit = comboBox(new String[]{"Todos","Disponivel","Esgotado"});
        JComboBox<String> filtroCat = comboBox(todasCategorias());
        JLabel lblCount = new JLabel(""); lblCount.setFont(new Font("Segoe UI",Font.PLAIN,12)); lblCount.setForeground(COR_MUTED);

        JPanel barraFiltro = new JPanel(new BorderLayout(10,0)); barraFiltro.setBackground(COR_BG); barraFiltro.setBorder(new EmptyBorder(0,0,10,0));
        JPanel dir = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); dir.setBackground(COR_BG);
        JLabel lbCat = new JLabel("Categoria:"); lbCat.setFont(new Font("Segoe UI",Font.PLAIN,13)); lbCat.setForeground(COR_MUTED);
        JLabel lbSit = new JLabel("Situacao:"); lbSit.setFont(new Font("Segoe UI",Font.PLAIN,13)); lbSit.setForeground(COR_MUTED);
        dir.add(lbCat); dir.add(filtroCat); dir.add(lbSit); dir.add(filtroSit); dir.add(lblCount);
        barraFiltro.add(busca, BorderLayout.CENTER); barraFiltro.add(dir, BorderLayout.EAST);
        p.add(barraFiltro, BorderLayout.NORTH);

        // --- tabela ---
        JPanel card = card(); card.setLayout(new BorderLayout());
        List<Produto> todosProd = produtoRepo.listarProdutos();
        String[] cols = {"ID","Nome","Categoria","Preco","Estoque","Situacao"};
        DefaultTableModel model = new DefaultTableModel(cols,0) { public boolean isCellEditable(int r,int c){return false;} };
        JTable t = new JTable(model); estilizarTabela(t);
        t.getColumnModel().getColumn(5).setCellRenderer(new RendererDisponibilidade());

        Runnable filtrar = () -> {
            String txt = busca.getText().trim().toLowerCase();
            String sit = (String) filtroSit.getSelectedItem();
            String cat = (String) filtroCat.getSelectedItem();
            model.setRowCount(0); int count = 0;
            for (Produto pr : todosProd) {
                String sitProd = pr.getEstoque() > 0 ? "Disponivel" : "Esgotado";
                String catProd = categoriaDe(pr.getNome());
                boolean nomeOk = pr.getNome().toLowerCase().contains(txt);
                boolean sitOk  = "Todos".equals(sit) || sitProd.equals(sit);
                boolean catOk  = "Todas".equals(cat) || catProd.equals(cat);
                if (nomeOk && sitOk && catOk) {
                    model.addRow(new Object[]{pr.getId(), pr.getNome(), catProd, moeda(pr.getPreco()), pr.getEstoque(), sitProd});
                    count++;
                }
            }
            lblCount.setText(count + " produto(s)");
        };
        filtrar.run();
        busca.getDocument().addDocumentListener(docListener(filtrar));
        filtroSit.addActionListener(e -> filtrar.run());
        filtroCat.addActionListener(e -> filtrar.run());

        JScrollPane sc = new JScrollPane(t); sc.setBorder(BorderFactory.createEmptyBorder());
        card.add(sc, BorderLayout.CENTER); p.add(card, BorderLayout.CENTER);
        return p;
    }

    // ===================== USUARIOS =====================
    private JPanel painelUsuarios() {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(COR_BG); p.setBorder(new EmptyBorder(16,16,16,16));
        JPanel card = card(); card.setLayout(new BorderLayout());
        String[] cols = {"ID","Nome","Email","Tipo","Acao"};
        DefaultTableModel model = new DefaultTableModel(cols,0) { public boolean isCellEditable(int r,int c){return c==4;} };
        JTable t = new JTable(model); estilizarTabela(t);
        t.getColumnModel().getColumn(4).setCellRenderer((table,value,sel,foc,row,col) -> criarBtn("Excluir", COR_VERMELHO_BG, COR_VERMELHO));
        t.getColumnModel().getColumn(4).setCellEditor(new EditorBotao("Excluir", COR_VERMELHO_BG, COR_VERMELHO, () -> {
            int row = t.getSelectedRow(); if (row < 0) return;
            int id = (int)t.getValueAt(row,0); if (id==1){toast("Nao e possivel excluir o admin!");return;}
            String nome = (String)t.getValueAt(row,1);
            if (JOptionPane.showConfirmDialog(frame,"Excluir \""+nome+"\"?","Confirmar",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) { usuarioRepo.excluirUsuario(id); carregarUsuarios(model); toast("Usuario excluido."); }
        }));
        carregarUsuarios(model); card.add(headerCard("Usuarios cadastrados",""), BorderLayout.NORTH);
        JScrollPane sc = new JScrollPane(t); sc.setBorder(BorderFactory.createEmptyBorder()); card.add(sc, BorderLayout.CENTER); p.add(card, BorderLayout.CENTER);
        return p;
    }
    private void carregarUsuarios(DefaultTableModel m) { m.setRowCount(0); usuarioRepo.listarUsuarios().forEach(u -> m.addRow(new Object[]{u.getId(),u.getNome(),u.getEmail(),u.getTipo().toString(),"Excluir"})); }

    // ===================== ESTOQUE (com busca) =====================
    private JPanel painelEstoque() {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(COR_BG); p.setBorder(new EmptyBorder(16,16,16,16));
        JPanel card = card(); card.setLayout(new BorderLayout());
        String[] cols = {"ID","Nome","Preco","Estoque","Situacao"};
        DefaultTableModel model = new DefaultTableModel(cols,0) { public boolean isCellEditable(int r,int c){return false;} };
        JTable t = new JTable(model); estilizarTabela(t);
        t.getColumnModel().getColumn(4).setCellRenderer(new RendererEstoque());

        Runnable reloadBase = () -> { model.setRowCount(0); produtoRepo.listarProdutos().forEach(pr -> model.addRow(new Object[]{pr.getId(),pr.getNome(),moeda(pr.getPreco()),pr.getEstoque(),pr.getEstoque()<=5?"Baixo":"Normal"})); };
        reloadBase.run();

        JButton bAdd = btnPrimario("+ Novo"), bRepor = btnSecundario("Repor estoque"), bDel = btnSecundario("Excluir");
        bAdd.addActionListener(e -> {
            JTextField fN = campo(), fP = campo(), fQ = campo();
            JPanel form = new JPanel(new GridLayout(6,1,4,4)); form.add(new JLabel("Nome:")); form.add(fN); form.add(new JLabel("Preco:")); form.add(fP); form.add(new JLabel("Qtd:")); form.add(fQ);
            if (JOptionPane.showConfirmDialog(frame,form,"Novo Produto",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
                try { int id=produtoRepo.gerarNovoId(); double preco=Double.parseDouble(fP.getText().replace(",",".")); int qtd=Integer.parseInt(fQ.getText().trim()); List<Produto> lista=produtoRepo.listarProdutos(); lista.add(new Produto(id,fN.getText().trim(),preco,qtd)); produtoRepo.salvarListaProdutos(lista); reloadBase.run(); toast("Produto cadastrado!"); } catch(Exception ex){toast("Dados invalidos.");}
            }
        });
        bRepor.addActionListener(e -> { int row=t.getSelectedRow(); if(row<0){toast("Selecione um produto.");return;} int id=(int)t.getValueAt(row,0); String nome=(String)t.getValueAt(row,1); String s=JOptionPane.showInputDialog(frame,"Qtd a adicionar para \""+nome+"\":"); if(s==null||s.trim().isEmpty())return; try{estoqueSvc.adicionarEstoque(id,Integer.parseInt(s.trim()));reloadBase.run();toast("Estoque atualizado!");}catch(Exception ex){toast("Qtd invalida.");} });
        bDel.addActionListener(e -> { int row=t.getSelectedRow(); if(row<0){toast("Selecione um produto.");return;} int id=(int)t.getValueAt(row,0); String nome=(String)t.getValueAt(row,1); if(JOptionPane.showConfirmDialog(frame,"Excluir \""+nome+"\"?","Confirmar",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){produtoRepo.excluirProduto(id);reloadBase.run();toast("Produto excluido.");} });

        // --- barra de busca integrada ---
        JTextField busca = campoBusca("Buscar produto no estoque...");
        JComboBox<String> filtroEst = comboBox(new String[]{"Todos","Normal","Baixo"});
        Runnable filtrarEstoque = () -> {
            String txt = busca.getText().trim().toLowerCase();
            String sit = (String) filtroEst.getSelectedItem();
            model.setRowCount(0);
            produtoRepo.listarProdutos().stream()
                    .filter(pr -> pr.getNome().toLowerCase().contains(txt))
                    .filter(pr -> { String s = pr.getEstoque()<=5?"Baixo":"Normal"; return "Todos".equals(sit)||s.equals(sit); })
                    .forEach(pr -> model.addRow(new Object[]{pr.getId(),pr.getNome(),moeda(pr.getPreco()),pr.getEstoque(),pr.getEstoque()<=5?"Baixo":"Normal"}));
        };
        busca.getDocument().addDocumentListener(docListener(filtrarEstoque));
        filtroEst.addActionListener(e -> filtrarEstoque.run());

        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,8)); acoes.setBackground(Color.WHITE); acoes.add(bRepor); acoes.add(bDel); acoes.add(bAdd);
        JPanel buscaRow = new JPanel(new BorderLayout(8,0)); buscaRow.setBackground(Color.WHITE); buscaRow.setBorder(new EmptyBorder(6,14,8,14));
        JLabel lbEst = new JLabel("Estoque:"); lbEst.setFont(new Font("Segoe UI",Font.PLAIN,13)); lbEst.setForeground(COR_MUTED);
        JPanel buscaDir = new JPanel(new FlowLayout(FlowLayout.RIGHT,6,0)); buscaDir.setBackground(Color.WHITE); buscaDir.add(lbEst); buscaDir.add(filtroEst);
        buscaRow.add(busca, BorderLayout.CENTER); buscaRow.add(buscaDir, BorderLayout.EAST);

        JPanel headerTop = new JPanel(new BorderLayout()); headerTop.setBackground(Color.WHITE); headerTop.setBorder(new MatteBorder(0,0,1,0,COR_BORDA));
        JPanel headerRow = new JPanel(new BorderLayout()); headerRow.setBackground(Color.WHITE); headerRow.add(headerCard("Gestao de Estoque",""), BorderLayout.WEST); headerRow.add(acoes, BorderLayout.EAST);
        headerTop.add(headerRow, BorderLayout.NORTH); headerTop.add(buscaRow, BorderLayout.SOUTH);
        card.add(headerTop, BorderLayout.NORTH);
        JScrollPane sc = new JScrollPane(t); sc.setBorder(BorderFactory.createEmptyBorder()); card.add(sc, BorderLayout.CENTER); p.add(card, BorderLayout.CENTER);
        return p;
    }

    // ===================== MEUS PEDIDOS =====================
    private JPanel painelMeusPedidos() {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(COR_BG); p.setBorder(new EmptyBorder(16,16,16,16));
        JPanel card = card(); card.setLayout(new BorderLayout()); card.add(headerCard("Meus Pedidos",""), BorderLayout.NORTH);
        List<Produto> produtos = produtoRepo.listarProdutos();
        List<Pedido> meus = pedidoRepo.listarPedidos().stream().filter(ped -> ped.getCriadoPor().equalsIgnoreCase(usuarioLogado.getNome())).sorted(Comparator.comparingInt(Pedido::getId).reversed()).collect(Collectors.toList());
        if (meus.isEmpty()) { JLabel vazio = new JLabel("Voce ainda nao fez nenhum pedido.", SwingConstants.CENTER); vazio.setFont(new Font("Segoe UI",Font.PLAIN,14)); vazio.setForeground(COR_MUTED); card.add(vazio, BorderLayout.CENTER); }
        else {
            String[] cols = {"#ID","Status","Itens","Valor est.","Codigo de retirada"};
            Object[][] dados = meus.stream().map(ped -> { double val=0; for(int idP:ped.getIdsProdutos()) for(Produto pr:produtos) if(pr.getId()==idP) val+=pr.getPreco(); return new Object[]{"#"+ped.getId(),ped.getStatus().toString(),ped.getIdsProdutos().size()+" item(s)",moeda(val),ped.getCodigoVerificacao()}; }).toArray(Object[][]::new);
            JTable t = tabela(dados, cols); t.getColumnModel().getColumn(1).setCellRenderer(new RendererStatus());
            JScrollPane sc = new JScrollPane(t); sc.setBorder(BorderFactory.createEmptyBorder()); card.add(sc, BorderLayout.CENTER);
        }
        p.add(card, BorderLayout.CENTER); return p;
    }

    // ===================== FAZER PEDIDO (com busca) =====================
    private JPanel painelFazerPedido() {
        List<Produto> carrinho = new ArrayList<>();

        // --- painel esquerdo: produtos ---
        JPanel cardProd = card(); cardProd.setLayout(new BorderLayout());
        cardProd.add(headerCard("Produtos disponiveis","Selecione e clique em Adicionar"), BorderLayout.NORTH);

        List<Produto> disponiveis = produtoRepo.listarProdutos().stream().filter(pr -> pr.getEstoque()>0).collect(Collectors.toList());
        String[] colsProd = {"ID","Nome","Categoria","Preco","Estoque"};
        DefaultTableModel modelProd = new DefaultTableModel(colsProd,0) { public boolean isCellEditable(int r,int c){return false;} };
        disponiveis.forEach(pr -> modelProd.addRow(new Object[]{pr.getId(),pr.getNome(),categoriaDe(pr.getNome()),moeda(pr.getPreco()),pr.getEstoque()}));

        JTable tProd = new JTable(modelProd); estilizarTabela(tProd); tProd.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // busca + filtro de categoria em tempo real
        JTextField buscaPed = campoBusca("Buscar produto por nome...");
        JComboBox<String> filtroCatPed = comboBox(todasCategorias());
        Runnable filtrarPed = () -> {
            String txt = buscaPed.getText().trim().toLowerCase();
            String cat = (String) filtroCatPed.getSelectedItem();
            modelProd.setRowCount(0);
            disponiveis.stream()
                    .filter(pr -> pr.getNome().toLowerCase().contains(txt))
                    .filter(pr -> "Todas".equals(cat) || categoriaDe(pr.getNome()).equals(cat))
                    .forEach(pr -> modelProd.addRow(new Object[]{pr.getId(),pr.getNome(),categoriaDe(pr.getNome()),moeda(pr.getPreco()),pr.getEstoque()}));
        };
        buscaPed.getDocument().addDocumentListener(docListener(filtrarPed));
        filtroCatPed.addActionListener(e -> filtrarPed.run());

        JPanel buscaPedRow = new JPanel(new BorderLayout(8,0)); buscaPedRow.setBackground(Color.WHITE); buscaPedRow.setBorder(new EmptyBorder(8,14,8,14));
        JPanel catPedRow = new JPanel(new FlowLayout(FlowLayout.RIGHT,6,0)); catPedRow.setBackground(Color.WHITE);
        JLabel lbCatPed = new JLabel("Categoria:"); lbCatPed.setFont(new Font("Segoe UI",Font.PLAIN,13)); lbCatPed.setForeground(COR_MUTED);
        catPedRow.add(lbCatPed); catPedRow.add(filtroCatPed);
        buscaPedRow.add(buscaPed, BorderLayout.CENTER); buscaPedRow.add(catPedRow, BorderLayout.EAST);

        JScrollPane scProd = new JScrollPane(tProd); scProd.setBorder(BorderFactory.createEmptyBorder());

        // --- painel direito: carrinho ---
        JPanel cardCart = card(); cardCart.setLayout(new BorderLayout()); cardCart.add(headerCard("Carrinho",""), BorderLayout.NORTH);
        DefaultListModel<String> modelCart = new DefaultListModel<>();
        JList<String> listaCart = new JList<>(modelCart); listaCart.setFont(new Font("Segoe UI",Font.PLAIN,13)); listaCart.setFixedCellHeight(34); listaCart.setBackground(Color.WHITE); listaCart.setBorder(new EmptyBorder(4,8,4,8)); listaCart.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scCart = new JScrollPane(listaCart); scCart.setBorder(BorderFactory.createEmptyBorder()); cardCart.add(scCart, BorderLayout.CENTER);
        JLabel lblTotal = new JLabel("Total: R$ 0,00"); lblTotal.setFont(new Font("Segoe UI",Font.BOLD,14)); lblTotal.setForeground(COR_TEXTO);
        JButton btnRemover = btnSecundario("Remover item"), btnFinalizar = btnPrimario("Finalizar pedido");
        btnRemover.setPreferredSize(new Dimension(150,36)); btnFinalizar.setPreferredSize(new Dimension(160,36));
        Runnable atualizarTotal = () -> lblTotal.setText("Total: "+moeda(carrinho.stream().mapToDouble(Produto::getPreco).sum())+" ("+carrinho.size()+" item(s))");
        btnRemover.addActionListener(e -> { int idx=listaCart.getSelectedIndex(); if(idx<0){toast("Selecione um item no carrinho.");return;} carrinho.remove(idx); modelCart.remove(idx); atualizarTotal.run(); });
        btnFinalizar.addActionListener(e -> {
            if(carrinho.isEmpty()){toast("Adicione ao menos um produto.");return;}
            for(Produto pr:carrinho) estoqueSvc.baixarEstoque(pr.getId());
            List<Integer> ids=carrinho.stream().map(Produto::getId).collect(Collectors.toList());
            int codigo=1000+new Random().nextInt(9000), idPedido=pedidoRepo.gerarNovoId();
            pedidoRepo.salvarPedido(new Pedido(idPedido,codigo,usuarioLogado.getNome(),StatusPedido.PENDENTE,ids));
            carrinho.clear(); modelCart.clear(); atualizarTotal.run();
            toast("Pedido #"+idPedido+" criado! Codigo: "+codigo);
        });
        JPanel botoesCart = new JPanel(new FlowLayout(FlowLayout.RIGHT,6,0)); botoesCart.setBackground(Color.WHITE); botoesCart.add(btnRemover); botoesCart.add(btnFinalizar);
        JPanel rodapeCart = new JPanel(new BorderLayout()); rodapeCart.setBackground(Color.WHITE); rodapeCart.setBorder(new CompoundBorder(new MatteBorder(1,0,0,0,COR_BORDA),new EmptyBorder(10,14,10,14))); rodapeCart.add(lblTotal, BorderLayout.WEST); rodapeCart.add(botoesCart, BorderLayout.EAST);
        cardCart.add(rodapeCart, BorderLayout.SOUTH);

        // botao adicionar fora da tabela
        JButton btnAdicionar = btnPrimario("+ Adicionar ao carrinho");
        btnAdicionar.addActionListener(e -> {
            int row = tProd.getSelectedRow(); if(row<0){toast("Selecione um produto.");return;}
            int modelRow = tProd.convertRowIndexToModel(row); if(modelRow<0||modelRow>=modelProd.getRowCount())return;
            int idProd = (int) modelProd.getValueAt(modelRow, 0);
            Produto escolhido = disponiveis.stream().filter(pr -> pr.getId()==idProd).findFirst().orElse(null);
            if(escolhido==null)return;
            carrinho.add(escolhido); modelCart.addElement(escolhido.getNome()+"  -  "+moeda(escolhido.getPreco())); atualizarTotal.run();
            toast("\""+escolhido.getNome()+"\" adicionado ao carrinho.");
        });
        JPanel rodapeProd = new JPanel(new BorderLayout()); rodapeProd.setBackground(Color.WHITE); rodapeProd.setBorder(new CompoundBorder(new MatteBorder(1,0,0,0,COR_BORDA),new EmptyBorder(10,14,10,14))); rodapeProd.add(btnAdicionar, BorderLayout.EAST);

        // monta o painel esquerdo na ordem correta: header / tabela / busca+botao
        cardProd.add(scProd, BorderLayout.CENTER);
        JPanel sulProd = new JPanel(new BorderLayout()); sulProd.setBackground(Color.WHITE);
        sulProd.add(buscaPedRow, BorderLayout.NORTH); sulProd.add(rodapeProd, BorderLayout.SOUTH);
        cardProd.add(sulProd, BorderLayout.SOUTH);

        JPanel split = new JPanel(new GridLayout(1,2,12,0)); split.setBackground(COR_BG); split.setBorder(new EmptyBorder(16,16,16,16)); split.add(cardProd); split.add(cardCart);
        return split;
    }

    // ===================== MEU PERFIL =====================
    private JPanel painelMeuPerfil() {
        JPanel p = new JPanel(new GridBagLayout()); p.setBackground(COR_BG); p.setBorder(new EmptyBorder(16,16,16,16));

        JPanel card = card(); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(28,32,28,32));
        card.setPreferredSize(new Dimension(420, 460));

        String iniciais = usuarioLogado.getNome().length() >= 2 ? usuarioLogado.getNome().substring(0,2).toUpperCase() : usuarioLogado.getNome().toUpperCase();
        JLabel av = avatar(iniciais, COR_VERDE_BG, COR_VERDE, 64);
        JPanel avWrap = new JPanel(); avWrap.setBackground(Color.WHITE); avWrap.add(av);

        JLabel titulo = new JLabel("Meu Perfil"); titulo.setFont(new Font("Segoe UI",Font.BOLD,20)); titulo.setForeground(COR_TEXTO); titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel tipoLbl = new JLabel(usuarioLogado.getTipo()==TipoUsuario.FUNCIONARIO?"Funcionario":"Cliente"); tipoLbl.setFont(new Font("Segoe UI",Font.PLAIN,12)); tipoLbl.setForeground(COR_MUTED); tipoLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        avWrap.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField fNome  = campo(); fNome.setText(usuarioLogado.getNome());
        JTextField fEmail = campo(); fEmail.setText(usuarioLogado.getEmail());
        JPasswordField fSenhaAtual = new JPasswordField(); estilizarCampo(fSenhaAtual);
        JPasswordField fSenhaNova  = new JPasswordField(); estilizarCampo(fSenhaNova);

        JButton btnSalvar = btnPrimario("Salvar alteracoes"); btnSalvar.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));
        JLabel lblMsg = new JLabel(" "); lblMsg.setFont(new Font("Segoe UI",Font.PLAIN,12)); lblMsg.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnSalvar.addActionListener(e -> {
            String novoNome  = fNome.getText().trim();
            String novoEmail = fEmail.getText().trim();
            String senhaAtual = new String(fSenhaAtual.getPassword());
            String senhaNova  = new String(fSenhaNova.getPassword());

            if (novoNome.isEmpty() || novoEmail.isEmpty()) { lblMsg.setForeground(COR_VERMELHO); lblMsg.setText("Nome e email nao podem ficar vazios."); return; }

            // se mudou o nome, verifica duplicidade (exceto o proprio usuario)
            Usuario existente = usuarioRepo.buscarPorNome(novoNome);
            if (existente != null && existente.getId() != usuarioLogado.getId()) {
                lblMsg.setForeground(COR_VERMELHO); lblMsg.setText("Esse nome de usuario ja esta em uso."); return;
            }

            String senhaFinal = usuarioLogado.getSenha();
            if (!senhaNova.isEmpty() || !senhaAtual.isEmpty()) {
                if (!senhaAtual.equals(usuarioLogado.getSenha())) { lblMsg.setForeground(COR_VERMELHO); lblMsg.setText("Senha atual incorreta."); return; }
                if (senhaNova.isEmpty()) { lblMsg.setForeground(COR_VERMELHO); lblMsg.setText("Digite a nova senha."); return; }
                senhaFinal = senhaNova;
            }

            Usuario atualizado = new Usuario(usuarioLogado.getId(), novoNome, novoEmail, senhaFinal, usuarioLogado.getTipo());
            atualizarUsuario(atualizado);
            usuarioLogado = atualizado;

            lblMsg.setForeground(COR_VERDE); lblMsg.setText("Perfil atualizado com sucesso!");
            fSenhaAtual.setText(""); fSenhaNova.setText("");
            toast("Perfil atualizado!");

            // atualiza sidebar (nome/iniciais) recarregando a tela
            ir("Meu Perfil");
        });

        card.add(avWrap); card.add(Box.createVerticalStrut(10));
        card.add(titulo); card.add(Box.createVerticalStrut(2)); card.add(tipoLbl);
        card.add(Box.createVerticalStrut(24));
        card.add(lbl("Nome de usuario")); card.add(Box.createVerticalStrut(5)); card.add(fNome);
        card.add(Box.createVerticalStrut(12));
        card.add(lbl("Email")); card.add(Box.createVerticalStrut(5)); card.add(fEmail);
        card.add(Box.createVerticalStrut(16));
        card.add(lbl("Senha atual (preencha para alterar a senha)")); card.add(Box.createVerticalStrut(5)); card.add(fSenhaAtual);
        card.add(Box.createVerticalStrut(12));
        card.add(lbl("Nova senha")); card.add(Box.createVerticalStrut(5)); card.add(fSenhaNova);
        card.add(Box.createVerticalStrut(20));
        card.add(btnSalvar); card.add(Box.createVerticalStrut(10)); card.add(lblMsg);

        p.add(card);
        return p;
    }

    // Atualiza um usuario existente reescrevendo o arquivo via excluir + recriar com mesmo ID
    private void atualizarUsuario(Usuario atualizado) {
        usuarioRepo.excluirUsuario(atualizado.getId());
        usuarioRepo.salvarUsuario(atualizado);
    }


    private JPanel card() { JPanel p = new JPanel(); p.setBackground(COR_CARD); p.setBorder(new LineBorder(COR_BORDA,1,true)); return p; }

    private JPanel statCard(String label, String valor, Color bg, Color fg) {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        p.setBackground(bg!=null?bg:new Color(248,248,246)); p.setBorder(new CompoundBorder(new LineBorder(COR_BORDA,1,true),new EmptyBorder(14,16,14,16)));
        JLabel l = new JLabel(label); l.setFont(new Font("Segoe UI",Font.PLAIN,11)); l.setForeground(fg!=null?fg:COR_MUTED);
        JLabel v = new JLabel(valor); v.setFont(new Font("Segoe UI",Font.BOLD,26)); v.setForeground(fg!=null?fg:COR_TEXTO);
        p.add(l); p.add(Box.createVerticalStrut(5)); p.add(v); return p;
    }

    private JPanel headerCard(String titulo, String sub) {
        JPanel h = new JPanel(new BorderLayout()); h.setBackground(Color.WHITE); h.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,COR_BORDA),new EmptyBorder(12,16,12,16)));
        JLabel t = new JLabel(titulo); t.setFont(new Font("Segoe UI",Font.BOLD,14)); t.setForeground(COR_TEXTO); h.add(t, BorderLayout.WEST);
        if (sub!=null&&!sub.isEmpty()) { JLabel s = new JLabel(sub); s.setFont(new Font("Segoe UI",Font.PLAIN,12)); s.setForeground(COR_MUTED); h.add(s, BorderLayout.EAST); }
        return h;
    }

    private JTable tabela(Object[][] dados, String[] cols) { JTable t = new JTable(dados,cols){public boolean isCellEditable(int r,int c){return false;}}; estilizarTabela(t); return t; }

    private void estilizarTabela(JTable t) {
        t.setFont(new Font("Segoe UI",Font.PLAIN,13)); t.setRowHeight(36); t.setShowGrid(false); t.setIntercellSpacing(new Dimension(0,0));
        t.setSelectionBackground(new Color(74,222,128,50)); t.setSelectionForeground(COR_TEXTO);
        t.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12)); t.getTableHeader().setBackground(new Color(248,248,246)); t.getTableHeader().setForeground(COR_MUTED); t.getTableHeader().setBorder(new MatteBorder(0,0,1,0,COR_BORDA)); t.getTableHeader().setPreferredSize(new Dimension(0,36));
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int col){
                super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,col);
                if(!isSelected) setBackground(row%2==0?Color.WHITE:new Color(249,249,247));
                setBorder(new EmptyBorder(0,12,0,12)); return this;
            }
        });
    }

    private JLabel lbl(String txt) { JLabel l=new JLabel(txt); l.setFont(new Font("Segoe UI",Font.BOLD,12)); l.setForeground(COR_MUTED); l.setAlignmentX(Component.LEFT_ALIGNMENT); return l; }

    private JTextField campo() { JTextField f=new JTextField(); estilizarCampo(f); return f; }

    private void estilizarCampo(JTextField f) {
        f.setFont(new Font("Segoe UI",Font.PLAIN,14)); f.setMaximumSize(new Dimension(Integer.MAX_VALUE,38)); f.setPreferredSize(new Dimension(0,38));
        f.setBorder(new CompoundBorder(new LineBorder(COR_BORDA,1,true),new EmptyBorder(4,10,4,10))); f.setBackground(Color.WHITE); f.setForeground(COR_TEXTO);
    }

    // campo de busca com placeholder desenhado manualmente
    private JTextField campoBusca(String placeholder) {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    g.setColor(COR_MUTED);
                    g.setFont(new Font("Segoe UI",Font.PLAIN,13));
                    g.drawString("  " + placeholder, 10, 20);
                }
            }
        };
        f.setFont(new Font("Segoe UI",Font.PLAIN,13));
        f.setPreferredSize(new Dimension(0,36));
        f.setBorder(new CompoundBorder(new LineBorder(COR_BORDA,1,true),new EmptyBorder(4,10,4,10)));
        f.setBackground(Color.WHITE); f.setForeground(COR_TEXTO);
        return f;
    }

    // DocumentListener utilitario
    private DocumentListener docListener(Runnable r) {
        return new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { r.run(); }
            public void removeUpdate(DocumentEvent e)  { r.run(); }
            public void changedUpdate(DocumentEvent e) { r.run(); }
        };
    }

    private JComboBox<String> comboBox(String[] itens) {
        JComboBox<String> c = new JComboBox<>(itens); c.setFont(new Font("Segoe UI",Font.PLAIN,13)); c.setBackground(Color.WHITE); c.setPreferredSize(new Dimension(140,32)); return c;
    }

    private JLabel avatar(String ini, Color bg, Color fg, int sz) {
        JLabel l = new JLabel(ini, SwingConstants.CENTER) {
            protected void paintComponent(Graphics g) { Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(getBackground()); g2.fillOval(0,0,getWidth(),getHeight()); g2.dispose(); super.paintComponent(g); }
        };
        l.setFont(new Font("Segoe UI",Font.BOLD,(int)(sz*0.38))); l.setBackground(bg); l.setForeground(fg); l.setOpaque(false); l.setPreferredSize(new Dimension(sz,sz)); return l;
    }

    private void toast(String msg) {
        JDialog d = new JDialog(frame); d.setUndecorated(true); d.setSize(380,46); d.setAlwaysOnTop(true);
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT,14,10)); p.setBackground(COR_BTN_ESCURO); p.setBorder(new LineBorder(new Color(255,255,255,30),1,true));
        JLabel l = new JLabel("OK  " + msg); l.setFont(new Font("Segoe UI",Font.PLAIN,13)); l.setForeground(Color.WHITE); p.add(l); d.setContentPane(p);
        Point loc = frame.getLocationOnScreen(); Dimension sz = frame.getSize(); d.setLocation(loc.x+sz.width-400, loc.y+sz.height-70); d.setVisible(true);
        new Timer(2800,e->d.dispose()){{setRepeats(false);start();}};
    }

    private void verificarEstoqueBaixo() {
        List<Produto> baixos = produtoRepo.listarProdutos().stream().filter(pr->pr.getEstoque()<=5).collect(Collectors.toList());
        if (!baixos.isEmpty()) { StringBuilder msg=new StringBuilder("Produtos com estoque baixo:\n\n"); for(Produto pr:baixos) msg.append("- ").append(pr.getNome()).append(" (").append(pr.getEstoque()).append(" unid.)\n"); SwingUtilities.invokeLater(()->JOptionPane.showMessageDialog(frame,msg.toString(),"Alerta de Estoque",JOptionPane.WARNING_MESSAGE)); }
    }

    private String moeda(double v) { return String.format("R$ %.2f", v); }

    // ===================== CATEGORIA (heuristica por nome, sem alterar model.Produto) =====================
    private static final String[][] PALAVRAS_CATEGORIA = {
            {"Analgesico",  "dipirona","paracetamol","ibuprofeno","aspirina","dor","analgesic"},
            {"Antibiotico",  "amoxicilina","azitromicina","cefalexina","antibiotic"},
            {"Vitamina",     "vitamina","complexo b","polivitaminico","suplemento"},
            {"Antialergico", "loratadina","alergia","antialergico","cetirizina"},
            {"Higiene",      "sabonete","shampoo","alcool","gel","mascara","protetor"},
            {"Gastrico",     "omeprazol","antiacido","gastrico","buscopan"}
    };

    private String categoriaDe(String nomeProduto) {
        String n = nomeProduto.toLowerCase();
        for (String[] grupo : PALAVRAS_CATEGORIA) {
            for (int i = 1; i < grupo.length; i++) {
                if (n.contains(grupo[i])) return grupo[0];
            }
        }
        return "Geral";
    }

    private String[] todasCategorias() {
        List<String> cats = new ArrayList<>();
        cats.add("Todas");
        for (String[] g : PALAVRAS_CATEGORIA) cats.add(g[0]);
        cats.add("Geral");
        return cats.toArray(new String[0]);
    }

    // ===================== RENDERERS =====================
    static class RendererStatus extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int r,int c){
            super.getTableCellRendererComponent(t,v,sel,foc,r,c); String s=v!=null?v.toString():""; setBorder(new EmptyBorder(0,12,0,12)); setFont(new Font("Segoe UI",Font.BOLD,12));
            switch(s){case "PENDENTE"->{ setBackground(new Color(255,240,210));setForeground(new Color(160,90,10));}case "APROVADO"->{ setBackground(new Color(220,245,220));setForeground(new Color(34,120,30));}case "PRONTO"->{ setBackground(new Color(220,235,255));setForeground(new Color(20,90,170));}case "REJEITADO"->{ setBackground(new Color(255,230,230));setForeground(new Color(170,35,35));}default->{ setBackground(Color.WHITE);setForeground(new Color(80,80,80));}}
            if(sel){setBackground(new Color(74,222,128,50));setForeground(new Color(25,25,25));} return this;
        }
    }
    static class RendererDisponibilidade extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int r,int c){
            super.getTableCellRendererComponent(t,v,sel,foc,r,c); setBorder(new EmptyBorder(0,12,0,12)); setFont(getFont().deriveFont(Font.BOLD));
            String s=v!=null?v.toString():""; if("Disponivel".equals(s)) setForeground(new Color(34,120,30)); else setForeground(new Color(170,35,35)); return this;
        }
    }
    static class RendererEstoque extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int r,int c){
            super.getTableCellRendererComponent(t,v,sel,foc,r,c); setBorder(new EmptyBorder(0,12,0,12));
            String s=v!=null?v.toString():""; if("Baixo".equals(s)){setForeground(new Color(170,35,35));setFont(getFont().deriveFont(Font.BOLD));}else{setForeground(new Color(34,120,30));setFont(getFont().deriveFont(Font.PLAIN));} return this;
        }
    }

    // ===================== EditorBotao =====================
    private static class EditorBotao extends AbstractCellEditor implements TableCellEditor {
        private final JButton botao;
        EditorBotao(String texto, Color bg, Color fg, Runnable acao) {
            botao = new JButton(texto) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isPressed()?bg.darker():bg); g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6); g2.dispose();
                    FontMetrics fm=g.getFontMetrics(); g.setColor(fg); g.setFont(getFont()); g.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2);
                }
            };
            botao.setFont(new Font("Segoe UI",Font.PLAIN,12)); botao.setContentAreaFilled(false); botao.setBorderPainted(false); botao.setFocusPainted(false); botao.setOpaque(false); botao.setBorder(new EmptyBorder(4,12,4,12)); botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            botao.addActionListener(e -> { fireEditingStopped(); acao.run(); });
        }
        public Component getTableCellEditorComponent(JTable table,Object value,boolean isSelected,int row,int column){ table.setRowSelectionInterval(row,row); return botao; }
        public Object getCellEditorValue() { return botao.getText(); }
    }
}